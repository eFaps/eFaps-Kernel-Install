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
package org.efaps.esjp.common.history.status;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

@EFapsUUID("1d135493-bc9e-4cb9-93b2-87860993281f")
@EFapsApplication("eFaps-Kernel")
public abstract class StatusHistory_Base
{

    public Return getFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance objectInst = InstanceUtils.isValid(_parameter.getInstance()) ? _parameter.getInstance()
                        : _parameter.getCallInstance();

        final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminCommon.GeneralInstance);
        attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.InstanceTypeID, objectInst.getType().getId());
        attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.InstanceID, objectInst.getId());

        final QueryBuilder queryBldr = new QueryBuilder(CICommon.HistoryStatus);
        queryBldr.addWhereAttrInQuery(CICommon.HistoryStatus.GeneralInstanceLink,
                        attrQueryBldr.getAttributeQuery(CIAdminCommon.GeneralInstance.ID));
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CICommon.HistoryStatus.Created, CICommon.HistoryStatus.Creator,
                        CICommon.HistoryStatus.StatusLink);
        multi.executeWithoutAccessCheck();
        final StringBuilder html = new StringBuilder()
                        .append("<ul style=\"list-style: disclosure-closed;")
                        .append("display: flex;flex-direction: row;flex-wrap: wrap;\">");
        while (multi.next()) {
            final DateTime created = multi.getAttribute(CICommon.HistoryStatus.Created);
            final Status status = Status.get(multi.<Long>getAttribute(CICommon.HistoryStatus.StatusLink));
            final Person creator = multi.getAttribute(CICommon.HistoryStatus.Creator);
            html.append("<li style=\"border: 1px solid;border-radius: 20px;padding: 5px;margin: 0 0 10px 20px;\">")
                .append("<span style=\"font-weight: bold;padding: 5px;\">").append(status.getLabel()).append("</span>")
                .append("<span style=\"padding: 5px;\">")
                    .append(creator.getFirstName()).append(creator.getLastName()).append("</span>")
                .append("<span style=\"padding: 5px;\">").append(created).append("</span>")
                .append("</li>");
        }
        html.append("</ul>");
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

}
