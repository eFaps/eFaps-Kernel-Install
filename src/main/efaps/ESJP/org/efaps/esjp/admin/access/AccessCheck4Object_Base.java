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
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.common.uiform.Create;
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

    /**
     * @return new Access4ObjectCreate()
     */
    protected Create getCreate()
    {
        return new Access4ObjectCreate();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing
     * @throws EFapsException on error
     */
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

    /**
     * @param _parameter as passed from eFaps API.
     * @return Create
     * @throws EFapsException on error.
     */
    public Return access4ObjectCreate(final Parameter _parameter)
        throws EFapsException
    {
        return getCreate().execute(_parameter);
    }


    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getAccessSetDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Instance instance = _parameter.getCallInstance();

        final Properties props = config.getAttributeValueAsProperties("AccessCheck4Object");
        String accessSets = null;
        final Map<String, Long> values = new TreeMap<String, Long>();
        if (props != null && instance != null && instance.isValid()) {
            accessSets = props.getProperty(instance.getType().getName() + ".AccessSets");
        }
        if (accessSets != null) {
            for (final String accessSet :  accessSets.split(";")) {
                final AccessSet set = AccessSet.getAccessSet(accessSet);
                if (set != null) {
                    values.put(set.getName(), set.getId());
                }
            }
        } else {
            //Admin_Access_AccessSet
            final QueryBuilder queryBldr = new QueryBuilder(Type.get(
                            UUID.fromString("40aa4ff1-4786-4169-9a34-b6fd9d8a75f1")));
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute("Name");
            multi.execute();
            while (multi.next()) {
                values.put(multi.<String>getAttribute("Name"), multi.getCurrentInstance().getId());
            }
        }

        html.append("<select name=\"").append(fieldValue.getField().getName()).append("\" size=\"1\">");
        for (final Entry<String, Long> entry : values.entrySet()) {
            html.append("<option value=\"").append(entry.getValue()).append("\" ");
            if (entry.getValue().equals(fieldValue.getValue())) {
                html.append("selected=\"selected\"");
            }
            html.append("/>").append(entry.getKey()).append("</option>");
        }
        html.append("</select>");
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return autoComplete4Person(final Parameter _parameter)
        throws EFapsException
    {
        final String input = (String) _parameter.get(ParameterValues.OTHERS);

        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        final Map<String, Map<String, String>> tmpMap = new TreeMap<String, Map<String, String>>();
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final Properties props = config.getAttributeValueAsProperties("AccessCheck4Object");
        final Instance instance = _parameter.getInstance();
        String persInRoles = null;
        String rolesAsList = null;
        if (props != null && instance != null && instance.isValid()) {
            persInRoles = props.getProperty(instance.getType().getName() + ".Person.InRole");
            rolesAsList = props.getProperty(instance.getType().getName() + ".Role.AsList");
        }

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person);

        if (persInRoles != null) {
            final List<Object>tmp = new ArrayList<Object>();
            for (final String role : persInRoles.split(";")) {
                final Role aType = Role.get(role);
                if (aType != null) {
                    tmp.add(aType.getId());
                }
            }
            final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminUser.Person2Role);
            attrQueryBldr.addWhereAttrEqValue(CIAdminUser.Person2Role.UserToLink , tmp.toArray());
            final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery(CIAdminUser.Person2Role.UserFromLink);
            queryBldr.addWhereAttrInQuery(CIAdminUser.Abstract.ID, attrQuery);
        }
        queryBldr.addWhereAttrMatchValue(CIAdminUser.Abstract.Name, input + "*").setIgnoreCase(true);
        queryBldr.addWhereAttrEqValue(CIAdminUser.Abstract.Status, true);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminUser.Abstract.Name);
        multi.execute();
        while (multi.next()) {
            final String name = multi.<String>getAttribute(CIAdminUser.Abstract.Name);
            final Map<String, String> map = new HashMap<String, String>();
            map.put("eFapsAutoCompleteKEY", ((Long) multi.getCurrentInstance().getId()).toString());
            map.put("eFapsAutoCompleteVALUE", name);
            map.put("eFapsAutoCompleteCHOICE", name + " - " + multi.getCurrentInstance().getType().getLabel());
            tmpMap.put(name, map);
        }
        if (rolesAsList != null) {
            for (final String roleName : rolesAsList.split(";")) {
                final Role role = Role.get(roleName);
                if (role != null) {
                    final Map<String, String> map = new HashMap<String, String>();
                    map.put("eFapsAutoCompleteKEY", ((Long) role.getId()).toString());
                    map.put("eFapsAutoCompleteVALUE", role.getName());
                    //"Admin_User_Role"
                    map.put("eFapsAutoCompleteCHOICE", role.getName() + " - "
                                    + Type.get(UUID.fromString("e4d6ecbe-f198-4f84-aa69-5a9fd3165112")).getLabel());
                    tmpMap.put(role.getName(), map);
                }
            }
        }
        list.addAll(tmpMap.values());
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, list);
        return retVal;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing
     * @throws EFapsException on error
     */
    public Return checkAccess4Grant(final Parameter _parameter)
        throws EFapsException
    {
        _parameter.put(ParameterValues.ACCESSTYPE,
                        AccessType.getAccessType(UUID.fromString("89362f2b-7a93-4133-8262-0b2925e285cb")));
        return new SimpleAccessCheckOnType().execute(_parameter);
    }

    /**
     * Create class.
     */
    public class Access4ObjectCreate
        extends Create
    {
        @Override
        protected void add2basicInsert(final Parameter _parameter,
                                       final Insert _insert)
            throws EFapsException
        {
            final Instance instObject = _parameter.getInstance();

            _insert.add("TypeId", instObject.getType().getId());
            _insert.add("ObjectId", instObject.getId());
        }
    }
}
