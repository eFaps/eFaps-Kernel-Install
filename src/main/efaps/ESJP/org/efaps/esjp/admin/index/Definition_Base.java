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

import org.efaps.admin.event.Parameter;
import org.efaps.admin.index.IndexDefinition;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.admin.common.IReloadCacheListener;
import org.efaps.util.EFapsException;

/**
 * The Class Definition_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("f47f76c0-1c7b-4cd0-8dc6-74261520e4ca")
@EFapsApplication("eFaps-Kernel")
public abstract class Definition_Base
    implements IReloadCacheListener
{

    @Override
    public void onReloadSystemConfig(final Parameter _parameter)
        throws EFapsException
    {
    }

    @Override
    public void onReloadCache(final Parameter _parameter)
        throws EFapsException
    {
        IndexDefinition.initialize();
    }

    @Override
    public int getWeight()
    {
        return 0;
    }
}
