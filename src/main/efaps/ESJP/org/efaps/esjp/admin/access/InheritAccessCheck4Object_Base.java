/*
 * Copyright 2003 - 2011 The eFaps Team
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

import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;


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
 * @version $Id$
 */
@EFapsUUID("6306017b-af25-4756-b19c-8ba00e51448e")
@EFapsRevision("$Rev$")
public abstract class InheritAccessCheck4Object_Base
    extends AccessCheck4Object
{
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
        // check if for this object something is defined, if not gop for the parent object
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
                stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                if (rs.next()) {
                    ret = (rs.getLong(1) > 0) ? true : false;
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            AccessCheckAbstract_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
        }
        if (ret) {
            ret = super.getObjectAccess(_parameter, _instance, _accessType);
        } else {
            final Instance parentInst = getParentInstance(_parameter, _instance, _accessType);
            if (check4SimpleAccessCheck(_parameter, parentInst)) {
                ret = getSimpleAccess4Type(_parameter).checkAccess(_parameter, parentInst, _accessType);
            } else {
                ret = getObjectAccess(_parameter, parentInst, _accessType);
            }
        }
        return ret;
    }

    /**
     * @param _parameter
     * @param _instance
     * @param _accessType
     * @return
     * @throws EFapsException
     */
    protected Instance getParentInstance(final Parameter _parameter,
                                       final Instance _instance,
                                       final AccessType _accessType)
        throws EFapsException
    {
        Instance ret = Instance.get("");
        final Properties props = getProperties(_parameter);
        final String parentAttribute = props.getProperty(_instance.getType().getName() + ".ParentAttribute");
        if (parentAttribute != null)
        {
            final PrintQuery print = new PrintQuery(_instance);
            final SelectBuilder sel = new SelectBuilder().linkto(parentAttribute).oid();
            print.addSelect(sel);
            if (print.executeWithoutAccessCheck()) {
                ret = Instance.get(print.<String>getSelect(sel));
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
        final Map<Instance, Boolean> ret = new HashMap<Instance, Boolean>();
        final StringBuilder cmd = new StringBuilder();
        final Map<Long, List<Long>> typeid2objectids = new HashMap<Long, List<Long>>();
        for (final Instance instance : _instances) {
            if (instance != null && instance.isValid()) {
                List<Long> ids;
                if (typeid2objectids.containsKey(instance.getType().getId())) {
                    ids = typeid2objectids.get(instance.getType().getId());
                } else {
                    ids = new ArrayList<Long>();
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
        final Set<Instance> instan = new HashSet<Instance>();
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            Statement stmt = null;
            try {
                stmt = con.getConnection().createStatement();
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
            con.commit();
        } catch (final SQLException e) {
            AccessCheckAbstract_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
            final List<Instance> accessInst = new ArrayList<Instance>();
            final List<Instance> inheritInst = new ArrayList<Instance>();
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

            final List<Instance> simpleAccess = new ArrayList<Instance>();
            final List<Instance> objectAccess = new ArrayList<Instance>();
            final Map<Instance, Instance> parentInsts = getParentInstances(_parameter, inheritInst, _accessType);
            for (final Instance instance : parentInsts.values()) {
                if (check4SimpleAccessCheck(_parameter, instance)) {
                    simpleAccess.add(instance);
                } else {
                    objectAccess.add(instance);
                }
            }
            final Map<Instance, Boolean> tmp = new HashMap<Instance, Boolean>();
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
     * @param _parameter
     * @param _instance
     * @param _accessType
     * @return
     * @throws EFapsException
     */
    protected Map<Instance, Instance> getParentInstances(final Parameter _parameter,
                                                         final List<Instance> _instances,
                                                         final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Instance> ret = new HashMap<Instance, Instance>();
        final Properties props = getProperties(_parameter);
        final Map<Type, List<Instance>> type2instance = new HashMap<Type, List<Instance>>();
        for (final Instance instance : _instances) {
            if (instance != null && instance.isValid()) {
                List<Instance> instances;
                if (type2instance.containsKey(instance.getType())) {
                    instances = type2instance.get(instance.getType());
                } else {
                    instances = new ArrayList<Instance>();
                    type2instance.put(instance.getType(), instances);
                }
                instances.add(instance);
            }
        }

        for (final Entry<Type, List<Instance>> entry : type2instance.entrySet()) {
            final String parentAttribute = props.getProperty(entry.getKey().getName() + ".ParentAttribute");
            if (parentAttribute != null)
            {
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
