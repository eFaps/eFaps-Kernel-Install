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
package org.efaps.esjp.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * To function the Attribute "AccessCheck4Object" + n (0-100) must be added to the
 * kernel SystemConfiguration containing a PropertyMap.
 * <table>
 * <tr>
 * <th>Property</th><th>Value</th><th>Description</th>
 * </tr>
 * <tr>
 * <td>[TYPENAME].ParentAttribute</td><td>String</td>
 * <td>Name of the attribute that links to the parent object.</td>
 * </tr>
 * </table>
 *
 * @author The eFaps Team
 */
@EFapsUUID("6306017b-af25-4756-b19c-8ba00e51448e")
@EFapsApplication("eFaps-Kernel")
public abstract class InheritAccessCheck4Object_Base
    extends AccessCheck4Object
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InheritAccessCheck4Object.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getObjectAccess(final Parameter _parameter,
                                      final Instance _instance,
                                      final AccessType _accessType)
        throws EFapsException
    {
        boolean ret = false;
        // check if for this object something is defined, if not go for the parent object
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select count(*) ")
            .append(" from T_ACCESS4OBJ ")
            .append(" where TYPEID = ").append(_instance.getType().getId())
            .append(" and OBJID =").append(_instance.getId());

        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            Statement stmt = null;
            try {
                stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                if (rs.next()) {
                    ret = rs.getLong(1) > 0 ? true : false;
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (final SQLException e) {
            InheritAccessCheck4Object_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        }
        if (ret) {
            ret = super.getObjectAccess(_parameter, _instance, _accessType);
        } else {
            final Instance parentInst = getParentInstance(_parameter, _instance, _accessType);
            if (check4SimpleAccessCheck(_parameter, parentInst)) {
                ret = getSimpleAccess4Type(_parameter).checkAccess(_parameter, parentInst, _accessType);
            } else if (!parentInst.isValid() && _instance.isValid()
                            && super.check4SimpleAccessCheck(_parameter, _instance)) {
                ret = getSimpleAccess4Type(_parameter).checkAccess(_parameter, _instance, _accessType);
            } else {
                ret = getObjectAccess(_parameter, parentInst, _accessType);
            }
        }
        return ret;
    }

    @Override
    protected boolean check4SimpleAccessCheck(final Parameter _parameter,
                                              final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
        //create
        if (!accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
            ret = super.check4SimpleAccessCheck(_parameter, _instance);
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instance     Instanc the parent is wanted for
     * @param _accessType   AccessType
     * @return Instance of the parent if found
     * @throws EFapsException on error
     */
    protected Instance getParentInstance(final Parameter _parameter,
                                         final Instance _instance,
                                         final AccessType _accessType)
        throws EFapsException
    {
        Instance ret = Instance.get("");
        final Properties props = getProperties(_parameter);
        final String parentAttribute = props.getProperty(_instance.getType().getName() + ".ParentAttribute");
        if (parentAttribute != null) {
            //create
            if (!_instance.isValid()
                            && _accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
                @SuppressWarnings("unchecked")
                final Map<Attribute, Object[]> values = (Map<Attribute, Object[]>) _parameter.get(
                                ParameterValues.NEW_VALUES);
                for (final Entry<Attribute, Object[]> entry : values.entrySet()) {
                    if (parentAttribute.equals(entry.getKey().getName())) {
                        final Long parentId = (Long) entry.getValue()[0];
                        final QueryBuilder queryBldr = new QueryBuilder(entry.getKey().getLink());
                        queryBldr.addWhereAttrEqValue("ID", parentId);
                        final InstanceQuery query = queryBldr.getQuery();
                        query.executeWithoutAccessCheck();
                        if (query.next()) {
                            ret = query.getCurrentValue();
                        }
                    }
                }
            } else {
                final PrintQuery print = new PrintQuery(_instance);
                final SelectBuilder sel = new SelectBuilder().linkto(parentAttribute).oid();
                print.addSelect(sel);
                if (print.executeWithoutAccessCheck()) {
                    ret = Instance.get(print.<String>getSelect(sel));
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<Instance, Boolean> getObjectAccess(final Parameter _parameter,
                                                     final List<Instance> _instances,
                                                     final AccessType _accessType)
        throws EFapsException
    {
        // check if for this objects something is defined, if not go for the parent object
        final Map<Instance, Boolean> ret = new HashMap<>();
        final StringBuilder cmd = new StringBuilder();
        final Map<Long, List<Long>> typeid2objectids = new HashMap<>();
        for (final Instance instance : _instances) {
            if (instance != null && instance.isValid()) {
                final List<Long> ids;
                if (typeid2objectids.containsKey(instance.getType().getId())) {
                    ids = typeid2objectids.get(instance.getType().getId());
                } else {
                    ids = new ArrayList<>();
                    typeid2objectids.put(instance.getType().getId(), ids);
                }
                ids.add(instance.getId());
            }
        }
        cmd.append("select TYPEID, OBJID ")
            .append(" from T_ACCESS4OBJ ")
            .append(" where (");
        boolean first = true;
        for (final Entry<Long, List<Long>>entry : typeid2objectids.entrySet()) {
            if (first) {
                first = false;
            } else {
                cmd.append(" OR ");
            }
            cmd.append(" (TYPEID = ").append(entry.getKey()).append(" and OBJID in (");
            boolean firstID = true;
            for (final Long id : entry.getValue()) {
                if (firstID) {
                    firstID = false;
                } else {
                    cmd.append(",");
                }
                cmd.append(id);
            }
            cmd.append(")) ");
        }
        cmd.append(")");
        final Set<Instance> instan = new HashSet<>();
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                while (rs.next()) {
                    instan.add(Instance.get(Type.get(rs.getLong(1)), rs.getLong(2)));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (final SQLException e) {
            InheritAccessCheck4Object_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            final List<Instance> accessInst = new ArrayList<>();
            final List<Instance> inheritInst = new ArrayList<>();
            for (final Instance inst : _instances) {
                if (instan.contains(inst)) {
                    accessInst.add(inst);
                } else {
                    inheritInst.add(inst);
                }
            }
            if (!accessInst.isEmpty()) {
                ret.putAll(super.getObjectAccess(_parameter, accessInst, _accessType));
            }

            final List<Instance> simpleAccess = new ArrayList<>();
            final List<Instance> objectAccess = new ArrayList<>();
            final Map<Instance, Instance> parentInsts = getParentInstances(_parameter, inheritInst, _accessType);
            for (final Instance instance : parentInsts.values()) {
                if (check4SimpleAccessCheck(_parameter, instance)) {
                    simpleAccess.add(instance);
                } else {
                    objectAccess.add(instance);
                }
            }
            final Map<Instance, Boolean> tmp = new HashMap<>();
            if (!simpleAccess.isEmpty()) {
                tmp.putAll(getSimpleAccess4Type(_parameter).checkAccess(_parameter, simpleAccess, _accessType));
            }
            if (!objectAccess.isEmpty()) {
                tmp.putAll(getObjectAccess(_parameter, objectAccess, _accessType));
            }

            for (final Instance instance : inheritInst) {
                boolean hasAccess = false;
                if (parentInsts.get(instance) != null && tmp.containsKey(parentInsts.get(instance))) {
                    hasAccess = tmp.get(parentInsts.get(instance));
                }
                ret.put(instance, hasAccess);
            }
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instances    Instances the parent is wanted for
     * @param _accessType   AccessType
     * @return Instance of the parent if found
     * @throws EFapsException on error
     */
    protected Map<Instance, Instance> getParentInstances(final Parameter _parameter,
                                                         final List<Instance> _instances,
                                                         final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Instance> ret = new HashMap<>();
        final Properties props = getProperties(_parameter);
        final Map<Type, List<Instance>> type2instance = new HashMap<>();
        for (final Instance instance : _instances) {
            if (instance != null && instance.isValid()) {
                final List<Instance> instances;
                if (type2instance.containsKey(instance.getType())) {
                    instances = type2instance.get(instance.getType());
                } else {
                    instances = new ArrayList<>();
                    type2instance.put(instance.getType(), instances);
                }
                instances.add(instance);
            }
        }

        for (final Entry<Type, List<Instance>> entry : type2instance.entrySet()) {
            final String parentAttribute = props.getProperty(entry.getKey().getName() + ".ParentAttribute");
            if (parentAttribute != null) {
                final MultiPrintQuery print = new MultiPrintQuery(entry.getValue());
                final SelectBuilder sel = new SelectBuilder().linkto(parentAttribute).oid();
                print.addSelect(sel);
                print.executeWithoutAccessCheck();
                while (print.next()) {
                    ret.put(print.getCurrentInstance(), Instance.get(print.<String>getSelect(sel)));
                }
            }
        }
        return ret;
    }
}
