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

import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AttributeSetInstObj;
import org.efaps.esjp.common.history.xml.IAttributeSetLog;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("004089a2-205d-450a-8dcb-05d7cd8ad8e2")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractAttributeSetHistoryTrigger_Base
    extends AbstractUpdateHistoryTrigger
{
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
        final Instance attrSetInst = _parameter.getInstance();
        final AttributeSetInstObj instObj = new AttributeSetInstObj();
        instObj.setOid(attrSetInst.getOid());
        instObj.setTypeUUID(attrSetInst.getType().getUUID());

        ((IAttributeSetLog) _log).setAttributSetInstObj(instObj);
        ((IAttributeSetLog) _log).getAttributSetInstObj().getAttributes()
                        .addAll(getAttributes(_parameter, attrSetInst));
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
            final AttributeSet attrSet = (AttributeSet) _parameter.getInstance().getType();
            final SelectBuilder selParent = SelectBuilder.get().linkto(attrSet.getAttributeName()).instance();
            final PrintQuery print = new PrintQuery(_parameter.getInstance());
            print.addSelect(selParent);
            print.executeWithoutAccessCheck();
            this.historyInstance = print.<Instance>getSelect(selParent);
            if (this.historyInstance.getType() instanceof Classification) {
                final String linkAttr = ((Classification) this.historyInstance.getType()).getLinkAttributeName();
                final PrintQuery print2 = new PrintQuery(this.historyInstance);
                final SelectBuilder selInst = SelectBuilder.get().linkto(linkAttr).instance();
                print2.addSelect(selInst);
                print2.executeWithoutAccessCheck();
                this.historyInstance = print2.getSelect(selInst);
            }
        }
        return this.historyInstance;
    }
}
