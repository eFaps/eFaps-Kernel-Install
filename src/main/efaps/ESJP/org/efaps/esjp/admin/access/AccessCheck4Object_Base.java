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
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
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
        if (check4SimpleAccessCheck(_parameter, _instance)) {
            ret = getSimpleAccess4Type(_parameter).checkAccess(_parameter, _instance, _accessType);
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
        final Map<Instance, Boolean> ret = new HashMap<Instance, Boolean>();
        final List<Instance> simpleAccess = new ArrayList<Instance>();
        final List<Instance> objectAccess = new ArrayList<Instance>();

        for (final Object instance : _instances) {
            final Instance inst = (Instance) instance;
            if (check4SimpleAccessCheck(_parameter, inst)) {
                simpleAccess.add(inst);
            } else {
                objectAccess.add(inst);
            }

        }
        if (!simpleAccess.isEmpty()) {
            ret.putAll(getSimpleAccess4Type(_parameter).checkAccess(_parameter, simpleAccess, _accessType));
        }
        if (!objectAccess.isEmpty()) {
            final StringBuilder cmd = new StringBuilder();
            final Map<Long, List<Long>> typeid2objectids = new HashMap<Long, List<Long>>();
            for (final Object instance : objectAccess) {
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

    protected boolean check4SimpleAccessCheck(final Parameter _parameter,
                                              final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
        //create
        if (accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
            ret = true;
        } else {
            final Properties props = getProperties(_parameter);
            final String persStr = props.getProperty(_instance.getType().getName() + ".Person.SimpleAccess4Type");
            final String roleStr = props.getProperty(_instance.getType().getName() + ".Role.SimpleAccess4Type");
            final String groupStr = props.getProperty(_instance.getType().getName() + ".Group.SimpleAccess4Type");
            if (persStr != null) {
                for (final String pers : persStr.split(";")) {
                    final Person person = Person.get(pers);
                    if (person != null && Context.getThreadContext().getPerson().hasChildPerson(person)) {
                        ret = true;
                        break;
                    }
                }
            } else if (roleStr != null) {
                for (final String pers : roleStr.split(";")) {
                    final Role role = Role.get(pers);
                    if (role != null && Context.getThreadContext().getPerson().isAssigned(role)) {
                        ret = true;
                        break;
                    }
                }
            } else if (groupStr != null) {
                for (final String pers : groupStr.split(";")) {
                    final Group group = Group.get(pers);
                    if (group != null && Context.getThreadContext().getPerson().isAssigned(group)) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }


    protected AccessCheckAbstract getSimpleAccess4Type(final Parameter _parameter)
    {
        return new SimpleAccessCheckOnType();
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Instance instance = _parameter.getCallInstance();

        final Properties props = getProperties(_parameter);
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
        final Properties props = getProperties(_parameter);
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


    public Return deleteOverrideTrigger(final Parameter _parameter)
        throws EFapsException
    {
        final Properties props = getProperties(_parameter);
        boolean delete = false;
        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        print.addAttribute("ObjectId", "TypeId");
        print.execute();
        final Long typeId = print.<Long>getAttribute("TypeId");
        final Long objectId = print.<Long>getAttribute("ObjectId");

        final String pers = props.getProperty(Type.get(typeId).getName() + ".Person.Default4Delete");
        final String grps = props.getProperty(Type.get(typeId).getName() + ".Group.Default4Delete");
        final String roles = props.getProperty(Type.get(typeId).getName() + ".Role.Default4Delete");

        if ((pers != null && !pers.isEmpty()) || (grps != null && !grps.isEmpty())
                        || (roles != null && !roles.isEmpty())) {
            final QueryBuilder queryBldr = new QueryBuilder(Type.get(UUID
                            .fromString("98d9b606-b1aa-4ae1-9f30-2cba0d99453b")));
            queryBldr.addWhereAttrEqValue("TypeId", typeId);
            queryBldr.addWhereAttrEqValue("ObjectId", objectId);
            final InstanceQuery query = queryBldr.getQuery();
            final List<Instance> list = query.execute();

            if (list.size() < 2) {
                insertOnDelete(_parameter, pers, Person.class, typeId, objectId);
                insertOnDelete(_parameter, grps, Group.class, typeId, objectId);
                insertOnDelete(_parameter, roles, Role.class, typeId, objectId);
            } else {
                delete = true;
            }
        } else {
            delete = true;
        }
        if (delete) {
            new Delete(_parameter.getInstance()).executeWithoutTrigger();
        }
        return new Return();
    }


    /**
     * @param _parameter
     * @param _class
     * @param _typeId
     * @param _objectId
     * @throws EFapsException
     */
    protected void insertOnDelete(final Parameter _parameter,
                                     final String _pers,
                                     final Class<?> _class,
                                     final Long _typeId,
                                     final Long _objectId) throws EFapsException
    {
        boolean del = false;
        if (_pers != null && !_pers.isEmpty()) {
            final String[] perAr = _pers.split(";");
            for (int i = 0; i < perAr.length; i = i + 2) {
                Long persId = null;
                if (_class.isAssignableFrom(Person.class)) {
                    final Person aPers = Person.get(perAr[i]);
                    if (aPers != null) {
                        persId = aPers.getId();
                    }
                } else if (_class.isAssignableFrom(Group.class)) {
                    final Group aPers = Group.get(perAr[i]);
                    if (aPers != null) {
                        persId = aPers.getId();
                    }
                } else if (_class.isAssignableFrom(Role.class)) {
                    final Role aPers = Role.get(perAr[i]);
                    if (aPers != null) {
                        persId = aPers.getId();
                    }
                }
                final AccessSet accSet = AccessSet.getAccessSet(perAr[i + 1]);
                if (persId != null && accSet != null) {
                    final QueryBuilder queryBldr = new QueryBuilder(Type.get(UUID
                                    .fromString("98d9b606-b1aa-4ae1-9f30-2cba0d99453b")));
                    queryBldr.addWhereAttrEqValue("TypeId", _typeId);
                    queryBldr.addWhereAttrEqValue("ObjectId", _objectId);
                    queryBldr.addWhereAttrEqValue("PersonLink", persId);
                    queryBldr.addWhereAttrEqValue("AccessSetLink", accSet.getId());
                    final InstanceQuery query = queryBldr.getQuery();
                    if (query.execute().isEmpty()) {
                        insertAccess4Object(_parameter, Instance.get(Type.get(_typeId), _objectId), persId,
                                        accSet.getId());
                        del = true;
                    }
                } else {
                    del = true;
                }
            }
        }
        if (del) {
            new Delete(_parameter.getInstance()).executeWithoutTrigger();
        }
    }

    public Instance insertAccess4Object(final Parameter _parameter,
                                        final Instance _instance,
                                        final Long _persId,
                                        final Long _accSetId)
        throws EFapsException
    {
        Instance ret = null;
        if (_instance != null && _instance.isValid() && _persId != null && _accSetId != null) {
            final Insert insert = new Insert(Type.get(UUID
                            .fromString("98d9b606-b1aa-4ae1-9f30-2cba0d99453b")));
            insert.add("TypeId", _instance.getType().getId());
            insert.add("ObjectId", _instance.getId());
            insert.add("PersonLink", _persId);
            insert.add("AccessSetLink", _accSetId);
            insert.execute();
            ret = insert.getInstance();
        }
        return ret;
    }

    protected Properties getProperties(final Parameter _parameter)
        throws EFapsException
    {
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final Properties props = new Properties();
        for (int i = 0; i < 100; i++) {
            final Properties propsi = config.getAttributeValueAsProperties("AccessCheck4Object" + i);
            if (propsi == null || propsi.isEmpty()) {
                break;
            } else {
                props.putAll(propsi);
            }
        }
        return props;
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