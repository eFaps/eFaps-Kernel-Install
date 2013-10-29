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
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ef7dcf84-99ec-4caf-8422-513bfb7eab39")
@EFapsRevision("$Rev$")
public abstract class Linked2ObjectAccessCheck_Base
    extends AbstractAccessCheck
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
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String middleTypeUUID = (String) properties.get("MiddleTypeUUID");
        final String fromAttributeName = (String) properties.get("FromAttribute");
        final String toAttributeName = (String) properties.get("ToAttribute");
        final Type type = Type.get(UUID.fromString(middleTypeUUID));
        final Attribute fromAttribute = type.getAttribute(fromAttributeName);
        final Attribute toAttribute = type.getAttribute(toAttributeName);
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select ").append(toAttribute.getSqlColNames().get(0))
            .append(" from ").append(type.getMainTable().getSqlTable())
            .append(" where ").append(fromAttribute.getSqlColNames().get(0)).append("=").append(_instance.getId());

        if (type.getMainTable().getSqlColType() != null) {
            cmd.append(" and ").append(type.getMainTable().getSqlColType()).append("=").append(type.getId());
        }
        long id = 0;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            AbstractAccessCheck_Base.LOG.debug("Checking access with: {}", cmd);
            Statement stmt = null;
            try {
                stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                if (rs.next()) {
                    id = rs.getLong(1);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            AbstractAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
        }
        if (id > 0) {
            final Instance instance = Instance.get(toAttribute.getLink(), id);
            final SimpleAccessCheckOnType accessCheck = new SimpleAccessCheckOnType();
            if (accessCheck.checkAccess(_parameter, instance, AccessTypeEnums.READ.getAccessType())) {
                ret = accessCheck.checkAccess(_parameter, _instance, _accessType);
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
        final Map<Instance, Boolean> ret = new HashMap<Instance, Boolean>();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String middleTypeUUID = (String) properties.get("MiddleTypeUUID");
        final String fromAttributeName = (String) properties.get("FromAttribute");
        final String toAttributeName = (String) properties.get("ToAttribute");
        final Type type = Type.get(UUID.fromString(middleTypeUUID));
        final Attribute fromAttribute = type.getAttribute(fromAttributeName);
        final Attribute toAttribute = type.getAttribute(toAttributeName);
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select ").append(fromAttribute.getSqlColNames().get(0))
                .append(", ").append(toAttribute.getSqlColNames().get(0))
            .append(" from ").append(type.getMainTable().getSqlTable())
            .append(" where ");
        if (type.getMainTable().getSqlColType() != null) {
            cmd.append(type.getMainTable().getSqlColType()).append("=").append(type.getId()).append(" and ");
        }
        cmd.append(fromAttribute.getSqlColNames().get(0)).append(" in (");
        for (final Object instObj : _instances) {
            cmd.append(((Instance) instObj).getId()).append(",");
        }
        cmd.append("0)");
        final Map<Long, Set<Long>> relMap = new HashMap<Long, Set<Long>>();
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            AbstractAccessCheck_Base.LOG.debug("Checking access with: {}", cmd);
            Statement stmt = null;
            try {
                stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                while (rs.next()) {
                    final long fromId = rs.getLong(1);
                    final long toId = rs.getLong(2);
                    Set<Long> froms;
                    if (relMap.containsKey(toId)) {
                        froms = relMap.get(toId);
                    } else {
                        froms = new HashSet<Long>();
                        relMap.put(toId, froms);
                    }
                    froms.add(fromId);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            AbstractAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
        }
        final SimpleAccessCheckOnType accessCheck = new SimpleAccessCheckOnType();

        for (final Entry<Long, Set<Long>> entry : relMap.entrySet()) {
            final Instance instance = Instance.get(toAttribute.getLink(), entry.getKey());
            final List<Instance>tmpInsts = new ArrayList<Instance>();
            for (final Object instObj : _instances) {
                if (entry.getValue().contains(((Instance) instObj).getId())) {
                    tmpInsts.add((Instance) instObj);
                }
            }
            if (accessCheck.checkAccess(_parameter, instance, AccessTypeEnums.READ.getAccessType())) {
                ret.putAll(accessCheck.checkAccess(_parameter, tmpInsts, _accessType));
            } else {
                for (final Instance instTmp : tmpInsts) {
                    ret.put(instTmp, false);
                }
            }
        }
        return ret;
    }
}
