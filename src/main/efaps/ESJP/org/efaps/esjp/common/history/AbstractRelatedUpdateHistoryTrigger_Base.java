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

package org.efaps.esjp.common.history;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.IRelatedLog;
import org.efaps.esjp.common.history.xml.RelatedInstObj;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("654f3855-8843-4c86-af8f-6f8e0a675c19")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractRelatedUpdateHistoryTrigger_Base
    extends AbstractUpdateHistoryTrigger
{

    /**
     * Select Statement to the connected instance.
     */
    public static final String HISEL = "Select4HistoryInstance";

    /** The history instance. */
    private Instance historyInstance;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void add2LogObject(final Parameter _parameter,
                                 final AbstractHistoryLog _log)
        throws EFapsException
    {
        final Instance relInst = _parameter.getInstance();
        final RelatedInstObj instObj = new RelatedInstObj();
        instObj.setOid(relInst.getOid());
        instObj.setTypeUUID(relInst.getType().getUUID());

        ((IRelatedLog) _log).setRelatedInstObj(instObj);

        ((IRelatedLog) _log).getRelatedInstObj().getAttributes().addAll(getAttributes(_parameter, relInst));
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return instance that belongs to the HistoryTrigger
     * @throws EFapsException on error
     */
    @Override
    protected Instance getHistoryInstance(final Parameter _parameter)
        throws EFapsException
    {
        if (this.historyInstance == null) {
            final String selHistoryInst = getProperty(_parameter, AbstractRelatedUpdateHistoryTrigger_Base.HISEL);
            final PrintQuery print = new PrintQuery(_parameter.getInstance());
            print.addSelect(selHistoryInst);
            print.executeWithoutAccessCheck();
            this.historyInstance = print.<Instance>getSelect(selHistoryInst);
        }
        return this.historyInstance;
    }
}
