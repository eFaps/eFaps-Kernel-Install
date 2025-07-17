/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.admin.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.efaps.eql.EQL;
import org.efaps.esjp.ci.CICommon;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@EFapsUUID("97f7ea4f-cacf-41e1-9b0e-8d1aefd4089a")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractUpdate
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUpdate.class);

    protected Map<String, URL> getFiles(final String _fileName,
                                        final InputStream _inputStream)
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
            final File updateFolder = new File(tmpfld, "eFapsUpdate");
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

    protected Map<RevItem, InstallFile> getInstallFiles(final Map<String, URL> files,
                                                        final List<RevItem> items)
        throws JsonParseException, JsonMappingException, IOException, URISyntaxException, EFapsException
    {
        final Map<RevItem, InstallFile> installFiles = new HashMap<>();
        installFiles.putAll(getInstallFiles(files, items, CIAdmin.Abstract));
        installFiles.putAll(getInstallFiles(files, items, CIAdminUser.Abstract));
        installFiles.putAll(getInstallFiles(files, items, CIAdminAccess.AccessSet));
        installFiles.putAll(getInstallFiles(files, items, CICommon.DBPropertiesBundle));

        int i = 0;
        for (final RevItem item : items) {
            LOG.info("Adding unfound Item {} / {}: {}", i, items.size(), item.getIdentifier());
            final InstallFile installFile = new InstallFile()
                            .setName(item.getName4InstallFile())
                            .setURL(item.getURL(files))
                            .setType(item.getFileType().getType())
                            .setRevision(item.getRevision())
                            .setDate(item.getDate());
            installFiles.put(item, installFile);
            i++;
        }
        return installFiles;
    }

    protected List<RevItem> getRevItemList(final Map<String, URL> files)
        throws StreamReadException, DatabindException, IOException, URISyntaxException
    {
        final URL json = files.get("revisions.json");
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper.readValue(new File(json.toURI()), mapper.getTypeFactory()
                        .constructCollectionType(List.class, RevItem.class));

    }

    protected void update(final Map<String, URL> files)
        throws InstallationException, EFapsException
    {
        try {
            final var items = getRevItemList(files);

            final Map<RevItem, InstallFile> installFiles = getInstallFiles(files, items);
            final List<InstallFile> installFileList = new ArrayList<>(installFiles.values());
            Collections.sort(installFileList, Comparator.comparing(InstallFile::getName));

            final MultiValuedMap<String, String> updateables = MultiMapUtils.newSetValuedHashMap();
            if (!installFileList.isEmpty()) {
                final Install install = new Install(true);
                for (final InstallFile installFile : installFileList) {
                    LOG.info("...Adding to Update: '{}' ", installFile.getName());
                    install.addFile(installFile);
                }
                updateables.putAll(install.updateLatest(null));
            }
            final var disconnectedChildren = updateables.get(CIAdminUserInterface.Menu.uuid.toString());
            reconnectChildren(files, disconnectedChildren);

            LOG.info("Terminated update.");
        } catch (final IOException e) {
            LOG.error("Catched", e);
        } catch (final URISyntaxException e) {
            LOG.error("Catched", e);
        }
    }

    protected void reconnectChildren(final Map<String, URL> files,
                                     final Collection<String> disconnectedChildren)
        throws IOException, EFapsException
    {
        if (disconnectedChildren != null) {
            LOG.info("Checking disconnected Children of Menus for reconnect");
            final var mapper = new XmlMapper();
            for (final var disconnectedChild : disconnectedChildren) {
                final var url = files.get(disconnectedChild);
                final var node = mapper.readTree(url);
                final var definition = node.get("definition");
                if (definition.has("parents")) {
                    final var parents = definition.get("parents");
                    final var parentList = parents.get("parent");
                    for (final var parent : parentList) {
                        final var parentName = parent.asText();
                        LOG.info("Tyring to reconnect: {} with  {}", disconnectedChild, parentName);
                        final var parentEval = EQL.builder()
                                        .print().query(CIAdminUserInterface.Menu)
                                        .where()
                                        .attribute(CIAdminUserInterface.Menu.Name).eq(parentName)
                                        .select().oid()
                                        .evaluate();
                        if (parentEval.next()) {
                            final var childEval = EQL.builder()
                                            .print().query(CIAdmin.Abstract)
                                            .where()
                                            .attribute(CIAdmin.Abstract.UUID).eq(disconnectedChild)
                                            .select().oid()
                                            .evaluate();
                            if (childEval.next()) {
                                EQL.builder().insert(CIAdminUserInterface.Menu2Command)
                                                .set(CIAdminUserInterface.Menu2Command.FromMenu, parentEval.inst())
                                                .set(CIAdminUserInterface.Menu2Command.ToCommand, childEval.inst())
                                                .execute();
                                LOG.info("Connected successfull");
                            }
                        }
                    }
                }

            }
        }
    }

    protected List<InstallFile> getInstallFilesForKeys(final Map<String, URL> files,
                                                       final Collection<String> keys)
        throws StreamReadException, DatabindException, IOException, URISyntaxException
    {
        final List<InstallFile> ret = new ArrayList<>();
        final List<RevItem> revItems = getRevItemList(files);
        int idx = 1;
        for (final var revItem : revItems) {
            if (keys.contains(revItem.getIdentifier())) {
                LOG.info("Getting install file for Item {} / {}: {}", idx, keys.size(), revItem.getIdentifier());
                final InstallFile installFile = new InstallFile()
                                .setName(revItem.getName4InstallFile())
                                .setURL(revItem.getURL(files))
                                .setType(revItem.getFileType().getType())
                                .setRevision(revItem.getRevision())
                                .setDate(revItem.getDate());
                ret.add(installFile);
                idx++;
            }
        }
        Collections.sort(ret, Comparator.comparing(InstallFile::getName));
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
                                && Seconds.secondsBetween(revDate, item.getDate()).getSeconds() > 10) {
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
