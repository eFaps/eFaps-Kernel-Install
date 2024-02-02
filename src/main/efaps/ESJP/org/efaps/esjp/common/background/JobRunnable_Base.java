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
package org.efaps.esjp.common.background;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Company;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJob;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The Class JobRunnable_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("c7dbd702-ce65-4037-85c6-2fe1062b75ee")
@EFapsApplication("eFaps-Kernel")
public abstract class JobRunnable_Base
    implements Runnable
{
    /** The job. */
    private final IJob job;

    /** The bridge. */
    private final IExecutionBridge bridge;

    /**
     * Instantiates a new job runnable_ base.
     *
     * @param _job the job
     * @param _bridge the bridge
     */
    public JobRunnable_Base(final IJob _job,
                            final IExecutionBridge _bridge)
    {
        this.job = _job;
        this.bridge = _bridge;
    }

    @Override
    public void run()
    {
        try {
            final Context context = Context.begin(this.bridge.getJobContext().getUserName(),
                            Context.Inheritance.Standalone);
            context.setCompany(Company.get(this.bridge.getJobContext().getCompanyUUID()));
            this.job.execute(this.bridge);
            Context.commit(true);
        }  catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
