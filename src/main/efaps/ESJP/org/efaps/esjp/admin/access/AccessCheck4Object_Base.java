/*
 * Copyright 2003 - 2016 The eFaps Team
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminAccess;
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
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.esjp.common.properties.PropertiesUtil;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;


/**
 * This Class is used to check if a user can Access this Object.<br>
 * The method execute is called with the Instance and the Accesstype as
 * parameters. For the instance object it is checked if the current context user
 * has the access defined in the list of access types.<br/>
 * To function the Attribute "AccessCheck4Object" + n (0-100) must be added to the
 * kernel SystemConfiguration containing a PropertyMap.
 * <table>
 * <tr>
 * <th>Property</th><th>Value</th><th>Description</th>
 * </tr>
 * <tr>
 * <td>[TYPENAME].Person.InRole</td><td>List of ";"-separated Roles </td>
 * <td>The Roles that must be assigned to a Person to be be shown in the dropdown
 * used to assign an access.</td>
 * </tr>
 * <tr>
 * <td>[TYPENAME].Roles.AsList</td><td>List of ";"-separated Roles </td>
 * <td>The Roles that will be shown in the dropdown used to assign an access.</td>
 * </tr>
 * <tr>
 * <td>[TYPENAME].Person.SimpleAccess4Type</td><td>List of ";"-separated Persons </td>
 * <td>The Persons for whom the simple access will always be applied.</td>
 * </tr>
 * <tr>
 * <td>[TYPENAME].Role.SimpleAccess4Type</td><td>List of ";"-separated Roles </td>
 * <td>The Role for which the simple access will always be applied.</td>
 * </tr>
 * <tr>
 * <td>[TYPENAME].Group.SimpleAccess4Type</td><td>List of ";"-separated Groups </td>
 * <td>The Groups for which the simple access will always be applied.</td>
 * </tr>
 * </table>
 *
 *
 * @author The eFaps Team
 * @version $Id:SimpleAccessCheckOnType.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("fa2720cc-8e76-476e-87cd-eef6eeb8b1e3")
@EFapsApplication("eFaps-Kernel")
public abstract class AccessCheck4Object_Base
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
        //create
        if (check4SimpleAccessCheck(_parameter, _instance)) {
            ret = getSimpleAccess4Type(_parameter).checkAccess(_parameter, _instance, _accessType);
        } else {
            ret = getObjectAccess(_parameter, _instance, _accessType);
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instance     Instance of the object the access is checked on
     * @param _accessType   type of access
     * @return true if access, else false
     * @throws EFapsException on error
     */
    protected boolean getObjectAccess(final Parameter _parameter,
                                      final Instance _instance,
                                      final AccessType _accessType)
        throws EFapsException
    {
        boolean ret = false;
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select count(*) ")
            .append(" from T_ACCESS4OBJ ")
            .append(" inner join T_ACCESSSET2TYPE on T_ACCESSSET2TYPE.ACCESSSET = ACCSETID ")
            .append(" where accesstype=").append(_accessType.getId())
            .append(" and TYPEID = ").append(_instance.getType().getId())
            .append(" and OBJID =").append(_instance.getId());

        final Context context = Context.getThreadContext();
        cmd.append(" and PERSID in (").append(context.getPersonId());
        for (final Long role : context.getPerson().getRoles()) {
            cmd.append(",").append(role);
        }
        for (final Long group : context.getPerson().getGroups()) {
            cmd.append(",").append(group);
        }
        cmd.append(")");

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
            AbstractAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
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
        final Map<Instance, Boolean> ret = new HashMap<>();
        final List<Instance> simpleAccess = new ArrayList<>();
        final List<Instance> objectAccess = new ArrayList<>();

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
            ret.putAll(getObjectAccess(_parameter, objectAccess, _accessType));
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instances    Instances to be checked
     * @param _accessType   access type
     * @return Mapping of access rights
     * @throws EFapsException on error
     */
    protected Map<Instance, Boolean> getObjectAccess(final Parameter _parameter,
                                                     final List<Instance> _instances,
                                                     final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Boolean> ret = new HashMap<>();
        final StringBuilder cmd = new StringBuilder();
        final Map<Long, List<Long>> typeid2objectids = new HashMap<>();
        for (final Object instance : _instances) {
            final Instance inst = (Instance) instance;
            if (inst != null && inst.isValid()) {
                final List<Long> ids;
                if (typeid2objectids.containsKey(inst.getType().getId())) {
                    ids = typeid2objectids.get(inst.getType().getId());
                } else {
                    ids = new ArrayList<>();
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
        for (final Long role : context.getPerson().getRoles()) {
            cmd.append(",").append(role);
        }
        for (final Long group : context.getPerson().getGroups()) {
            cmd.append(",").append(group);
        }
        cmd.append(")");
        final Set<Instance> instan = new HashSet<>();
        ConnectionResource con = null;
        try {
            con = context.getConnectionResource();
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
            AbstractAccessCheck_Base.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } finally {
            for (final Object inst : _instances) {
                ret.put((Instance) inst, instan.contains(inst));
            }
        }
        return ret;
    }

    /**
     * Check if the simple access must be applied.
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _instance     instance to be checked for simple access
     * @return  true if simple access else false
     * @throws EFapsException on error
     */
    protected boolean check4SimpleAccessCheck(final Parameter _parameter,
                                              final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
        // create
        if (accessType.getUUID().equals(UUID.fromString("1dd13a42-e04f-4bce-85cf-3931ae94267f"))) {
            ret = true;
        } else {
            final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                            _instance.getType().getName());

            final Map<Integer, String> users = PropertiesUtil.analyseProperty(properties, "SimpleAccess4Type.User", 0);
            for (final Entry<Integer, String> entry : users.entrySet()) {
                final AbstractUserObject userObj = getUserObject(entry.getValue());
                if (userObj != null) {
                    if (userObj instanceof Person
                                    && Context.getThreadContext().getPerson().hasChildPerson((Person) userObj)) {
                        ret = true;
                        break;
                    } else if (userObj instanceof Role
                                    && Context.getThreadContext().getPerson().isAssigned((Role) userObj)) {
                        ret = true;
                        break;
                    } else if (userObj instanceof Group
                                    && Context.getThreadContext().getPerson().isAssigned((Group) userObj)) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return the access class for simple access check.
     * @throws EFapsException on error
     */
    protected AbstractAccessCheck getSimpleAccess4Type(final Parameter _parameter)
        throws EFapsException
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
        final List<Instance> instances = new ArrayList<>();
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.Access4Object);
        queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.TypeId, _parameter.getInstance().getType().getId());
        queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.ObjectId, _parameter.getInstance().getId());
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
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final Instance instance = _parameter.getCallInstance();
        final List<DropDownPosition> values = new ArrayList<>();
        if (InstanceUtils.isValid(instance)) {
            final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                            instance.getType().getName());
            final Map<Integer, String> accessSets = PropertiesUtil.analyseProperty(properties, "Grant.AccessSet", 0);
            for (final String accessSetStr : accessSets.values()) {
                final AccessSet accessSet = getAccessSet(accessSetStr);
                if (accessSet != null) {
                    values.add(new Field().getDropDownPosition(_parameter, accessSet.getId(), accessSet.getName())
                                    .setSelected(Long.valueOf(accessSet.getId()).equals(uiValue.getObject())));
                }
            }
        }
        if (values.isEmpty()) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminAccess.AccessSet.Name);
            multi.execute();
            while (multi.next()) {
                values.add(new Field().getDropDownPosition(_parameter, multi.getCurrentInstance().getId(),
                                multi.<String>getAttribute(CIAdminAccess.AccessSet.Name))
                                .setSelected(Long.valueOf(multi.getCurrentInstance().getId())
                                                .equals(uiValue.getObject())));
            }
        }
        Collections.sort(values, new Comparator<DropDownPosition>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
            }
        });

        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
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

        final List<Map<String, String>> list = new ArrayList<>();
        final Map<String, Map<String, String>> tmpMap = new TreeMap<>();

        final Instance instance = _parameter.getCallInstance();
        if (InstanceUtils.isValid(instance)) {
            final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                            instance.getType().getName());
            final Map<Integer, String> inRoles = PropertiesUtil.analyseProperty(properties, "Grant.Person.InRole", 0);
            final Set<Long> roleIds = new HashSet<>();
            for (final String inRole : inRoles.values()) {
                final AbstractUserObject role = getUserObject(inRole);
                if (role != null && role instanceof Role) {
                    roleIds.add(role.getId());
                }
            }
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person);
            if (!roleIds.isEmpty()) {
                final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminUser.Person2Role);
                attrQueryBldr.addWhereAttrEqValue(CIAdminUser.Person2Role.UserToLink, roleIds.toArray());
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
                final Map<String, String> map = new HashMap<>();
                map.put("eFapsAutoCompleteKEY", String.valueOf(multi.getCurrentInstance().getId()));
                map.put("eFapsAutoCompleteVALUE", name);
                map.put("eFapsAutoCompleteCHOICE", name + " - " + multi.getCurrentInstance().getType().getLabel());
                tmpMap.put(name, map);
            }

            final Map<Integer, String> users = PropertiesUtil.analyseProperty(properties, "Grant.User", 0);
            for (final String user : users.values()) {
                final AbstractUserObject userObj = getUserObject(user);
                if (userObj != null) {
                    String typeLabel = "";
                    if (userObj instanceof Role) {
                        typeLabel = CIAdminUser.RoleAbstract.getType().getLabel();
                    } else if (userObj instanceof Group) {
                        typeLabel = CIAdminUser.Group.getType().getLabel();
                    } else if (userObj instanceof Person) {
                        typeLabel = CIAdminUser.Person.getType().getLabel();
                    }
                    final Map<String, String> map = new HashMap<>();
                    map.put("eFapsAutoCompleteKEY", String.valueOf(userObj.getId()));
                    map.put("eFapsAutoCompleteVALUE", userObj.getName());
                    map.put("eFapsAutoCompleteCHOICE", userObj.getName() + " - " + typeLabel);
                    tmpMap.put(userObj.getName(), map);
                }
            }
        }
        list.addAll(tmpMap.values());
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, list);
        return retVal;
    }

    /**
     * On delete of a Access4Object check if ti is permitted to delete the laste one.
     * @param _parameter    Paramater as passed by the eFasp API
     * @return  new Return
     * @throws EFapsException on error
     */
    public Return deleteOverrideTrigger(final Parameter _parameter)
        throws EFapsException
    {
        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        print.addAttribute(CIAdminAccess.Access4Object.ObjectId, CIAdminAccess.Access4Object.TypeId);
        print.execute();
        final Long typeId = print.getAttribute(CIAdminAccess.Access4Object.TypeId);
        final Long objectId = print.getAttribute(CIAdminAccess.Access4Object.ObjectId);

        final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                        Type.get(typeId).getName());
        final Map<Integer, String> users = PropertiesUtil.analyseProperty(properties, "Delete.User", 0);
        final Map<Integer, String> sets = PropertiesUtil.analyseProperty(properties, "Delete.AccessSet", 0);

        new Delete(_parameter.getInstance()).executeWithoutTrigger();

        // if a defintion exists ensure that they are set
        if (!users.isEmpty()) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.Access4Object);
            queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.TypeId, typeId);
            queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.ObjectId, objectId);
            final InstanceQuery query = queryBldr.getQuery();
            // if the deleted access was the last add the defaults
            if (query.execute().isEmpty()) {
                for (final Entry<Integer, String> entry : users.entrySet()) {
                    final AbstractUserObject userObj = getUserObject(entry.getValue());
                    final AccessSet accessSet = getAccessSet(sets.get(entry.getKey()));
                    if (userObj != null && accessSet != null) {
                        insertAccess4Object(_parameter, _parameter.getInstance(), userObj.getId(), accessSet.getId());
                    }
                }
            }
        }
        return new Return();
    }

    /**
     * Delete override trigger for object.
     * On deletion of the object remove all access4object and
     * - if a delete accessobject is defined add this one
     * - if no defined delete object
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return deleteOverrideTrigger4Object(final Parameter _parameter)
        throws EFapsException
    {

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.Access4Object);
        queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.TypeId, _parameter.getInstance().getType().getId());
        queryBldr.addWhereAttrEqValue(CIAdminAccess.Access4Object.ObjectId, _parameter.getInstance().getId());
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        while (query.next()) {
            new Delete(query.getCurrentValue()).executeWithoutTrigger();
        }

        final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                        _parameter.getInstance().getType().getName());
        final Map<Integer, String> users = PropertiesUtil.analyseProperty(properties, "Delete.User", 0);
        final Map<Integer, String> sets = PropertiesUtil.analyseProperty(properties, "Delete.AccessSet", 0);

        if (users.isEmpty()) {
            new Delete(_parameter.getInstance()).executeWithoutTrigger();
        } else {
            for (final Entry<Integer, String> entry : users.entrySet()) {
                final AbstractUserObject userObj = getUserObject(entry.getValue());
                final AccessSet accessSet = getAccessSet(sets.get(entry.getKey()));
                if (userObj != null && accessSet != null) {
                    insertAccess4Object(_parameter, _parameter.getInstance(), userObj.getId(), accessSet.getId());
                }
            }
        }
        return new Return();
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instance     instance of the object
     * @param _persId       id of the person
     * @param _accSetId     id of the accessSet
     * @return Instance
     * @throws EFapsException on error
     */
    public Instance insertAccess4Object(final Parameter _parameter,
                                        final Instance _instance,
                                        final Long _persId,
                                        final Long _accSetId)
        throws EFapsException
    {
        Instance ret = null;
        if (_instance != null && _instance.isValid() && _persId != null && _accSetId != null) {
            final Insert insert = new Insert(CIAdminAccess.Access4Object);
            insert.add(CIAdminAccess.Access4Object.TypeId, _instance.getType().getId());
            insert.add(CIAdminAccess.Access4Object.ObjectId, _instance.getId());
            insert.add(CIAdminAccess.Access4Object.PersonLink, _persId);
            insert.add(CIAdminAccess.Access4Object.AccessSetLink, _accSetId);
            insert.execute();
            ret = insert.getInstance();
        }
        return ret;
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @return  Properties
     * @throws EFapsException on error
     */
    protected Properties getProperties(final Parameter _parameter)
        throws EFapsException
    {
        return KernelConfigurations.ACCESS4OBJECT.get();
    }

    /**
     * Insert access for object trigger. Shoul be executed on after
     * insert of the object.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return insertPostTrigger4Object(final Parameter _parameter)
        throws EFapsException
    {
        final Instance objInst = _parameter.getInstance();
        final Properties properties = PropertiesUtil.getProperties4Prefix(getProperties(_parameter),
                        objInst.getType().getName());
        // check if the creator must be added
        if (properties.containsKey("Insert.AccessSet4Creator")) {
            final AccessSet accessSet = getAccessSet(properties.getProperty("Insert.AccessSet4Creator"));
            if (accessSet != null) {
                insertAccess4Object(_parameter, objInst, Context.getThreadContext().getPersonId(), accessSet.getId());
            }
        }

        final Map<Integer, String> users = PropertiesUtil.analyseProperty(properties, "Insert.User", 0);
        final Map<Integer, String> sets = PropertiesUtil.analyseProperty(properties, "Insert.AccessSet", 0);
        for (final Entry<Integer, String> entry : users.entrySet()) {
            final AbstractUserObject userObj = getUserObject(entry.getValue());
            final AccessSet accessSet = getAccessSet(sets.get(entry.getKey()));
            if (userObj != null && accessSet != null) {
                insertAccess4Object(_parameter, objInst, userObj.getId(), accessSet.getId());
            }
        }
        return new Return();
    }

    /**
     * Gets the access set.
     *
     * @param _key the key
     * @return the access set
     * @throws CacheReloadException the cache reload exception
     */
    protected AccessSet getAccessSet(final String _key)
        throws CacheReloadException
    {
        final AccessSet ret;
        if (UUIDUtil.isUUID(_key)) {
            ret = AccessSet.get(UUID.fromString(_key));
        } else {
            ret = AccessSet.get(_key);
        }
        return ret;
    }

    /**
     * Gets the user object.
     *
     * @param _key the key
     * @return the user object
     * @throws EFapsException the e faps exception
     */
    protected AbstractUserObject getUserObject(final String _key)
        throws EFapsException
    {
        final AbstractUserObject ret;
        if (UUIDUtil.isUUID(_key)) {
            ret = AbstractUserObject.getUserObject(UUID.fromString(_key));
        } else {
            ret = AbstractUserObject.getUserObject(_key);
        }
        return ret;
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
            _insert.add(CIAdminAccess.Access4Object.TypeId, instObject.getType().getId());
            _insert.add(CIAdminAccess.Access4Object.ObjectId, instObject.getId());
        }
    }
}
