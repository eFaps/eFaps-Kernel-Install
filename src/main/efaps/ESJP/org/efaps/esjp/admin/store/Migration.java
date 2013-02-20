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


package org.efaps.esjp.admin.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.db.Checkin;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Migration
{
    /**
     * Migrate teh files from a vfs repository to jcr repository;
     * <ol>
     *  <li>Change the repository definition for the type to be migrated</li>
     *  <li>Get the files from the file system.</li>
     * </ol>
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Return
     * @throws EFapsException
     */
    public Return vfs2jcs(final Parameter _parameter)
        throws EFapsException
    {
        final String baseDirPath = "/efaps/store/archives";
        final File baseDir = new File(baseDirPath);
        final IOFileFilter fileFilter = new RegexFileFilter("[0-9]*\\.[0-9]*");

        final Collection<File> files = FileUtils.listFiles(baseDir, fileFilter, TrueFileFilter.INSTANCE);
        for (final File file : files) {
            final Instance inst = Instance.get(file.getName());
            try {
                System.out.println(file);
                //GeneralStoreVFS
                final QueryBuilder queryBldr= new QueryBuilder(UUID.fromString("cd21d9e9-9009-46bf-9ed7-26bedc623149"));
                queryBldr.addWhereAttrEqValue("InstanceID", inst.getId());
                queryBldr.addWhereAttrEqValue("InstanceTypeID", inst.getType().getId());
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute("FileLength", "FileName");
                multi.execute();
                multi.next();
                final String fileName = multi.<String>getAttribute("FileName");
                final Long fileLength = multi.<Long>getAttribute("FileLength");
                final FileInputStream finput = new FileInputStream(file);
                final InputStream input = new GZIPInputStream(finput);
                final Checkin checkin = new Checkin(inst);
                checkin.execute(fileName, input, fileLength.intValue());
            } catch (final FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return new Return();
    }
}
