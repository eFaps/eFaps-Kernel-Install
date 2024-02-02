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
package org.efaps.esjp.admin.common;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: IReloadCacheListener.java 12201 2014-03-04 21:03:29Z
 *          jan@moxter.net $
 */
@EFapsUUID("fc1fa2c3-d5c4-4e85-8486-173e808da5f6")
@EFapsApplication("eFaps-Kernel")
public interface IReloadCacheListener
    extends IEsjpListener
{

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    void onReloadSystemConfig(final Parameter _parameter)
        throws EFapsException;

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    void onReloadCache(final Parameter _parameter)
        throws EFapsException;
}
