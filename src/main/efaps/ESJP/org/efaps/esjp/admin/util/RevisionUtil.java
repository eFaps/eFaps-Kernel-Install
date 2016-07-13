/*
 * Copyright 2003 - 2016 The eFaps Team
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
package org.efaps.esjp.admin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.rest.Update;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * The Class RevisionUtil.
 *
 * @author The eFaps Team
 */
@EFapsUUID("ae1aac5a-5c81-44f3-9e8d-ca66f71fab1f")
@EFapsApplication("eFaps-Kernel")
public class RevisionUtil
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RevisionUtil.class);

    /**
     * Check revisions.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     * @throws InstallationException on error
     */
    public Return checkRevisions(final Parameter _parameter)
        throws EFapsException, InstallationException
    {

        try (final FileInputStream in = new FileInputStream(new File(
                        "/home/janmoxter/git/FonPeru/install/target/CIItems.tar"));
                        final TarArchiveInputStream tarInput = new TarArchiveInputStream(in);) {

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

            final Map<String, URL> files = new HashMap<>();
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            while (currentEntry != null) {
                final byte[] bytess = new byte[(int) currentEntry.getSize()];
                tarInput.read(bytess);
                final File file = new File(dateFolder, currentEntry.getName());
                final FileOutputStream output = new FileOutputStream(file);
                output.write(bytess);
                output.close();
                files.put(currentEntry.getName(), file.toURI().toURL());
                currentEntry = tarInput.getNextTarEntry();
            }

            final List<InstallFile> installFiles = new ArrayList<>();

            final URL json = files.get("revisions.json");
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JodaModule());
            final List<RevItem> items = mapper.readValue(new File(json.toURI()), mapper.getTypeFactory()
                            .constructCollectionType(List.class, RevItem.class));
            int i = 1;
            for (final RevItem item : items) {
                LOG.info("Checking Item {} / {}: {}", i, items.size(), item);

                final QueryBuilder queryBldr = new QueryBuilder(CIAdmin.Abstract);
                queryBldr.addWhereAttrEqValue(CIAdmin.Abstract.UUID, item.getUuid());
                final MultiPrintQuery multi = queryBldr.getPrint();
                final SelectBuilder selRevision = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink).attribute(
                                CIAdminCommon.ApplicationRevision.Revision);
                final SelectBuilder selRevDate = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink).attribute(
                                CIAdminCommon.ApplicationRevision.Date);
                final SelectBuilder selApp = SelectBuilder.get().linkto(CIAdmin.Abstract.RevisionLink).linkto(
                                CIAdminCommon.ApplicationRevision.ApplicationLink).attribute(
                                                CIAdminCommon.Application.Name);
                multi.addSelect(selRevision, selApp, selRevDate);
                multi.addAttribute(CIAdmin.Abstract.Name);
                multi.execute();
                boolean update = false;
                String name = "NEWELEMENT: " +  item.getUuid();
                if (multi.next()) {
                    final String revision = multi.getSelect(selRevision);
                    final DateTime revDate = multi.getSelect(selRevDate);
                    final String app = multi.getSelect(selApp);
                    name = multi.getAttribute(CIAdmin.Abstract.Name);
                    LOG.info("Found obj: {}, type: {}, name: {},  app: {}, date: {}, revision: {} ",
                                    multi.getCurrentInstance().getOid(),
                                    multi.getCurrentInstance().getType().getName(),
                                    name, app, revDate, revision);
                    if (!item.getApplication().equals(app)) {
                        LOG.warn("Different Application: {} - {}", item.getApplication(), app);
                        update = true;
                    }
                    if (!item.getRevision().equals(revision)) {
                        LOG.warn("Different Revision: {} - {}", item.getRevision(), revision);
                        update = true;
                    }
                } else {
                    LOG.warn("Could not find item: {} ", item);
                    update = true;
                }
                if (update) {
                    final InstallFile installFile = new InstallFile()
                                    .setName(name)
                                    .setURL(files.get(item.getUuid() + ".xml"))
                                    .setType("install-xml")
                                    .setRevision(item.getRevision())
                                    .setDate(item.getDate());
                    installFiles.add(installFile);
                }
                i++;
            }
            Collections.sort(installFiles, new Comparator<InstallFile>()
            {

                @Override
                public int compare(final InstallFile _installFile0,
                                   final InstallFile _installFile1)
                {
                    return _installFile0.getName().compareTo(_installFile1.getName());
                }
            });

            if (!installFiles.isEmpty()) {
                final Install install = new Install(true);
                for (final InstallFile installFile : installFiles) {
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
        return new Return();
    }

    /**
     * The Class RevItem.
     *
     * @author The eFaps Team
     */
    public static class RevItem
    {

        /** The uuid. */
        private String uuid;

        /** The application. */
        private String application;

        /** The revision. */
        private String revision;

        /** The date. */
        private DateTime date;

        /**
         * Instantiates a new rev item.
         */
        public RevItem()
        {
        }

        /**
         * Getter method for the instance variable {@link #application}.
         *
         * @return value of instance variable {@link #application}
         */
        public String getApplication()
        {
            return this.application;
        }

        /**
         * Getter method for the instance variable {@link #revision}.
         *
         * @return value of instance variable {@link #revision}
         */
        public String getRevision()
        {
            return this.revision;
        }

        /**
         * Getter method for the instance variable {@link #uuid}.
         *
         * @return value of instance variable {@link #uuid}
         */
        public String getUuid()
        {
            return this.uuid;
        }

        /**
         * Sets the uuid.
         *
         * @param _uuid the new uuid
         */
        public void setUuid(final String _uuid)
        {
            this.uuid = _uuid;
        }

        /**
         * Sets the application.
         *
         * @param _application the new application
         */
        public void setApplication(final String _application)
        {
            this.application = _application;
        }

        /**
         * Sets the revision.
         *
         * @param _revision the new revision
         */
        public void setRevision(final String _revision)
        {
            this.revision = _revision;
        }

        /**
         * Gets the date.
         *
         * @return the date
         */
        public DateTime getDate()
        {
            return this.date;
        }

        /**
         * Sets the date.
         *
         * @param _date the new date
         */
        public void setDate(final DateTime _date)
        {
            this.date = _date;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
