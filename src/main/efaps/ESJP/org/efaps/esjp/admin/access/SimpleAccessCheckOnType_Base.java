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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * This Class is used to check if a user can Access this Type.<br>
 * The method execute is called with the Instance and the Accesstype as
 * parameters. For the instance object it is checked if the current context user
 * has the access defined in the list of access types.
 *
 * @author The eFaps Team
 * @version $Id:SimpleAccessCheckOnType.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("628a19f6-463f-415d-865b-ba72e303a507")
@EFapsRevision("$Rev$")
public abstract class SimpleAccessCheckOnType_Base
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
        final Context context = Context.getThreadContext();

        final StringBuilder cmd = new StringBuilder();
        cmd.append("select count(*) from T_ACCESSSET2USER ");
        Type type;
        if (_parameter.get(ParameterValues.CLASS) instanceof Classification) {
            type = (Classification) _parameter.get(ParameterValues.CLASS);
        } else {
            type = _instance.getType();
        }
        final Set<Long> users = new HashSet<Long>();
        final Set<Role> localRoles = new HashSet<Role>();

        if (type.isCheckStatus() && !_accessType.equals(AccessTypeEnums.CREATE.getAccessType())) {
            cmd.append(" join T_ACCESSSET2STATUS on T_ACCESSSET2USER.ACCESSSET = T_ACCESSSET2STATUS.ACCESSSET")
                .append(" join ").append(type.getMainTable().getSqlTable())
                .append(" on ").append(type.getMainTable().getSqlTable()).append(".")
                .append(type.getStatusAttribute().getSqlColNames().get(0))
                .append("=T_ACCESSSET2STATUS.ACCESSSTATUS");
        }

        cmd.append(" where T_ACCESSSET2USER.ACCESSSET in (0");
        for (final AccessSet accessSet : type.getAccessSets()) {
            if (accessSet.getAccessTypes().contains(_accessType)) {
                cmd.append(",").append(accessSet.getId());
                users.addAll(accessSet.getUserIds());
            }
        }
        cmd.append(") ").append("and T_ACCESSSET2USER.USERABSTRACT in (").append(context.getPersonId());
        for (final Long roleId : context.getPerson().getRoles()) {
            if (users.contains(roleId)) {
                cmd.append(",").append(roleId);
                final Role role = Role.get(roleId);
                if (role.isLocal()) {
                    localRoles.add(role);
                }
            }
        }
        cmd.append(")");
        if (type.isCheckStatus() && !_accessType.equals(AccessTypeEnums.CREATE.getAccessType())) {
            cmd.append(" and ").append(type.getMainTable().getSqlTable()).append(".ID=").append(_instance.getId());
        }

        if (type.isGroupDepended() && !_accessType.equals(AccessTypeEnums.CREATE.getAccessType())
                        && !localRoles.isEmpty()
                        && EFapsSystemConfiguration.KERNEL.get().getAttributeValueAsBoolean(
                                        KernelSettings.ACTIVATE_GROUPS)) {
            cmd.append(" and ").append(type.getMainTable().getSqlTable()).append(".")
                            .append(type.getGroupAttribute().getSqlColNames().get(0)).append(" in (")
                            .append(" select GROUPID from T_USERASSOC where ROLEID in (");
            boolean first = true;
            for (final Role role : localRoles) {
                if (first) {
                    first = false;
                } else {
                    cmd.append(",");
                }
                cmd.append(role.getId());
            }
            cmd.append(") and GROUPID in (");
            first = true;
            for (final Long group : context.getPerson().getGroups()) {
                if (first) {
                    first = false;
                } else {
                    cmd.append(",");
                }
                cmd.append(group);
            }
            cmd.append("))");
        }
        AccessCheckAbstract_Base.LOG.debug("cheking access with: {}", cmd);
        return executeStatement(_parameter, context, cmd);
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
        final Map<Instance, Boolean> accessMap = new HashMap<Instance, Boolean>();
        final Context context = Context.getThreadContext();

        final Type type = ((Instance) _instances.get(0)).getType();
        if (type.isCheckStatus()) {
            final Set<Long> users = new HashSet<Long>();
            final Set<Role> localRoles = new HashSet<Role>();
            final StringBuilder cmd = new StringBuilder();
            cmd.append("select ").append(type.getMainTable().getSqlTable()).append(".ID ")
                .append(" from T_ACCESSSET2USER ")
                .append(" join T_ACCESSSET2STATUS on T_ACCESSSET2USER.ACCESSSET = T_ACCESSSET2STATUS.ACCESSSET")
                .append(" join ").append(type.getMainTable().getSqlTable()).append(" on ")
                .append(type.getMainTable().getSqlTable()).append(".")
                .append(type.getStatusAttribute().getSqlColNames().get(0))
                .append("=T_ACCESSSET2STATUS.ACCESSSTATUS")
                .append(" where T_ACCESSSET2USER.ACCESSSET in (0");
            for (final AccessSet accessSet : type.getAccessSets()) {
                if (accessSet.getAccessTypes().contains(_accessType)) {
                    cmd.append(",").append(accessSet.getId());
                    users.addAll(accessSet.getUserIds());
                }
            }
            cmd.append(") ").append("and T_ACCESSSET2USER.USERABSTRACT in (").append(context.getPersonId());
            for (final Long roleId : context.getPerson().getRoles()) {
                if (users.contains(roleId)) {
                    cmd.append(",").append(roleId);
                    final Role role = Role.get(roleId);
                    if (role.isLocal()) {
                        localRoles.add(role);
                    }
                }
            }
            cmd.append(")");
            // add the check for groups if: the type is group depended, a local
            // role is defined for the user, the group mechanism is activated
            if (type.isGroupDepended() && !localRoles.isEmpty()
                            && EFapsSystemConfiguration.KERNEL.get().getAttributeValueAsBoolean(
                                            KernelSettings.ACTIVATE_GROUPS)) {
                cmd.append(" and ").append(type.getMainTable().getSqlTable()).append(".")
                                .append(type.getGroupAttribute().getSqlColNames().get(0)).append(" in (")
                                .append(" select GROUPID from T_USERASSOC where ROLEID in (");
                boolean first = true;
                for (final Role role : localRoles) {
                    if (first) {
                        first = false;
                    } else {
                        cmd.append(",");
                    }
                    cmd.append(role.getId());
                }
                cmd.append(") and GROUPID in (");
                first = true;
                for (final Long group : context.getPerson().getGroups()) {
                    if (first) {
                        first = false;
                    } else {
                        cmd.append(",");
                    }
                    cmd.append(group);
                }
                cmd.append("))");
            }

            final Set<Long> idList = new HashSet<Long>();

            ConnectionResource con = null;
            try {
                con = context.getConnectionResource();

                Statement stmt = null;
                try {
                    AccessCheckAbstract_Base.LOG.debug("Checking access with: {}", cmd);
                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(cmd.toString());

                    while (rs.next()) {
                        idList.add(rs.getLong(1));
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
                    accessMap.put((Instance) inst, idList.contains(((Instance) inst).getId()));
                }
            }
        } else {
            final boolean access = checkAccess(_parameter, (Instance) _instances.get(0), _accessType);
            for (final Object inst : _instances) {
                accessMap.put((Instance) inst, access);
            }
        }
        return accessMap;
    }


    /**
     * Method that queries against the database.
     * @param _parameter Parameter as passed by the eFasp API
     * @param _context Context
     * @param _cmd cmd
     * @return true if access granted else false
     * @throws EFapsException on error
     */
    protected boolean executeStatement(final Parameter _parameter,
                                       final Context _context,
                                       final StringBuilder _cmd)
        throws EFapsException
    {
        boolean hasAccess = false;

        ConnectionResource con = null;
        try {
            con = _context.getConnectionResource();
            AccessCheckAbstract_Base.LOG.debug("Checking access with: {}", _cmd);
            Statement stmt = null;
            try {
                stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(_cmd.toString());
                if (rs.next()) {
                    hasAccess = (rs.getLong(1) > 0) ? true : false;
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            AccessCheckAbstract_Base.LOG.error("sql statement '" + _cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
        }
        return hasAccess;
    }
}
