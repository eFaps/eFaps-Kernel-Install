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
import java.util.Collection;
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
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author The eFaps Team
 */
@EFapsUUID("ef7dcf84-99ec-4caf-8422-513bfb7eab39")
@EFapsApplication("eFaps-Kernel")
public abstract class Linked2ObjectAccessCheck_Base
    extends AbstractAccessCheck
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Linked2ObjectAccessCheck.class);

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
        // for create the accesscheck is send directly to SimpleAccessCheckOnType
        if (AccessTypeEnums.CREATE.getAccessType().equals(_accessType)) {
            final SimpleAccessCheckOnType accessCheck = new SimpleAccessCheckOnType();
            ret = accessCheck.checkAccess(_parameter, _instance, _accessType);
        } else {
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
            try {
                final ConnectionResource con = Context.getThreadContext().getConnectionResource();
               Linked2ObjectAccessCheck_Base.LOG.debug("Checking access with: {}", cmd);
                Statement stmt = null;
                try {
                    stmt = con.createStatement();
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
            } catch (final SQLException e) {
               Linked2ObjectAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
            }
            if (id > 0) {
                final Instance instance = Instance.get(toAttribute.getLink(), id);
                if (instance.getType().hasAccess(instance, AccessTypeEnums.READ.getAccessType())) {
                    final SimpleAccessCheckOnType accessCheck = new SimpleAccessCheckOnType();
                    ret = accessCheck.checkAccess(_parameter, _instance, _accessType);
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
                                                 final Collection<Instance> _instances,
                                                 final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Boolean> ret = new HashMap<>();
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
        final Map<Long, Set<Long>> relMap = new HashMap<>();
        try {
            final ConnectionResource con = Context.getThreadContext().getConnectionResource();
           Linked2ObjectAccessCheck_Base.LOG.debug("Checking access with: {}", cmd);
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                while (rs.next()) {
                    final long fromId = rs.getLong(1);
                    final long toId = rs.getLong(2);
                    final Set<Long> froms;
                    if (relMap.containsKey(toId)) {
                        froms = relMap.get(toId);
                    } else {
                        froms = new HashSet<>();
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
        } catch (final SQLException e) {
           Linked2ObjectAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        }
        final SimpleAccessCheckOnType accessCheck = new SimpleAccessCheckOnType();

        for (final Entry<Long, Set<Long>> entry : relMap.entrySet()) {
            final Instance instance = Instance.get(toAttribute.getLink(), entry.getKey());
            final List<Instance>tmpInsts = new ArrayList<>();
            for (final Object instObj : _instances) {
                if (entry.getValue().contains(((Instance) instObj).getId())) {
                    tmpInsts.add((Instance) instObj);
                }
            }
            if (instance.getType().hasAccess(instance, AccessTypeEnums.READ.getAccessType())) {
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
