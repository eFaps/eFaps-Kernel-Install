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
package org.efaps.esjp.admin.index;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * The Class ReIndexJob_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("083415d2-2521-4ab9-8c06-11949821c2b4")
@EFapsApplication("eFaps-Kernel")
public abstract class UpdateIndexJob_Base
    implements Job
{

    @Override
    public void execute(final JobExecutionContext _context)
        throws JobExecutionException
    {
        final Parameter paramter = new Parameter();
        try {
            new Process().updateIndex(paramter);
        } catch (final EFapsException e) {
            throw new JobExecutionException(e);
        }
    }
}
