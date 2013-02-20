/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.file;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Utility class used to create empty files in a user depended temporarily
 * file-folder architecture.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b3bae05a-8db6-4a89-9f84-37564945049d")
@EFapsRevision("$Rev$")
public abstract class FileUtil_Base
{
    /**
     * Name of the temp folder.
     */
    public static final String TMPFOLDERNAME = "eFapsUserDepTemp";

    /**
     * Method to get a file with given name and ending.
     * Spaces will be replaced by underscores.
     * @param _name     name for the file
     * @param _ending   ending for the file
     * @return file
     * @throws EFapsException on error
     */
    public File getFile(final String _name,
                        final String _ending)
        throws EFapsException
    {
        return getFile(_name.replace(File.separator, "_").replace(" ", "_") + "." + _ending);
    }

    /**
     * Method to get a file with given name and ending.
     * @param _name     name for the file
     * @return file
     * @throws EFapsException on error
     */
    public File getFile(final String _name)
        throws EFapsException
    {
        File ret = null;
        try {
            final File temp = File.createTempFile("eFaps", ".tmp");
            final File tmpfld = temp.getParentFile();
            temp.delete();
            final File storeFolder = new File(tmpfld, FileUtil_Base.TMPFOLDERNAME);
            final NumberFormat formater = NumberFormat.getInstance();
            formater.setMinimumIntegerDigits(8);
            formater.setGroupingUsed(false);
            final File userFolder = new File(storeFolder, formater.format(Context.getThreadContext().getPersonId()));
            if (!userFolder.exists()) {
                userFolder.mkdirs();
            }
            ret = new File(userFolder, _name);
        } catch (final IOException e) {
            throw new EFapsException(FileUtil_Base.class, "IOException", e);
        }
        return ret;
    }
}
