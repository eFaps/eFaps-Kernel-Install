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
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
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
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);
        final String[] childOids = (String[]) others.get("selectedRow");
        if (childOids != null) {
            final Map<Integer, String> parentAttrs = analyseProperty(_parameter, "ConnectParentAttribute");
            final Map<Integer, String> childAttrs = analyseProperty(_parameter, "ConnectChildAttribute");
            final Map<Integer, String> childTypes = analyseProperty(_parameter, "ConnectChildType");
            final Map<Integer, String> parentTypes = analyseProperty(_parameter, "ConnectParentType");
            final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");

            final boolean checkMultiple = "false".equals(getProperty(_parameter, "AllowMultiple"));
            if (parentAttrs.isEmpty() || childAttrs.isEmpty() || parentAttrs.size() != childAttrs.size()
                            || types.isEmpty()) {
                Connect_Base.LOG.error("Must have properties 'ConnectParentAttribute' and 'ConnectChildAttribute' "
                                + "of same size and at least one 'ConnectType'");
            } else {
                for (final String childOid : childOids) {
                    final Instance childInst = Instance.get(childOid);
                    if (childInst.isValid()) {
                        final int idx = getIdx(_parameter, childInst, parentTypes, types, childTypes);
                        if (idx > -1) {
                            boolean check = false;
                            final String typeStr = types.get(idx);
                            final Type type = isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr);
                            final Attribute parentAttr = getAttr(_parameter, idx, types, parentAttrs);
                            final Attribute childAttr = getAttr(_parameter, idx, types, childAttrs);
                            if (checkMultiple) {
                                final QueryBuilder queryBldr = new QueryBuilder(type);
                                queryBldr.addWhereAttrEqValue(parentAttr, _parameter.getInstance());
                                queryBldr.addWhereAttrEqValue(childAttr, childInst);
                                final InstanceQuery query = queryBldr.getQuery();
                                query.executeWithoutAccessCheck();
                                if (query.next()) {
                                    check = true;
                                }
                            }
                            if (!check) {
                                final Insert insert = new Insert(type);
                                addInsertConnect(_parameter, insert);
                                insert.add(parentAttr, _parameter.getInstance());
                                insert.add(childAttr, childInst);
                                insert.execute();
                            }
                        }
                    }
                }
            }
        }
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _idx    index
     * @param _types mapping of childTypes
     * @param _attrs    mapping of types
     * @return the attribute
     * @throws EFapsException on error
     */
    protected Attribute getAttr(final Parameter _parameter,
                                final int _idx,
                                final Map<Integer, String> _types,
                                final Map<Integer, String> _attrs)
        throws EFapsException
    {
        Attribute ret = null;
        final String typeStr = _types.get(_idx);
        final Type type = isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr);
        final String attrStr;
        if (_attrs.containsKey(_idx)) {
            attrStr = _attrs.get(_idx);
        } else {
            attrStr = _attrs.values().iterator().next();
        }
        ret = type.getAttribute(attrStr);
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _childInst    instance to be connected
     * @param _childTypes mapping of childTypes
     * @param _types    mapping of types
     * @return the type
     * @throws EFapsException on error
     */
    protected int getIdx(final Parameter _parameter,
                             final Instance _childInst,
                             final Map<Integer, String> _parentTypes,
                             final Map<Integer, String> _types,
                             final Map<Integer, String> _childTypes)
        throws EFapsException
    {
        int ret = -1;
        // simple version without any mapping
        if (_parentTypes.isEmpty() && _childTypes.isEmpty()) {
            ret = _types.keySet().iterator().next();
        } else {
            // only childTypes
            if (_parentTypes.isEmpty()) {
                for (final Entry<Integer, String> entry : _childTypes.entrySet()) {
                    final String childTypeStr = entry.getValue();
                    final Type childType = isUUID(childTypeStr) ? Type.get(UUID.fromString(childTypeStr)) : Type
                                    .get(childTypeStr);
                    if (_childInst.getType().equals(childType)) {
                        ret = entry.getKey();
                        break;
                    }
                }
             // only parentTypes
            } else if (_childTypes.isEmpty()) {
                for (final Entry<Integer, String> entry : _parentTypes.entrySet()) {
                    final String parentTypeStr = entry.getValue();
                    final Type parentType = isUUID(parentTypeStr) ? Type.get(UUID.fromString(parentTypeStr)) : Type
                                    .get(parentTypeStr);
                    if (_parameter.getInstance().getType().equals(parentType)) {
                        ret = entry.getKey();
                        break;
                    }
                }
            // both
            } else {
                for (final Entry<Integer, String> entry : _parentTypes.entrySet()) {
                    final String parentTypeStr = entry.getValue();
                    final Type parentType = isUUID(parentTypeStr) ? Type.get(UUID.fromString(parentTypeStr)) : Type
                                    .get(parentTypeStr);
                    if (_parameter.getInstance().getType().equals(parentType)) {
                        final String childTypeStr = _childTypes.get(entry.getKey());
                        final Type childType = isUUID(childTypeStr) ? Type.get(UUID.fromString(childTypeStr)) : Type
                                        .get(childTypeStr);
                        if (_childInst.getType().equals(childType)) {
                            ret = entry.getKey();
                            break;
                        }
                    }
                }
            }
        }
        return ret;
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
}
