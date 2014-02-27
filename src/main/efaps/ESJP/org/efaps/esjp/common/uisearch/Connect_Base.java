/*
ConnectTypeMap * Copyright 2003 - 2013 The eFaps Team
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
import java.util.UUID;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Connect.class);


    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final String[] childOids = (String[]) others.get("selectedRow");
        if (childOids != null) {

            final String parentAttr = getProperty(_parameter, "ConnectParentAttribute");
            final String childAttr = getProperty(_parameter, "ConnectChildAttribute");
            final boolean allowAttr = "false".equals(getProperty(_parameter, "AllowMultiple"));
            if (parentAttr == null || childAttr == null) {
                Connect_Base.LOG.error("Missing properties 'ConnectParentAttribute' or 'ConnectChildAttribute'");
            }
            for (final String childOid : childOids) {
                final Instance child = Instance.get(childOid);
                if (child.isValid()) {
                    final Type type = getConnectType(_parameter, child);
                    if (type != null) {
                        boolean check = false;
                        if (allowAttr) {
                            final QueryBuilder queryBldr = new QueryBuilder(type);
                            queryBldr.addWhereAttrEqValue(parentAttr, parent.getId());
                            queryBldr.addWhereAttrEqValue(childAttr, child.getId());
                            final InstanceQuery query = queryBldr.getQuery();
                            query.executeWithoutAccessCheck();
                            if (query.next()) {
                                check = true;
                            }
                        }
                        if (!check) {
                            final Insert insert = new Insert(type);
                            addInsertConnect(_parameter, insert);
                            insert.add(parentAttr, parent.getId());
                            insert.add(childAttr, child.getId());
                            insert.execute();
                        }
                    }
                }
            }
        }
        return new Return();
    }

    /**
     * To be used by implementation to add to the basic insert.
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _insert       insert to be added to
     * @throws EFapsException on error
     */
    protected void addInsertConnect(final Parameter _parameter,
                                    final Insert _insert)
        throws EFapsException
    {
        // to implement
    }

    /**
     * Get the connect type.
     * Can be used with a concatenated mapping of
     * 'UUID from ChildObject';'UUID from MiddleObject'
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
        Type ret = null;
        // if no simple type, check for a mapping
        if (typeStr == null) {
            final Map<Integer, String> mapping = analyseProperty(_parameter, "ConnectTypeMap");
            for (final String typeMap : mapping.values()) {
                final String[] uuids = typeMap.split(";");
                if (_childInstance.getType().getUUID().equals(UUID.fromString(uuids[0]))) {
                    ret = Type.get(UUID.fromString(uuids[1]));
                }
            }
        } else {
            ret  = Type.get(typeStr);
        }
        return ret;
    }
}
