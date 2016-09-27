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

import java.util.HashMap;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.DeleteRelatedLog;
import org.efaps.esjp.common.history.xml.IRelatedLog;
import org.efaps.esjp.common.history.xml.RelatedInstObj;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("f0cc5c71-5cb1-471d-b392-7c1bb848af99")
@EFapsApplication("eFaps-Kernel")
public abstract class DeleteRelatedHistoryTrigger_Base
    extends AbstractRelatedUpdateHistoryTrigger
{
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

        if (_parameter.get(ParameterValues.NEW_VALUES) == null) {
            _parameter.put(ParameterValues.NEW_VALUES, new HashMap<>());
        }
        ((IRelatedLog) _log).getRelatedInstObj().getAttributes().addAll(getAttributes(_parameter, relInst));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getHistoryType()
    {
        return CICommon.HistoryRelatedDelete.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractHistoryLog getLogObject(final Parameter _parameter)
    {
        return new DeleteRelatedLog();
    }
}
