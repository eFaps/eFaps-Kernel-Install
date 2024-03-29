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
package org.efaps.esjp.common.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.LinkType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("44b16fe0-5f8f-4ae5-8468-dbb98bcc61b6")
@EFapsApplication("eFaps-Kernel")
public abstract class Tag_Base
    extends AbstractCommon
{

    /**
     * Tag field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return tagFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        @SuppressWarnings("unchecked")
        final List<Instance> reqInstances = (List<Instance>) _parameter.get(ParameterValues.REQUEST_INSTANCES);

        final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
        final Map<Instance, Instance> inst2inst = new HashMap<>();
        if (reqInstances == null) {
            queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, _parameter.getInstance());
        } else {
            if (containsProperty(_parameter, "Select4Instance")) {
                final String select = getProperty(_parameter, "Select4Instance");
                final MultiPrintQuery multi = new MultiPrintQuery(reqInstances);
                multi.addSelect(select);
                multi.execute();
                while (multi.next()) {
                    inst2inst.put(multi.getCurrentInstance(), multi.getSelect(select));
                }
                queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, inst2inst.values().toArray());
            } else {
                queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, reqInstances.toArray());
            }
        }
        final Map<Instance, String> inst2label = new HashMap<>();
        final Type relType = queryBldr.getType();
        Attribute linkAttr = null;
        for (final Attribute attr : relType.getAttributes(LinkType.class)) {
            if (attr.getSqlColNames().get(0).equals("OBJECTID")) {
                linkAttr = attr;
                break;
            }
        }
        final CachedMultiPrintQuery multi = queryBldr.getCachedPrint4Request();
        final SelectBuilder selInst = SelectBuilder.get().linkto(linkAttr.getName()).instance();
        multi.addSelect(selInst);
        multi.execute();
        while (multi.next()) {
            final Instance inst = multi.getSelect(selInst);
            if (!inst2label.containsKey(inst)) {
                inst2label.put(inst, multi.getCurrentInstance().getType().getLabel());
            } else {
                inst2label.put(inst, inst2label.get(inst) + ", " + multi.getCurrentInstance().getType().getLabel());
            }
        }
        if (inst2inst.isEmpty()) {
            ret.put(ReturnValues.VALUES, inst2label.get(_parameter.getInstance()));
        } else {
            ret.put(ReturnValues.VALUES, inst2label.get(inst2inst.get(_parameter.getInstance())));
        }
        return ret;
    }

    /**
     * Check 4 tag.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return check4Tag(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance objectInst = _parameter.getInstance();
        boolean access = false;
        final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
        if (queryBldr != null) {
            queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, objectInst);
            access = queryBldr.getQuery().executeWithoutAccessCheck().isEmpty();
        }

        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
        if (!inverse && access || inverse && !access) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Sets the tag.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return setTag(final Parameter _parameter)
        throws EFapsException
    {

        final Collection<String> typeStrs = analyseProperty(_parameter, "Type").values();
        final List<Type> types = new ArrayList<>();
        for (final String typeStr : typeStrs) {
            if (isUUID(typeStr)) {
                types.add(Type.get(UUID.fromString(typeStr)));
            } else {
                types.add(Type.get(typeStr));
            }
        }

        final boolean evalOID = BooleanUtils.toBoolean(getProperty(_parameter, "EvalOID", "false"));
        final Set<Instance> instances = new HashSet<>();
        if (evalOID) {
            final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
            for (final String oid : oids) {
                final Instance inst = Instance.get(oid);
                if (inst.isValid()) {
                    instances.add(inst);
                }
            }
        } else {
            instances.add(_parameter.getInstance());
        }

        for (final Type type : types) {
            for (final Instance instance : instances) {
                Tag.tagObject(_parameter, instance, type);
            }
        }
        final Return ret = new Return();
        return ret;
    }

    /**
     * Removes the tag.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return removeTag(final Parameter _parameter)
        throws EFapsException
    {

        final Collection<String> typeStrs = analyseProperty(_parameter, "Type").values();
        final List<Type> types = new ArrayList<>();
        for (final String typeStr : typeStrs) {
            if (isUUID(typeStr)) {
                types.add(Type.get(UUID.fromString(typeStr)));
            } else {
                types.add(Type.get(typeStr));
            }
        }
        final boolean evalOID = BooleanUtils.toBoolean(getProperty(_parameter, "EvalOID", "false"));

        final Set<Instance> instances = new HashSet<>();
        if (evalOID) {
            final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
            for (final String oid : oids) {
                final Instance inst = Instance.get(oid);
                if (inst.isValid()) {
                    instances.add(inst);
                }
            }
        } else {
            instances.add(_parameter.getInstance());
        }
        for (final Type type : types) {
            for (final Instance instance : instances) {
                Tag.untagObject(_parameter, instance, type);
            }
        }
        final Return ret = new Return();
        return ret;
    }

    /**
     * Tag object.
     *
     * @param _parameter the parameter
     * @param _objectInst the object inst
     * @param _tagType the tag type
     * @return the instance
     * @throws EFapsException the eFaps exception
     */
    protected static Instance tagObject(final Parameter _parameter,
                                        final Instance _objectInst,
                                        final Type _tagType)
        throws EFapsException
    {
        Instance ret = null;
        if (InstanceUtils.isValid(_objectInst)) {
            final QueryBuilder queryBldr = new QueryBuilder(_tagType);
            queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, _objectInst);
            if (queryBldr.getQuery().executeWithoutAccessCheck().isEmpty()) {
                final Insert insert = new Insert(_tagType);
                insert.add(CICommon.TagAbstract.ObjectID, _objectInst);
                insert.execute();
                ret = insert.getInstance();
            }
        }
        return ret;
    }

    /**
     * Tag object.
     *
     * @param _parameter the parameter
     * @param _objectInst the object inst
     * @param _tagType the tag type
     * @return the instance
     * @throws EFapsException the eFaps exception
     */
    protected static Instance untagObject(final Parameter _parameter,
                                          final Instance _objectInst,
                                          final Type _tagType)
        throws EFapsException
    {
        final Instance ret = null;
        if (InstanceUtils.isValid(_objectInst)) {
            final QueryBuilder queryBldr = new QueryBuilder(_tagType);
            queryBldr.addWhereAttrEqValue(CICommon.TagAbstract.ObjectID, _objectInst);
            for (final Instance inst : queryBldr.getQuery().executeWithoutAccessCheck()) {
                new Delete(inst).execute();
            }
        }
        return ret;
    }
}
