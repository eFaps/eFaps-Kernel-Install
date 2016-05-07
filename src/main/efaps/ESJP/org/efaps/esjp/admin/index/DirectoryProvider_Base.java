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
        final Directory ret;
        try {
            ret = FSDirectory.open(new File("/eFaps/index").toPath());
        } catch (final IOException e) {
            throw new EFapsException(DirectoryProvider.class, "");
        }
        return ret;
    }
}
