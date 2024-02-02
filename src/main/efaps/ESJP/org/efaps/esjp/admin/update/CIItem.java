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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Context.FileParameter;
import org.efaps.rest.Update;
import org.efaps.update.FileType;
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
    extends AbstractUpdate
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Return updateFromFile(final Parameter _parameter)
        throws EFapsException, InstallationException, IOException
    {
        CIItem.LOG.info("===Start of Update via UserInterface===");
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

        final Map<String, FileParameter> fileParameters = Context.getThreadContext().getFileParameters();

        final List<FileParameter> fileItems = new ArrayList<>();
        if (fileParameters.containsKey("ciitem")) {
            fileItems.add(fileParameters.get("ciitem"));
        }
        int i = 0;
        while (fileParameters.containsKey("ciitem_" + i)) {
            fileItems.add(fileParameters.get("ciitem_" + i));
            i++;
        }

        final List<InstallFile> installFiles = new ArrayList<>();
        for (final FileParameter fileItem : fileItems) {
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
        }
        install(installFiles);
        return new Return();
    }
}
