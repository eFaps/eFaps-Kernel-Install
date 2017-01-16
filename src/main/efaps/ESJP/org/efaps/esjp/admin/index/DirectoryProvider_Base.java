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
package org.efaps.esjp.admin.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.efaps.admin.index.IDirectoryProvider;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;

/**
 * The Class AnalyzerProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("5695e5bf-5fb2-4770-9f6a-5ee82b3c728c")
@EFapsApplication("eFaps-Kernel")
public abstract class DirectoryProvider_Base
    implements IDirectoryProvider
{

    /** The Constant INDEXPATH. */
    protected static final String INDEXPATH = "index";

    /** The Constant TAXONOMYPATH. */
    protected static final String TAXONOMYPATH = "taxonomy";

    @Override
    public Directory getDirectory()
        throws EFapsException
    {
        return getDirectory(Context.getThreadContext().getCompany().getId(), Context.getThreadContext().getLanguage(),
                        DirectoryProvider.INDEXPATH);
    }

    @Override
    public Directory getTaxonomyDirectory()
        throws EFapsException
    {
        return getDirectory(Context.getThreadContext().getCompany().getId(), Context.getThreadContext().getLanguage(),
                        DirectoryProvider.TAXONOMYPATH);
    }

    /**
     * Gets the directory.
     *
     * @param _companyId the _company id
     * @param _language the _language
     * @param _path the _path
     * @return the directory
     * @throws EFapsException the e faps exception
     */
    public Directory getDirectory(final Long _companyId,
                                  final String _language,
                                  final String _path)
        throws EFapsException
    {
        final Directory ret;
        try {

            final String basefolder = KernelConfigurations.INDEXBASEFOLDER.get();
            final File base = new File(basefolder, String.valueOf(_companyId));
            final File folder1 = new File(base, _language);
            final File folder2 = new File(folder1, _path);
            if (!folder2.exists()) {
                folder2.mkdirs();
            }
            ret = FSDirectory.open(folder2.toPath());
        } catch (final IOException e) {
            throw new EFapsException(DirectoryProvider.class, "IOException", e);
        }
        return ret;
    }
}
