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
import org.efaps.admin.datamodel.Status;
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
@EFapsApplication("eFaps-Kernel")
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

            if (!validateProperties(_parameter)) {
                Connect_Base.LOG.error("Must have properties 'ConnectParentAttribute' and 'ConnectChildAttribute' "
                                + "of same size and at least one 'ConnectType'");
            } else {
                for (final String childOid : childOids) {
                    final Instance childInst = Instance.get(childOid);
                    if (childInst.isValid()) {
                        final int idx = getIdx(_parameter, childInst);
                        final TypeWA typeWithAttr = getTypeWithAttribute(_parameter, childInst, idx);
                        if (typeWithAttr != null && checkMultiple(_parameter, typeWithAttr, childInst)) {
                            final Insert insert = new Insert(typeWithAttr.getType());
                            addInsertConnect(_parameter, insert);
                            insert.add(typeWithAttr.getParentAttr(), _parameter.getInstance());
                            insert.add(typeWithAttr.getChildAttr(), childInst);
                            insert.execute();
                        }
                    }
                }
            }
        }
        return new Return();
    }

    protected boolean checkMultiple(final Parameter _parameter,
                                    final TypeWA _typeWA,
                                    final Instance _childInst)
        throws EFapsException
    {
        final boolean checkMultiple = "false".equals(getProperty(_parameter, "AllowMultiple"));
        boolean check = false;
        if (checkMultiple) {
            final QueryBuilder queryBldr = new QueryBuilder(_typeWA.getType());
            queryBldr.addWhereAttrEqValue(_typeWA.getParentAttr(), _parameter.getInstance());
            queryBldr.addWhereAttrEqValue(_typeWA.getChildAttr(), _childInst);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            if (!query.next()) {
                check = true;
            }
        } else {
            check = true;
        }
        return check;
    }

    protected boolean validateProperties(final Parameter _parameter)
        throws EFapsException
    {
        final Map<Integer, String> parentAttrs = analyseProperty(_parameter, "ConnectParentAttribute");
        final Map<Integer, String> childAttrs = analyseProperty(_parameter, "ConnectChildAttribute");
        final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");

        return !(parentAttrs.isEmpty() || childAttrs.isEmpty() || parentAttrs.size() != childAttrs.size()
                        || types.isEmpty());
    }

    protected TypeWA getTypeWithAttribute(final Parameter _parameter,
                                          final Instance _childInst,
                                          final int _idx)
        throws EFapsException
    {
        final Map<Integer, String> parentAttrs = analyseProperty(_parameter, "ConnectParentAttribute");
        final Map<Integer, String> childAttrs = analyseProperty(_parameter, "ConnectChildAttribute");
        final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");

        final String typeStr = types.get(_idx);

        final TypeWA typeWithAttr = new TypeWA();
        typeWithAttr.setParentAttr(getAttr(_parameter, _idx, types, parentAttrs));
        typeWithAttr.setChildAttr(getAttr(_parameter, _idx, types, childAttrs));
        typeWithAttr.setType(isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr));

        return typeWithAttr;
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
                             final Instance _childInst)
        throws EFapsException
    {
        int ret = -1;

        final Map<Integer, String> childTypes = analyseProperty(_parameter, "ConnectChildType");
        final Map<Integer, String> parentTypes = analyseProperty(_parameter, "ConnectParentType");
        final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");

        // simple version without any mapping
        if (parentTypes.isEmpty() && childTypes.isEmpty()) {
            ret = types.keySet().iterator().next();
        } else {
            // only childTypes
            if (parentTypes.isEmpty()) {
                for (final Entry<Integer, String> entry : childTypes.entrySet()) {
                    final String childTypeStr = entry.getValue();
                    final Type childType = isUUID(childTypeStr) ? Type.get(UUID.fromString(childTypeStr)) : Type
                                    .get(childTypeStr);
                    if (_childInst.getType().equals(childType)) {
                        ret = entry.getKey();
                        break;
                    }
                }
             // only parentTypes
            } else if (childTypes.isEmpty()) {
                for (final Entry<Integer, String> entry : parentTypes.entrySet()) {
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
                for (final Entry<Integer, String> entry : parentTypes.entrySet()) {
                    final String parentTypeStr = entry.getValue();
                    final Type parentType = isUUID(parentTypeStr) ? Type.get(UUID.fromString(parentTypeStr)) : Type
                                    .get(parentTypeStr);
                    if (_parameter.getInstance().getType().equals(parentType)) {
                        final String childTypeStr = childTypes.get(entry.getKey());
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
        if (getProperty(_parameter, "StatusGroup") != null && getProperty(_parameter, "Status") != null) {
            final Status status = Status
                            .find(getProperty(_parameter, "StatusGroup"), getProperty(_parameter, "Status"));
            if (status != null) {
                _insert.add(_insert.getInstance().getType().getStatusAttribute(), status);
            }
        }
    }


    public static class TypeWA
    {

        private Attribute parentAttr;

        private Attribute childAttr;

        private Type type;

        public void setParentAttr(final Attribute _parentAttr)
        {
            this.parentAttr = _parentAttr;
        }

        public Attribute getParentAttr()
        {
            return this.parentAttr;
        }

        public void setChildAttr(final Attribute _childAttr)
        {
            this.childAttr = _childAttr;
        }

        public Attribute getChildAttr()
        {
            return this.childAttr;
        }

        public void setType(final Type _type)
        {
            this.type = _type;
        }

        public Type getType()
        {
            return this.type;
        }
    }
}
