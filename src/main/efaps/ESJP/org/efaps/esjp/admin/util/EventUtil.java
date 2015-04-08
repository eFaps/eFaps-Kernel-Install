/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.esjp.admin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d86499fc-828c-459f-9dd2-8006c39e9da3")
@EFapsApplication("eFaps-Kernel")
public class EventUtil
    extends AbstractUtil
{

    /**
     * Delete all events related to Attributes, Commands and Menus.
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    public void removeEvents(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            final List<String> events = new ArrayList<String>();
            events.add("1497940a-f5e7-4701-a8ce-09311a429d2d"); //Admin_DataModel_AttributeInsertEvent
            events.add("031e3816-d9f3-4535-9bb4-05b25a4db61f"); //Admin_DataModel_AttributeRangeEvent
            events.add("cd4b6f31-726b-4fc5-9597-27a2b41c7e03"); //Admin_DataModel_AttributeRateEvent
            events.add("48f94042-0ffd-4836-9849-255dc956f2f1"); //Admin_DataModel_AttributeReadEvent
            events.add("f609fd67-6648-4ecf-94c2-aa66ece7dfb3"); //Admin_DataModel_AttributeUpdateEvent
            events.add("4eb14278-f982-4a53-92ad-be95b4ee6b30"); //Admin_DataModel_AttributeValidateEvent
            events.add("71b3a912-ec7a-4ab9-81f2-59b4b3db808d"); //Admin_UI_TableEvaluateEvent
            events.add("939e78ba-9254-4790-848a-ca6402f7ee58"); //Admin_UI_ValidateEvent
            events.add("850296d7-cddf-4add-bc0f-dc0ae2f53b27"); //Admin_UI_InstanceManagerEvent
            events.add("83f314e5-1c99-4602-95b8-a257d04c0ce6"); //Admin_UI_AbstractAccessCheckEvent
            events.add("5676c7cd-72fe-473c-a95f-63875b3dd7cd"); //Admin_UI_CommandExecuteEvent

            for (final String event :events) {
                final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString(event));
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                while (query.next()) {
                    final Delete del = new Delete(query.getCurrentValue());
                    del.execute();
                }
            }
        }
    }
}
