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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
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
        // TODO Auto-generated method stub
        return false;
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
        final StringBuilder cmd = new StringBuilder();
        final Map<Long, List<Long>> typeid2objectids = new HashMap<Long, List<Long>>();
        for (final Object instance : _instances) {
            final Instance inst = ((Instance) instance);
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
        cmd.append("select TYPEID, OBJID, ACCSETID ")
            .append(" from T_ACCESS4OBJ WHERE ");
        boolean first = true;
        for (final Entry<Long, List<Long>>entry : typeid2objectids.entrySet()) {
            if (first) {
                first = false;
            } else {
                cmd.append(" OR ");
            }
            cmd.append(" TYPEID = ").append(entry.getKey()).append(" and OBJID in (");
            boolean firstID = true;
            for (final Long id : entry.getValue()) {
                if (firstID) {
                    firstID = false;
                } else {
                    cmd.append(", ");
                }
                cmd.append(id);
            }
            cmd.append(") ");
        }
        final Context context = Context.getThreadContext();
        cmd.append(" and PERSID in (").append(context.getPersonId());
        for (final Role role : context.getPerson().getRoles()) {
            cmd.append(",").append(role.getId());
        }
        for (final Group group : context.getPerson().getGroups()) {
            cmd.append(",").append(group.getId());
        }
        cmd.append(")");
        return ret;
    }
}
