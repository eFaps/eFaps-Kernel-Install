/*
 * Copyright 2003 - 2017 The eFaps Team
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.rest.Update;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CIItem.
 *
 * @author The eFaps Team
 */
@EFapsUUID("252d6996-ffd7-4528-b2e8-a2f272524e00")
@EFapsApplication("eFaps-Kernel")
public class CIItem
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CIItem.class);

    /**
     * Update from file.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     * @throws InstallationException the installation exception
     * @throws IOException
     */
    public Return updateFromFile(final Parameter _parameter)
        throws EFapsException, InstallationException, IOException
    {

        final Context context = Context.getThreadContext();
        final Context.FileParameter fileItem = context.getFileParameters().get("ciitem");

        CIItem.LOG.info("===Start of Update via REST===");
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

        final List<InstallFile> installFiles = new ArrayList<>();

        final InputStream in = fileItem.getInputStream();

        final File file = new File(dateFolder, fileItem.getName());

        final FileOutputStream out = new FileOutputStream(file);
        final byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();

        final String ending = file.getName().substring(file.getName().lastIndexOf(".") + 1);

        final FileType filetype = FileType.getFileTypeByExtension(ending);

        CIItem.LOG.info("= Receieved: '{}'", file.getName());

        if (filetype != null) {
            final InstallFile installFile = new InstallFile().setName(file.getName()).setURL(file.toURI().toURL())
                            .setType(filetype.getType());

            installFile.setDate(new DateTime());
            installFiles.add(installFile);
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
                CIItem.LOG.info("...Adding to Update: '{}' ", installFile.getName());
                install.addFile(installFile);
            }
            install.updateLatest(null);
        }

        return new Return();
    }
}