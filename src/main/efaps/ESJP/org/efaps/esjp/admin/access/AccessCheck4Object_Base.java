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
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;


/**
 * This Class is used to check if a user can Access this Object.<br>
 * The method execute is called with the Instance and the Accesstype as
 * parameters. For the instance object it is checked if the current context user
 * has the access defined in the list of access types.
 *
 * @author The eFaps Team
 * @version $Id:SimpleAccessCheckOnType.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("fa2720cc-8e76-476e-87cd-eef6eeb8b1e3")
@EFapsRevision("$Rev$")
public abstract class AccessCheck4Object_Base
    extends AccessCheckAbstract
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkAccess(final Parameter _parameter,
                                  final Instance _instance,
                                  final AccessType _accessType)
        throws EFapsException
    {
        boolean ret = false;
        //create
        if (_accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
            ret = new SimpleAccessCheckOnType().checkAccess(_parameter, _instance, _accessType);
        } else {
            final StringBuilder cmd = new StringBuilder();
            cmd.append("select count(*) ")
                .append(" from T_ACCESS4OBJ ")
                .append(" inner join T_ACCESSSET2TYPE on T_ACCESSSET2TYPE.ACCESSSET = ACCSETID ")
                .append(" where accesstype=").append(_accessType.getId())
                .append(" and TYPEID = ").append(_instance.getType().getId())
                .append(" and OBJID =").append(_instance.getId());

            final Context context = Context.getThreadContext();
            cmd.append(" and PERSID in (").append(context.getPersonId());
            for (final Role role : context.getPerson().getRoles()) {
                cmd.append(",").append(role.getId());
            }
            for (final Group group : context.getPerson().getGroups()) {
                cmd.append(",").append(group.getId());
            }
            cmd.append(")");


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
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<Instance, Boolean> checkAccess(final Parameter _parameter,
                                                 final List<?> _instances,
                                                 final AccessType _accessType)
        throws EFapsException
    {
        Map<Instance, Boolean> ret = new HashMap<Instance, Boolean>();
         //create
        if (_accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
            ret = new SimpleAccessCheckOnType().checkAccess(_parameter, _instances, _accessType);
        } else {
            final StringBuilder cmd = new StringBuilder();
            final Map<Long, List<Long>> typeid2objectids = new HashMap<Long, List<Long>>();
            for (final Object instance : _instances) {
                final Instance inst = (Instance) instance;
                if (inst != null && inst.isValid()) {
                    List<Long> ids;
                    if (typeid2objectids.containsKey(inst.getType().getId())) {
                        ids = typeid2objectids.get(inst.getType().getId());
                    } else {
                        ids = new ArrayList<Long>();
                        typeid2objectids.put(inst.getType().getId(), ids);
                    }
                    ids.add(inst.getId());
                }
            }
            cmd.append("select TYPEID, OBJID ")
                .append(" from T_ACCESS4OBJ ")
                .append(" inner join T_ACCESSSET2TYPE on T_ACCESSSET2TYPE.ACCESSSET = ACCSETID ")
                .append(" where (accesstype=").append(_accessType.getId()).append(") and (");
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
            final Context context = Context.getThreadContext();
            cmd.append(") and PERSID in (").append(context.getPersonId());
            for (final Role role : context.getPerson().getRoles()) {
                cmd.append(",").append(role.getId());
            }
            for (final Group group : context.getPerson().getGroups()) {
                cmd.append(",").append(group.getId());
            }
            cmd.append(")");
            final Set<Instance> instan = new HashSet<Instance>();
            ConnectionResource con = null;
            try {
                con = context.getConnectionResource();
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
                for (final Object inst : _instances) {
                    ret.put((Instance) inst, instan.contains(inst));
                }
            }
        }
        return ret;
    }

    public Return access4ObjectMultiprint(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final List<Instance> instances = new ArrayList<Instance>();
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(UUID
                                                        .fromString("98d9b606-b1aa-4ae1-9f30-2cba0d99453b")));
        queryBldr.addWhereAttrEqValue("TypeId", _parameter.getInstance().getType().getId());
        queryBldr.addWhereAttrEqValue("ObjectId", _parameter.getInstance().getId());
        final InstanceQuery multi = queryBldr.getQuery();
        multi.execute();
        while (multi.next()) {
            instances.add(multi.getCurrentValue());
        }
        ret.put(ReturnValues.VALUES, instances);
        return ret;
    }
}
