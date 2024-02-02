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

import java.util.UUID;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("1e4b28da-1ac8-453a-aaa2-2cfa2b45e180")
@EFapsApplication("eFaps-Kernel")
public class LazyLoader
    implements Job
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LazyLoader.class);

    @Override
    public void execute(final JobExecutionContext context)
        throws JobExecutionException
    {
        try {
            final var elements = KernelConfigurations.LAZYLOAD.get();
            for (final String element : elements) {
                if (UUIDUtil.isUUID(element)) {
                    final var uuid = UUID.fromString(element);
                    AbstractCommand cmd = Command.get(uuid);
                    if (cmd == null) {
                        cmd = Menu.get(uuid);
                        if (cmd == null) {
                            cmd = Search.get(uuid);
                        }
                    }
                } else {
                    AbstractCommand cmd = Command.get(element);
                    if (cmd == null) {
                        cmd = Menu.get(element);
                        if (cmd == null) {
                            cmd = Search.get(element);
                        }
                    }
                }
            }
        } catch (final EFapsException e) {
            LOG.error("Catched exception", e);
        }
    }
}
