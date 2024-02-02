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

import java.io.Serializable;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJobContext;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;

/**
 * The Class ExecutionBridge_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("82ce419a-6645-401f-8a86-feacdb70d547")
@EFapsApplication("eFaps-Kernel")
public abstract class ExecutionBridge_Base
    implements IExecutionBridge
{

    /** */
    private static final long serialVersionUID = 1L;

    /** The job context. */
    private IJobContext jobContext;

    /** The target. */
    private int target = 100;

    /** The progress. */
    private int progress = 0;

    /** The instance. */
    private Instance instance;

    /** The job name. */
    private String jobName;


    @Override
    public String getJobName()
    {
        return this.jobName;
    }

    /**
     * Sets the job name.
     *
     * @param _jobName the new job name
     */
    public void setJobName(final String _jobName)
    {
        this.jobName = _jobName;
    }

    @Override
    public boolean isStop()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCancel()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getProgress()
    {
        return this.progress;
    }

    /**
     * Sets the progress.
     *
     * @param _progress the new progress
     * @return the execution bridge
     */
    public ExecutionBridge setProgress(final int _progress)
    {
        this.progress = _progress;
        return (ExecutionBridge) this;
    }

    @Override
    public boolean isFinished()
    {
        return getProgress() ==  getTarget();
    }

    @Override
    public IJobContext getJobContext()
    {
        return this.jobContext;
    }

    /**
     * Sets the job context.
     *
     * @param _jobContext the job context
     * @return the execution bridge
     */
    public ExecutionBridge setJobContext(final IJobContext _jobContext)
    {
        this.jobContext = _jobContext;
        return (ExecutionBridge) this;
    }

    @Override
    public Serializable getContent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public int getTarget()
    {
        return this.target;
    }

    /**
     * Sets the target.
     *
     * @param _target the new target
     * @return the execution bridge
     */
    public ExecutionBridge setTarget(final int _target)
    {
        this.target = _target;
        return (ExecutionBridge) this;
    }

    /**
     * Register progress.
     *
     * @throws EFapsException on error
     */
    public void registerProgress()
        throws EFapsException
    {
        setProgress(getProgress() + 1);
        final Update update = new Update(getInstance());
        update.add(CICommon.BackgroundJobAbstract.StatusAbstract, Status.find(CICommon.BackgroundJobStatus.Active));
        if (getJobName() != null) {
            update.add(CICommon.BackgroundJobAbstract.Title, getJobName());
        }
        update.add(CICommon.BackgroundJobAbstract.Target, getTarget());
        update.add(CICommon.BackgroundJobAbstract.Progress, getProgress());
        update.execute();
        Context.save();
    }

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Sets the instance.
     *
     * @param _instance the instance
     * @return the execution bridge
     */
    public ExecutionBridge setInstance(final Instance _instance)
    {
        this.instance = _instance;
        return (ExecutionBridge) this;
    }

    /**
     * Close.
     *
     * @throws EFapsException on error
     */
    public void close()
        throws EFapsException
    {
        final Update update = new Update(getInstance());
        update.add(CICommon.BackgroundJobAbstract.StatusAbstract, Status.find(CICommon.BackgroundJobStatus.Finished));
        if (getJobName() != null) {
            update.add(CICommon.BackgroundJobAbstract.Title, getJobName());
        }
        update.add(CICommon.BackgroundJobAbstract.Target, getTarget());
        update.add(CICommon.BackgroundJobAbstract.Progress, getProgress());
        update.execute();
    }
}
