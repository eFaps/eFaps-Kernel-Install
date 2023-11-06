/*
 * Copyright 2003 - 2020 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.efaps.esjp.admin.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminAccess;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIAdminUser;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.ci.CIType;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.rest.Update;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@EFapsUUID("97f7ea4f-cacf-41e1-9b0e-8d1aefd4089a")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractUpdate
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUpdate.class);

    protected Map<String, URL> getFiles(final String _fileName, final InputStream _inputStream)
    {
        final Map<String, URL> files = new HashMap<>();
        final boolean compress = GzipUtils.isCompressedFilename(_fileName);
        try (

                        TarArchiveInputStream tarInput = new TarArchiveInputStream(
                                        compress ? new GzipCompressorInputStream(_inputStream) : _inputStream);) {

            File tmpfld = AppConfigHandler.get().getTempFolder();
            if (tmpfld == null) {
                final File temp = File.createTempFile("eFaps", ".tmp");
                tmpfld = temp.getParentFile();
                temp.delete();
            }
            final File updateFolder = new File(tmpfld, Update.TMPFOLDERNAME);
            if (!updateFolder.exists()) {
                updateFolder.mkdirs();
            }
            final File dateFolder = new File(updateFolder, ((Long) new Date().getTime()).toString());
            dateFolder.mkdirs();

            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            while (currentEntry != null) {
                final byte[] bytess = new byte[(int) currentEntry.getSize()];
                tarInput.read(bytess);
                final File file = new File(dateFolder.getAbsolutePath() + "/" + currentEntry.getName());
                file.getParentFile().mkdirs();
                final FileOutputStream output = new FileOutputStream(file);
                output.write(bytess);
                output.close();
                files.put(currentEntry.getName(), file.toURI().toURL());
                currentEntry = tarInput.getNextTarEntry();
            }

        } catch (final IOException e) {
            LOG.error("Catched", e);
        }
        return files;
    }

    protected Map<RevItem, InstallFile> getInstallFiles(final Map<String, URL> _files)
        throws JsonParseException, JsonMappingException, IOException, URISyntaxException, EFapsException
    {
        final Map<RevItem, InstallFile> installFiles = new HashMap<>();
        final URL json = _files.get("revisions.json");
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        final List<RevItem> items = mapper.readValue(new File(json.toURI()), mapper.getTypeFactory()
                        .constructCollectionType(List.class, RevItem.class));

        installFiles.putAll(getInstallFiles(_files, items, CIAdmin.Abstract));
        installFiles.putAll(getInstallFiles(_files, items, CIAdminUser.Abstract));
        installFiles.putAll(getInstallFiles(_files, items, CIAdminAccess.AccessSet));
        installFiles.putAll(getInstallFiles(_files, items, CICommon.DBPropertiesBundle));

        int i = 0;
        for (RevItem item : items) {
            LOG.info("Adding unfound Item {} / {}: {}", i, items.size(), item.getIdentifier());
            final InstallFile installFile = new InstallFile()
                            .setName(item.getName4InstallFile())
                            .setURL(item.getURL(_files))
                            .setType(item.getFileType().getType())
                            .setRevision(item.getRevision())
                            .setDate(item.getDate());
            installFiles.put(item, installFile);
            i++;
        }
        return installFiles;
    }

    protected void update(final Map<String, URL> _files)
        throws InstallationException, EFapsException
    {
        try {
            final Map<RevItem, InstallFile> installFiles = getInstallFiles(_files);
            final List<RevItem> allItems = new ArrayList<>(installFiles.keySet());
            final List<InstallFile> installFileList = new ArrayList<>(installFiles.values());
            Collections.sort(installFileList, Comparator.comparing(InstallFile::getName));

            final List<InstallFile> dependendFileList = new ArrayList<>();
            // check if a object that depends on another object must be added to
            // the update
            final Map<String, String> depenMap = getDependendMap();
            final Set<String> tobeAdded = new HashSet<>();
            for (final RevItem item : installFiles.keySet()) {
                if (depenMap.containsKey(item.getIdentifier())) {
                    tobeAdded.add(depenMap.get(item.getIdentifier()));
                }
            }
            if (!tobeAdded.isEmpty()) {
                // check if the object to be added is already part ot the list
                for (final RevItem item : installFiles.keySet()) {
                    final Iterator<String> tobeiter = tobeAdded.iterator();
                    while (tobeiter.hasNext()) {
                        final String ident = tobeiter.next();
                        if (item.getIdentifier().equals(ident)) {
                            tobeiter.remove();
                        }
                    }
                }
            }
            int i = 0;
            if (!tobeAdded.isEmpty()) {
                i = 1;
                // add the objects to the list that are missing
                for (final RevItem item : allItems) {
                    if (tobeAdded.contains(item.getIdentifier())) {
                        LOG.info("Adding releated Item {} / {}: {}", i, tobeAdded.size(), item);
                        final InstallFile installFile = new InstallFile()
                                        .setName(item.getName4InstallFile())
                                        .setURL(item.getURL(_files))
                                        .setType(item.getFileType().getType())
                                        .setRevision(item.getRevision())
                                        .setDate(item.getDate());
                        dependendFileList.add(installFile);
                        i++;
                    }
                }
            }
            final MultiValuedMap<String, String> updateables = MultiMapUtils.newSetValuedHashMap();
            if (!installFileList.isEmpty()) {
                final Install install = new Install(true);
                for (final InstallFile installFile : installFileList) {
                    LOG.info("...Adding to Update: '{}' ", installFile.getName());
                    install.addFile(installFile);
                }
                updateables.putAll(install.updateLatest(null));
            }
            if (!dependendFileList.isEmpty()) {
                LOG.info("Update for related Items");
                final Install install = new Install(true);
                for (final InstallFile installFile : dependendFileList) {
                    LOG.info("...Adding to Update: '{}' ", installFile.getName());
                    install.addFile(installFile);
                }
                updateables.putAll(install.updateLatest(null));
            }

            final List<InstallFile> updateablesFileList = new ArrayList<>();
            Collections.sort(updateablesFileList, Comparator.comparing(InstallFile::getName));
            for (final Entry<String, String> entry : updateables.entries()) {
                final String value = entry.getValue();
                if (UUIDUtil.isUUID(value)) {
                    final Optional<RevItem> revItemOpt = installFiles.keySet().stream().filter(item -> value.equals(item
                                    .getIdentifier())).findFirst();
                    if (revItemOpt.isPresent()) {
                        updateablesFileList.add(installFiles.get(revItemOpt.get()));
                    }
                }
            }
            if (!updateablesFileList.isEmpty()) {
                LOG.info("Update for updateable Items");
                final Install install = new Install(true);
                for (final InstallFile installFile : updateablesFileList) {
                    LOG.info("...Adding to Update: '{}' ", installFile.getName());
                    install.addFile(installFile);
                }
                install.updateLatest(null);
            }
            LOG.info("Terminated update.");
        } catch (final IOException e) {
            LOG.error("Catched", e);
        } catch (final URISyntaxException e) {
            LOG.error("Catched", e);
        }
    }

    /**
     * Gets the dependend map.
     *
     * @return the dependend map
     * @throws EFapsException the e faps exception
     */
    protected Map<String, String> getDependendMap()
        throws EFapsException
    {
        final Map<String, String> ret = new HashMap<>();
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Menu2Command);
        final MultiPrintQuery multi = queryBldr.getPrint();
        final SelectBuilder selFromUUID = SelectBuilder.get().linkto(CIAdminUserInterface.Menu2Command.FromMenu)
                        .attribute(CIAdminUserInterface.Menu.UUID);
        final SelectBuilder selToUUID = SelectBuilder.get().linkto(CIAdminUserInterface.Menu2Command.ToCommand)
                        .attribute(CIAdminUserInterface.Command.UUID);
        multi.addSelect(selFromUUID, selToUUID);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            ret.put(multi.<String>getSelect(selFromUUID), multi.<String>getSelect(selToUUID));
        }
        return ret;
    }

    /**
     * Gets the install files.
     *
     * @param _files the files
     * @param _items the items
     * @param _ciType the ci type
     * @return the install files
     * @throws EFapsException on error
     */
    protected Map<RevItem, InstallFile> getInstallFiles(final Map<String, URL> _files,
                                                        final List<RevItem> _items,
                                                        final CIType _ciType)
        throws EFapsException
    {
        final Map<RevItem, InstallFile> ret = new HashMap<>();
        final Iterator<RevItem> iter = _items.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final RevItem item = iter.next();
            LOG.info("Checking Item {} / {}: {}", i, _items.size(), item.getIdentifier());

            final MultiPrintQuery multi = getQueryBldr(item, _ciType).getPrint();
            final SelectBuilder selRevision = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink)
                            .attribute(CIAdminCommon.ApplicationRevision.Revision);
            final SelectBuilder selRevDate = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink)
                            .attribute(CIAdminCommon.ApplicationRevision.Date);
            final SelectBuilder selApp = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink)
                            .linkto(CIAdminCommon.ApplicationRevision.ApplicationLink)
                            .attribute(CIAdminCommon.Application.Name);
            multi.addSelect(selRevision, selApp, selRevDate);
            multi.addAttribute(CIAdmin.Abstract.Name);
            multi.executeWithoutAccessCheck();
            boolean update = false;
            if (multi.next()) {
                final String revision = multi.getSelect(selRevision);
                final DateTime revDate = multi.getSelect(selRevDate);
                final String app = multi.getSelect(selApp);
                final String name = multi.getAttribute(CIAdmin.Abstract.Name);
                LOG.info("Found obj: {}, type: {}, name: {},  app: {}, date: {}, revision: {} ",
                                multi.getCurrentInstance().getOid(),
                                multi.getCurrentInstance().getType().getName(), name,
                                app, revDate, revision);
                if (!item.getApplication().equals(app)) {
                    LOG.warn("Different Application: {} - {}", item.getApplication(), app);
                    update = true;
                }
                if (!item.getRevision().equals(revision)) {
                    LOG.warn("Different Revision: {} - {}", item.getRevision(), revision);
                    update = true;
                }
                if (!update && item.getDate().isAfter(revDate)
                                && Seconds.secondsBetween(item.getDate(), revDate).getSeconds() > 2) {
                    LOG.warn("Different Date: {} - {}", item.getDate(), revDate);
                    update = true;
                }

                if (update) {
                    final InstallFile installFile = new InstallFile()
                                    .setName(item.getName4InstallFile(name))
                                    .setURL(item.getURL(_files))
                                    .setType(item.getFileType().getType())
                                    .setRevision(item.getRevision())
                                    .setDate(item.getDate());
                    ret.put(item, installFile);
                }
                iter.remove();
            }
            i++;
        }
        return ret;
    }

    /**
     * Gets the query bldr.
     *
     * @param _item the item
     * @param _ciType the ci type
     * @return the query bldr
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldr(final RevItem _item,
                                        final CIType _ciType)
        throws EFapsException
    {
        QueryBuilder ret = null;
        switch (_item.getFileType()) {
            case XML:
                ret = new QueryBuilder(_ciType);
                ret.addWhereAttrEqValue(CIAdmin.Abstract.UUID, _item.getIdentifier());
                break;
            case JAVA:
                ret = new QueryBuilder(CIAdminProgram.Java);
                ret.addWhereAttrEqValue(CIAdminProgram.Java.Name, _item.getIdentifier());
                break;
            case CSS:
                ret = new QueryBuilder(CIAdminProgram.CSS);
                ret.addWhereAttrEqValue(CIAdminProgram.CSS.Name, _item.getIdentifier());
                break;
            case JS:
                ret = new QueryBuilder(CIAdminProgram.JavaScript);
                ret.addWhereAttrEqValue(CIAdminProgram.JavaScript.Name, _item.getIdentifier());
                break;
            case JRXML:
                ret = new QueryBuilder(CIAdminProgram.JasperReport);
                ret.addWhereAttrEqValue(CIAdminProgram.JasperReport.UUID, _item.getIdentifier());
                break;
            default:
                break;
        }
        return ret;
    }

    protected void install(final List<InstallFile> _installFiles)
        throws InstallationException, EFapsException
    {
        Collections.sort(_installFiles,
                        Comparator.comparing(InstallFile::getName));

        if (!_installFiles.isEmpty()) {
            final Install install = new Install(true);
            for (final InstallFile installFile : _installFiles) {
                LOG.info("...Adding to Update: '{}' ", installFile.getName());
                install.addFile(installFile);
            }
            install.updateLatest(null);
        }
    }

    /**
     * The Class RevItem.
     *
     * @author The eFaps Team
     */
    public static class RevItem
    {

        /** The file type. */
        private FileType fileType;

        /** The identifier. */
        private String identifier;

        /** The application. */
        private String application;

        /** The revision. */
        private String revision;

        /** The date. */
        private DateTime date;

        /**
         * Gets the file type.
         *
         * @return the file type
         */
        public FileType getFileType()
        {
            return fileType;
        }

        /**
         * Sets the file type.
         *
         * @param _fileType the new file type
         */
        public void setFileType(final FileType _fileType)
        {
            fileType = _fileType;
        }

        /**
         * Getter method for the instance variable {@link #application}.
         *
         * @return value of instance variable {@link #application}
         */
        public String getApplication()
        {
            return application;
        }

        /**
         * Sets the application.
         *
         * @param _application the new application
         */
        public void setApplication(final String _application)
        {
            application = _application;
        }

        /**
         * Getter method for the instance variable {@link #revision}.
         *
         * @return value of instance variable {@link #revision}
         */
        public String getRevision()
        {
            return revision;
        }

        /**
         * Sets the revision.
         *
         * @param _revision the new revision
         */
        public void setRevision(final String _revision)
        {
            revision = _revision;
        }

        /**
         * Getter method for the instance variable {@link #identifier}.
         *
         * @return value of instance variable {@link #identifier}
         */
        public String getIdentifier()
        {
            return identifier;
        }

        /**
         * Sets the identifier.
         *
         * @param _identifier the new identifier
         */
        public void setIdentifier(final String _identifier)
        {
            identifier = _identifier;
        }

        /**
         * Gets the date.
         *
         * @return the date
         */
        public DateTime getDate()
        {
            return date;
        }

        /**
         * Sets the date.
         *
         * @param _date the new date
         */
        public void setDate(final DateTime _date)
        {
            date = _date;
        }

        /**
         * Gets the name 4 install file.
         *
         * @return the name 4 install file
         */
        public String getName4InstallFile()
        {
            final String ret = switch (getFileType()) {
                case JAVA -> getIdentifier() + ".java";
                default -> getIdentifier();
            };
            return ret;
        }

        /**
         * Gets the name 4 install file.
         *
         * @param _name the name
         * @return the name 4 install file
         */
        public String getName4InstallFile(final String _name)
        {
            final String ret = switch (getFileType()) {
                case JAVA -> getIdentifier() + ".java";
                default -> _name;
            };
            return ret;
        }

        /**
         * Gets the url.
         *
         * @param _files the files
         * @return the url
         */
        public URL getURL(final Map<String, URL> _files)
        {
            final URL ret = switch (getFileType()) {
                case JAVA -> _files.get(getIdentifier().replace('.', '/') + ".java");
                case CSS -> _files.get(StringUtils.removeEnd(getIdentifier(), ".css").replace('.', '/') + ".css");
                case JS -> _files.get(StringUtils.removeEnd(getIdentifier(), ".js").replace('.', '/') + ".js");
                case JRXML -> _files.get(getIdentifier() + ".jrxml");
                default -> _files.get(getIdentifier());
            };
            return ret;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
