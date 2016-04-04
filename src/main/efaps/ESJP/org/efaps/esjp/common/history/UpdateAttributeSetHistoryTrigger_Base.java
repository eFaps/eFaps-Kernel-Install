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

import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.UpdateAttributeSetLog;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("47c27fed-5c1f-49e6-a04d-799b4f4cd4bf")
@EFapsApplication("eFaps-Kernel")
public abstract class UpdateAttributeSetHistoryTrigger_Base
    extends AbstractAttributeSetHistoryTrigger
{

    /**
     * @param _parameter Parameter as passed by the eFaps API @return new
     * Return @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        Return ret = new Return();
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        if (!values.isEmpty()) {
            ret = super.execute(_parameter);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getHistoryType()
    {
        return CICommon.HistoryAttributeSetUpdate.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractHistoryLog getLogObject(final Parameter _parameter)
    {
        return new UpdateAttributeSetLog();
    }
}
