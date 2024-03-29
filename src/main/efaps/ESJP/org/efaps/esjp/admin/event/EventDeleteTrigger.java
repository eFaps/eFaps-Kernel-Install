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
package org.efaps.esjp.admin.event;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * Class provides trigger functionalities for deletion or events.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("fb9bddcb-0f39-4b55-88aa-891b91a2ee5b")
@EFapsApplication("eFaps-Kernel")
public class EventDeleteTrigger
    implements EventExecution
{

    /**
     * Method is executed from the DELETE_PRE trigger of all event types. It
     * removes all subelements of the event. like Properties.
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
        return new Return();
    }
}
