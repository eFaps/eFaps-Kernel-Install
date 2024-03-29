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
package org.efaps.esjp.admin.ui;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminEvent;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * Class provides methods in relation to fields.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("801f350c-3cdf-4a60-b4ee-19b8a63ec867")
@EFapsApplication("eFaps-Kernel")
public class FieldDeleteTrigger
    implements EventExecution
{
    /**
     * Method is executed from the DELETE_PRE trigger of the type
     * Admin_UI_Field, Admin_UI_FieldSet, Admin_UI_FieldHeading,
     * Admin_UI_FieldTable, Admin_UI_FieldGroup. <br>
     * It removes all subelements of the field. like Properties and so on.
     *
     * @param _parameter Parameter as passed by eFaps to an esjp
     * @return new Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();

        // remove properties
        final QueryBuilder propQueryBldr = new QueryBuilder(CIAdminCommon.Property);
        propQueryBldr.addWhereAttrEqValue(CIAdminCommon.Property.Abstract, instance.getId());
        final InstanceQuery propQuery = propQueryBldr.getQuery();
        propQuery.executeWithoutAccessCheck();
        while (propQuery.next()) {
            final Delete del = new Delete(propQuery.getCurrentValue());
            del.execute();
        }

        // remove links
        final QueryBuilder linkQueryBldr = new QueryBuilder(CIAdminUserInterface.Link);
        linkQueryBldr.addWhereAttrEqValue(CIAdminUserInterface.Link.From, instance.getId());
        final InstanceQuery linkQuery = linkQueryBldr.getQuery();
        linkQuery.executeWithoutAccessCheck();
        while (linkQuery.next()) {
            final Delete del = new Delete(linkQuery.getCurrentValue());
            del.execute();
        }

        // remove events
        final QueryBuilder eventQueryBldr = new QueryBuilder(CIAdminEvent.Definition);
        eventQueryBldr.addWhereAttrEqValue(CIAdminEvent.Definition.Abstract, instance.getId());
        final InstanceQuery eventQuery = eventQueryBldr.getQuery();
        eventQuery.executeWithoutAccessCheck();
        while (eventQuery.next()) {
            final Delete del = new Delete(eventQuery.getCurrentValue());
            del.execute();
        }
        return new Return();
    }
}
