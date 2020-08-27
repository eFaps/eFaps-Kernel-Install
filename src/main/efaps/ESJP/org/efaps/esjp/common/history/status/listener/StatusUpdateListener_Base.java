/*
 * Copyright 2003 - 2020 The eFaps Team
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
package org.efaps.esjp.common.history.status.listener;

import org.efaps.admin.datamodel.attributetype.IStatusChangeListener;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.eql2.StmtFlag;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("b6ce7392-4771-4ba1-8859-cc299a0051e2")
@EFapsApplication("eFaps-Kernel")
public abstract class StatusUpdateListener_Base
    implements IStatusChangeListener
{

    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdateListener.class);

    @Override
    public int getWeight()
    {
        return 0;
    }

    @Override
    public void onInsert(final Instance _instance, final Long _statusId)
        throws EFapsException
    {
        registerUpdate(_instance, _statusId);
    }

    @Override
    public void onUpdate(final Instance _instance, final Long _statusId)
        throws EFapsException
    {
        registerUpdate(_instance, _statusId);
    }

    protected void registerUpdate(final Instance _instance, final Long _statusId)
        throws EFapsException
    {
        if (_instance.getType().isGeneralInstance()) {
            final Insert insert = new Insert(CICommon.HistoryStatus);
            insert.add(CICommon.HistoryStatus.GeneralInstanceLink, _instance.getGeneralId());
            insert.add(CICommon.HistoryStatus.StatusLink, _statusId);
            insert.executeWithoutTrigger();
        } else {
            LOG.warn("Could not register Status update due to not being a General Instance: {}, StatusID: {}",
                            _instance, _statusId);
        }
    }

    @Override
    public void onDelete(final Instance _instance)
        throws EFapsException
    {
        if (_instance.getType().isGeneralInstance()) {
            final var eval = EQL.builder()
                            .with(StmtFlag.TRIGGEROFF)
                            .print()
                            .query(CICommon.HistoryStatus)
                            .where()
                            .attribute(CICommon.HistoryStatus.GeneralInstanceLink).eq(_instance.getGeneralId())
                            .select().instance()
                            .evaluate();
            while (eval.next()) {
                new Delete((Instance) eval.get(1)).executeWithoutTrigger();
            }
        }
    }
}
