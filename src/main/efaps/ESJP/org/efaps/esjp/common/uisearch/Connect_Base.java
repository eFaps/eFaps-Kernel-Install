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


package org.efaps.esjp.common.uisearch;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("251927a6-d71d-4a7b-b0d2-b6bb5a50b73f")
@EFapsRevision("$Rev$")
public abstract class Connect_Base
    extends AbstractCommon
    implements EventExecution
{
    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final String[] childOids = (String[]) others.get("selectedRow");
        if (childOids != null) {

            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");
            final boolean allowAttr = "false".equals(properties.get("AllowMultiple"));

            for (final String childOid : childOids) {
                final Instance child = Instance.get(childOid);
                final Type type = getConnectType(_parameter, child);
                boolean check = false;
                if (allowAttr) {
                    final QueryBuilder queryBldr = new QueryBuilder(type);
                    queryBldr.addWhereAttrEqValue(parentAttr, parent.getId());
                    queryBldr.addWhereAttrEqValue(childAttr, child.getId());
                    final InstanceQuery query = queryBldr.getQuery();
                    query.execute();
                    if (query.next()) {
                        check = true;
                    }
                }
                if (!check) {
                    final Insert insert = new Insert(type);
                    insert.add(parentAttr, parent.getId());
                    insert.add(childAttr, child.getId());
                    insert.execute();
                }
            }
        }
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _childInstance Instance of the child that will be connected
     * @return the type used for the connection
     * @throws EFapsException on error
     */
    protected Type getConnectType(final Parameter _parameter,
                                  final Instance _childInstance)
        throws EFapsException
    {
        final String typeStr = getProperty(_parameter, "ConnectType");
        return Type.get(typeStr);
    }
}