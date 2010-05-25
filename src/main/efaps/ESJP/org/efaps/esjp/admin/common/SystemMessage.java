/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.esjp.admin.common;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("6e4c3e39-5a28-4c5e-9485-f4a0028827db")
@EFapsRevision("$Rev$")
public class SystemMessage
    implements EventExecution
{
    /**
     * Method for create a new connection of user's.
     * @param _parameter Parameter as passed from the eFaps API.
     * @return new empty Return.
     * @throws EFapsException on error.
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final String[] childOids = (String[]) others.get("selectedRow");
        if (childOids != null) {
            final String type = (String) properties.get("ConnectType");
            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");

            for (final String childOid : childOids) {
                final Instance child = Instance.get(childOid);
                final Insert insert = new Insert(type);
                insert.add(parentAttr, "" + parent.getId());
                insert.add(childAttr, "" + child.getId());
                insert.add("Status", Status.find("Admin_Common_SystemMessageStatus", "Unread").getId());
                insert.execute();
            }
        }
        return new Return();
    }

    /**
     * Method for show alerts message of any user in case existing.
     * @param _parameter Parameter as passed from the eFaps API.
     * @return ret Return.
     * @throws EFapsException on error.
     */
    public Return showAlertMessage(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final String types = (String) properties.get("Types");

        final QueryBuilder queryBldr = new QueryBuilder(Type.get(types));
        queryBldr.addWhereAttrEqValue("UserLink", Context.getThreadContext().getPerson().getId());
        queryBldr.addWhereAttrNotEqValue("Status",
                        Status.find("Admin_Common_SystemMessageStatus", "Canceled").getId());
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        ret.put(ReturnValues.VALUES, query.getInstances());

        return ret;
    }

    /**
     * Set the status.
     * @param _parameter Parameter as passed from the eFaps API.
     * @return ret Return.
     * @throws EFapsException on error.
     */
    public Return setStatus(final Parameter _parameter)
        throws EFapsException
    {
        final String[] allOids = (String[]) _parameter.get(ParameterValues.OTHERS);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        for (final String rowOid : allOids) {
            final Update update = new Update(rowOid);
            update.add("Status",
                            Status.find("Admin_Common_SystemMessageStatus", (String) properties.get("Status")).getId());
            update.execute();
        }
        return new Return();
    }
}
