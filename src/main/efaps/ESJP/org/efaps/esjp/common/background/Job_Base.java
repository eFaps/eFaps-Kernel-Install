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

import java.util.UUID;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * The Class Job_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("fa782a2b-2b99-4528-98e2-cb22085e4a01")
@EFapsApplication("eFaps-Kernel")
public abstract class Job_Base
    extends AbstractCommon
{
    /**
     *
     * @param _parameter Parameter as passed from  the eFaps API.
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return accessCheck4Single(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final String typeStr = getProperty(_parameter, "Type");
        Type type;
        if (isUUID(typeStr)) {
            type = Type.get(UUID.fromString(typeStr));
        } else {
            type = Type.get(typeStr);
        }
        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));

        final QueryBuilder queryBldr = new QueryBuilder(type);
        queryBldr.addWhereAttrEqValue(CICommon.BackgroundJobAbstract.StatusAbstract,
                        Status.find(CICommon.BackgroundJobStatus.Active),
                        Status.find(CICommon.BackgroundJobStatus.Scheduled));
        final Boolean access = queryBldr.getQuery().executeWithoutAccessCheck().isEmpty();

        if (access == null && inverse || access && !inverse) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }
}
