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
package org.efaps.esjp.admin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.runlevel.IRunLevelListener;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RunLevelListener_Base.
 */
@EFapsUUID("999443c8-8f9f-47be-aad1-974a1b09421b")
@EFapsApplication("eFaps-Kernel")
public abstract class RunLevelListener_Base
    implements IRunLevelListener
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RunLevelListener.class);

    /** The firsttime. */
    private static boolean FIRSTTIME = true;

    @Override
    public void onExecute(final String _levelName)
        throws EFapsException
    {
        if (FIRSTTIME && "webapp".equals(_levelName) && KernelConfigurations.ACTIVATEMOSKITO.get()) {
            try {
                final String clazzName = "org.moskito.controlagent.endpoints.rmi.RMIEndpoint";
                final Class<?> clazz = Class.forName(clazzName);
                final Method method = clazz.getMethod("startRMIEndpoint");
                method.invoke(null);
            } catch (final ClassNotFoundException e) {
                LOG.info("Did not find init class for RMI Endpoint used by Mosikito");
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Problems on init of Moskito", e);
            }
        }
        FIRSTTIME = false;
    }

    @Override
    public int getWeight()
    {
        return 0;
    }
}
