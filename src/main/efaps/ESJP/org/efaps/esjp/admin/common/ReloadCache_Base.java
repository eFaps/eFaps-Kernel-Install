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

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to reload the Cache.<br>
 * This Class is a Java eFaps Program which is stored inside the eFaps-Database.
 * It is executed on user interaction through a trigger on a Command.
 *
 * @author The eFaps Team
 * @version $Id:ReloadCache.java 1563 2007-10-28 14:07:41Z tmo $
 *
 */
@EFapsUUID("1d4f1263-9315-4f59-bd5e-bd364f907bac")
@EFapsApplication("eFaps-Kernel")
public abstract class ReloadCache_Base
    implements EventExecution
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReloadCache.class);

    /**
     * Reload the whole Cache for eFaps.
     *
     * @param _parameter Parameter as pased from the eFaps API
     * @throws EFapsException on error
     * @return new empty Return
     *
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        ReloadCache_Base.LOG.info("reload Cache by: {}", Context.getThreadContext().getPerson().getName());
        RunLevel.init("webapp");
        RunLevel.execute();
        for (final IReloadCacheListener listener : Listener.get().<IReloadCacheListener>invoke(
                        IReloadCacheListener.class)) {
            listener.onReloadCache(_parameter);
        }
        ReloadCache_Base.LOG.info("reload Cache finished successfully");
        return new Return();
    }

    /**
     * Relaod the SystemConfigurations.
     *
     * @param _parameter Parameter as pased from the eFaps API
     * @throws EFapsException on error
     * @return new empty Return
     *
     */
    public Return reloadSystemConfigurations(final Parameter _parameter)
        throws EFapsException
    {
        ReloadCache_Base.LOG.info("reload SystemConfigurations by: {}",
                        Context.getThreadContext().getPerson().getName());
        SystemConfiguration.initialize();
        for (final IReloadCacheListener listener : Listener.get().<IReloadCacheListener>invoke(
                        IReloadCacheListener.class)) {
            listener.onReloadSystemConfig(_parameter);
        }
        ReloadCache_Base.LOG.info("reload SystemConfigurations finished successfully");
        return new Return();
    }

    public Return reloadUI(final Parameter _parameter)
        throws EFapsException
    {
        ReloadCache_Base.LOG.info("reload UI by: {}", Context.getThreadContext().getPerson().getName());
        AbstractUserInterfaceObject.initialize();

        ReloadCache_Base.LOG.info("reload UI finished successfully");
        return new Return();
    }
}
