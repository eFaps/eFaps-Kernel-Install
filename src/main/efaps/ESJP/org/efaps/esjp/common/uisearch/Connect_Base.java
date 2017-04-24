/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.esjp.common.uisearch;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.api.datamodel.Relationship;
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
        final List<Instance> instances = getSelectedInstances(_parameter);
        if (!instances.isEmpty()) {
            if (!validateProperties(_parameter)) {
                Connect_Base.LOG.error("Must have properties 'ConnectParentAttribute' and 'ConnectChildAttribute' "
                                + "of same size and at least one 'ConnectType'");
            } else {
                for (final Instance childInst : instances) {
                    final int idx = getIdx(_parameter, childInst);
                    final ConnectType connectType = getConnectType(_parameter, childInst, idx);
                    if (connectType != null) {
                        final Insert insert = new Insert(connectType.getType());
                        addInsertConnect(_parameter, insert);
                        insert.add(connectType.getParentAttr(), _parameter.getInstance());
                        insert.add(connectType.getChildAttr(), childInst);
                        insert.execute();
                    }
                }
            }
        }
        return new Return();
    }

    /**
     * Validate properties.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return true, if successful
     * @throws EFapsException on error
     */
    protected boolean validateProperties(final Parameter _parameter)
        throws EFapsException
    {
        final Map<Integer, String> parentAttrs = analyseProperty(_parameter, "ConnectParentAttribute");
        final Map<Integer, String> childAttrs = analyseProperty(_parameter, "ConnectChildAttribute");
        final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");

        return !(parentAttrs.isEmpty() || childAttrs.isEmpty() || parentAttrs.size() != childAttrs.size()
                        || types.isEmpty());
    }

    /**
     * Gets the type with attribute.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _childInst the child inst
     * @param _idx the idx
     * @return the type with attribute
     * @throws EFapsException on error
     */
    protected ConnectType getConnectType(final Parameter _parameter,
                                         final Instance _childInst,
                                         final int _idx)
        throws EFapsException
    {
        final Map<Integer, String> types = analyseProperty(_parameter, "ConnectType");
        final Map<Integer, String> parentAttrs = analyseProperty(_parameter, "ConnectParentAttribute");
        final Map<Integer, String> childAttrs = analyseProperty(_parameter, "ConnectChildAttribute");

        final String typeStr = types.get(_idx);

        final ConnectType ret = new ConnectType();
        ret.setParentAttr(getAttr(_parameter, _idx, types, parentAttrs));
        ret.setChildAttr(getAttr(_parameter, _idx, types, childAttrs));
        ret.setType(isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr));

        evalRelationship(_parameter, ret, _idx);

        return ret;
    }

    /**
     * Evaluate relationship.
     *
     * @param _parameter the _parameter
     * @param _connectType the _connect type
     * @param _idx the _idx
     * @throws EFapsException the e faps exception
     */
    protected void evalRelationship(final Parameter _parameter,
                                    final ConnectType _connectType,
                                    final int _idx)
        throws EFapsException
    {
        // first priority has the listener
        for (final IConnectListener listener : Listener.get().<IConnectListener>invoke(IConnectListener.class)) {
            listener.evalRelationship(_parameter, _connectType, _idx);
        }
        // if the listener does not define it search in properties
        if (Relationship.Undefined.equals(_connectType.getRelationship())) {
            final Map<Integer, String> relationships = analyseProperty(_parameter, "Relationship");
            final Map<Integer, String> parentIsFrom = analyseProperty(_parameter, "RelationshipParentIsFrom");
            final Map<Integer, String> uniques = analyseProperty(_parameter, "RelationshipUnique");
            if (!relationships.isEmpty()) {
                final Relationship relationship =  EnumUtils.getEnum(Relationship.class, relationships.get(_idx));
                _connectType.setRelationship(relationship);
                if (uniques.containsKey(_idx) && !uniques.get(_idx).isEmpty()) {
                    _connectType.setUnique(BooleanUtils.toBoolean(uniques.get(_idx)));
                }
                if (parentIsFrom.containsKey(_idx) && !parentIsFrom.get(_idx).isEmpty()) {
                    _connectType.setParentIsFrom(BooleanUtils.toBoolean(parentIsFrom.get(_idx)));
                }
            }
        }
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
     * Gets the idx.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _childInst    instance to be connected
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
                    if (childType.isAbstract() && _childInst.getType().isKindOf(childType)
                                    || _childInst.getType().equals(childType)) {
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


    /**
     * Validate.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return validate(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final List<Instance> instances = getSelectedInstances(_parameter);
        final StringBuilder warning = new StringBuilder();
        if (!instances.isEmpty()) {
            if (!validateProperties(_parameter)) {
                Connect_Base.LOG.error("Must have properties 'ConnectParentAttribute' and 'ConnectChildAttribute' "
                                + "of same size and at least one 'ConnectType'");
            } else {
                for (final Instance childInst : instances) {
                    final int idx = getIdx(_parameter, childInst);
                    final ConnectType connectType = getConnectType(_parameter, childInst, idx);
                    if (!Relationship.Undefined.equals(connectType.relationship)) {
                        warning.append(checkRelationship(_parameter, connectType,
                                        connectType.isParentIsFrom() ? _parameter.getInstance() : childInst,
                                        connectType.isParentIsFrom() ? childInst : _parameter.getInstance(),
                                        idx));
                    }
                }
            }
        }
        if (warning.length() == 0) {
            ret.put(ReturnValues.TRUE, true);
        } else {
            ret.put(ReturnValues.SNIPLETT, warning.toString());
        }
        return ret;
    }

    /**
     * Check constrains.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _connectType the type wa
     * @param _fromInst the _from inst
     * @param _toInst the _to instif true, if successful
     * @param _idx the _idx
     * @return the char sequence
     * @throws EFapsException on error
     */
    protected CharSequence checkRelationship(final Parameter _parameter,
                                             final ConnectType _connectType,
                                             final Instance _fromInst,
                                             final Instance _toInst,
                                             final int _idx)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        //first check for Unique
        if (_connectType.isUnique()) {
            final QueryBuilder queryBldr = new QueryBuilder(_connectType.getType());
            queryBldr.addWhereAttrEqValue(_connectType.getFromAttr(), _fromInst);
            queryBldr.addWhereAttrEqValue(_connectType.getToAttr(), _toInst);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            if (query.next()) {
                ret.append(getFormatedDBProperty("Realtionship.UniqueWarning", _idx + 1));
            }
        }
        if (ret.length() == 0) {
            switch (_connectType.getRelationship()) {
                case OneToOne:
                    // check from
                    final QueryBuilder queryBldr1 = new QueryBuilder(_connectType.getType());
                    queryBldr1.addWhereAttrEqValue(_connectType.getFromAttr(), _fromInst);
                    final InstanceQuery query1 = queryBldr1.getQuery();
                    query1.executeWithoutAccessCheck();
                    if (query1.next()) {
                        ret.append(getFormatedDBProperty("Realtionship.OneToOneWarning", _idx + 1));
                    }
                    if (ret.length() == 0) {
                        // check to
                        final QueryBuilder queryBldr2 = new QueryBuilder(_connectType.getType());
                        queryBldr2.addWhereAttrEqValue(_connectType.getToAttr(), _toInst);
                        final InstanceQuery query2 = queryBldr2.getQuery();
                        query2.executeWithoutAccessCheck();
                        if (query2.next()) {
                            ret.append(getFormatedDBProperty("Realtionship.OneToOneWarning", _idx + 1));
                        }
                    }
                    break;
                case OneToMany:
                    // check from
                    final QueryBuilder queryBldr3 = new QueryBuilder(_connectType.getType());
                    queryBldr3.addWhereAttrEqValue(_connectType.getFromAttr(), _fromInst);
                    final InstanceQuery query3 = queryBldr3.getQuery();
                    query3.executeWithoutAccessCheck();
                    if (query3.next()) {
                        ret.append(getFormatedDBProperty("Realtionship.OneToManyWarning", _idx + 1));
                    }
                    break;
                case ManyToOne:
                    // check to
                    final QueryBuilder queryBldr4 = new QueryBuilder(_connectType.getType());
                    queryBldr4.addWhereAttrEqValue(_connectType.getToAttr(), _toInst);
                    final InstanceQuery query4 = queryBldr4.getQuery();
                    query4.executeWithoutAccessCheck();
                    if (query4.next()) {
                        ret.append(getFormatedDBProperty("Realtionship.ManyToOneWarning", _idx + 1));
                    }
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    /**
     * The Class ConnectType.
     *
     * @author The eFaps Team
     */
    public static class ConnectType
    {

        /** The parent attr. */
        private Attribute parentAttr;

        /** The child attr. */
        private Attribute childAttr;

        /** The type. */
        private Type type;

        /** The relationship. */
        private Relationship relationship = Relationship.Undefined;

        /** The parent is from. */
        private boolean parentIsFrom = true;

        /** The unique. */
        private boolean unique = true;

        /**
         * Sets the parent attr.
         *
         * @param _parentAttr the new parent attr
         * @return the type wa
         */
        public ConnectType setParentAttr(final Attribute _parentAttr)
        {
            this.parentAttr = _parentAttr;
            return this;
        }

        /**
         * Gets the parent attr.
         *
         * @return the parent attr
         */
        public Attribute getParentAttr()
        {
            return this.parentAttr;
        }

        /**
         * Sets the child attr.
         *
         * @param _childAttr the new child attr
         * @return the type wa
         */
        public ConnectType setChildAttr(final Attribute _childAttr)
        {
            this.childAttr = _childAttr;
            return this;
        }

        /**
         * Gets the child attr.
         *
         * @return the child attr
         */
        public Attribute getChildAttr()
        {
            return this.childAttr;
        }

        /**
         * Sets the type.
         *
         * @param _type the new type
         * @return the type wa
         */
        public ConnectType setType(final Type _type)
        {
            this.type = _type;
            return this;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public Type getType()
        {
            return this.type;
        }

        /**
         * Getter method for the instance variable {@link #relationship}.
         *
         * @return value of instance variable {@link #relationship}
         */
        public Relationship getRelationship()
        {
            return this.relationship;
        }

        /**
         * Setter method for instance variable {@link #relationship}.
         *
         * @param _relationship value for instance variable {@link #relationship}
         * @return the type wa
         */
        public ConnectType setRelationship(final Relationship _relationship)
        {
            this.relationship = _relationship;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #fromAttr}.
         *
         * @return value of instance variable {@link #fromAttr}
         */
        public Attribute getFromAttr()
        {
            return isParentIsFrom() ?  getParentAttr() : getChildAttr();
        }

        /**
         * Getter method for the instance variable {@link #toAttr}.
         *
         * @return value of instance variable {@link #toAttr}
         */
        public Attribute getToAttr()
        {
            return isParentIsFrom() ? getChildAttr() :  getParentAttr();
        }

        /**
         * Getter method for the instance variable {@link #unique}.
         *
         * @return value of instance variable {@link #unique}
         */
        public boolean isUnique()
        {
            return this.unique;
        }

        /**
         * Setter method for instance variable {@link #unique}.
         *
         * @param _unique value for instance variable {@link #unique}
         * @return the connect type
         */
        public ConnectType setUnique(final boolean _unique)
        {
            this.unique = _unique;
            return this;
        }


        /**
         * Getter method for the instance variable {@link #parentIsFrom}.
         *
         * @return value of instance variable {@link #parentIsFrom}
         */
        public boolean isParentIsFrom()
        {
            return this.parentIsFrom;
        }


        /**
         * Setter method for instance variable {@link #parentIsFrom}.
         *
         * @param _parentIsFrom value for instance variable {@link #parentIsFrom}
         */
        public void setParentIsFrom(final boolean _parentIsFrom)
        {
            this.parentIsFrom = _parentIsFrom;
        }
    }
}
