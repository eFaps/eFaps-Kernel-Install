/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.esjp.bpm.task;

import java.util.Map;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StatusTask
    extends StatusTask_Base
{

    public Return setStatus(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        @SuppressWarnings("unchecked")
        final Map<String, Object> parameters = (Map<String, Object>) _parameter.get(ParameterValues.BPM_VALUES);
        final Instance inst = Instance.get((String) parameters.get("OID"));
        final String statusStr = (String) parameters.get("Status");

        if (inst.isValid()) {
            final Status status = Status.find(inst.getType().getStatusAttribute().getLink().getUUID(), statusStr);
            if (status != null) {
                final Update update = new Update(inst);
                update.add(inst.getType().getStatusAttribute(), status.getId());
                update.execute();
            }
        }
        return ret;
    }
}
