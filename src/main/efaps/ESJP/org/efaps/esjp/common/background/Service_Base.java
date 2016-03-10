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
package org.efaps.esjp.common.background;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.efaps.admin.common.NumberGenerator;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJob;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;

/**
 * The Class Service_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("dd9f1e3e-f86c-4cba-8ca2-3b868b6ce09d")
@EFapsApplication("eFaps-Kernel")
public abstract class Service_Base
{

    /** The service. */
    private static Service_Base SERVICE;

    /** The executor service. */
    private final ExecutorService executorService;

    /**
     * Instantiates a new service_ base.
     */
    protected Service_Base()
    {
        final int corePoolSize = 2;
        final int maxdPoolSize = 10;

        this.executorService = new ThreadPoolExecutor(corePoolSize, maxdPoolSize, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(),
                        new BasicThreadFactory.Builder().namingPattern("eFaps-BackgroundProcess-%s").build());
    }

    /**
     * Launch a job. (Saves the context to ensure the job
     * instance will be accessible from other processes.)
     *
     * @param _type the type
     * @param _job the _job
     * @return the execution bridge
     * @throws EFapsException on error
     */
    public IExecutionBridge launch(final Type _type,
                                   final IJob _job)
        throws EFapsException
    {
        final Insert insert = new Insert(_type);
        insert.add(CICommon.BackgroundJobAbstract.StatusAbstract, Status.find(CICommon.BackgroundJobStatus.Scheduled));
        insert.add(CICommon.BackgroundJobAbstract.Name,
                        NumberGenerator.get(UUID.fromString("06617556-f557-4463-bdad-3e9259f4bacc")).getNextVal());
        insert.execute();
        Context.save();
        final ExecutionBridge bridge = new ExecutionBridge();
        bridge.setInstance(insert.getInstance())
            .setJobContext(new JobContext()
                        .setUserName(Context.getThreadContext().getPerson().getName())
                        .setLocale(Context.getThreadContext().getLocale())
                        .setCompanyUUID(Context.getThreadContext().getCompany().getUUID()));
        this.executorService.execute(new JobRunnable(_job, bridge));
        return bridge;
    }

    /**
     * Gets the.
     *
     * @return the service_ base
     */
    protected static Service get()
    {
        if (Service_Base.SERVICE == null) {
            SERVICE = new Service();
        }
        return (Service) SERVICE;
    }

}
