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
import org.efaps.db.Context;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;

/**
 * The Class AnalyzerProvider_Base.
 *
 * @author The eFaps Team
 */
public abstract class DirectoryProvider_Base
    implements IDirectoryProvider
{

    @Override
    public Directory getDirectory()
        throws EFapsException
    {
        return getDirectory(Context.getThreadContext().getCompany().getId(), Context.getThreadContext().getLanguage());
    }

    /**
     * Gets the directory.
     *
     * @param _companyId the _company id
     * @param _language the _language
     * @return the directory
     * @throws EFapsException the e faps exception
     */
    public Directory getDirectory(final Long _companyId,
                                  final String _language)
        throws EFapsException
    {
        final Directory ret;
        try {

            final String basefolder = KernelConfigurations.INDEXBASEFOLDER.get();
            final File base = new File(basefolder, String.valueOf(_companyId));
            final File folder = new File(base, _language);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            ret = FSDirectory.open(folder.toPath());
        } catch (final IOException e) {
            throw new EFapsException(DirectoryProvider.class, "");
        }
        return ret;
    }
}
