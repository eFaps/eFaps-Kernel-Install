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
package org.efaps.esjp.admin.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminUser;
import org.efaps.ci.CIType;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.admin.access.SimpleAccessCheckOnType;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.util.EFapsException;


/**
 * Class contains some method need to create a Person and to connect the person
 * to a role.
 *
 * @author The eFaps Team
 */
@EFapsUUID("9688a81e-eaae-4bd2-b6df-a5a8087483d0")
@EFapsApplication("eFaps-Kernel")
public abstract class Person_Base
{
    /**
     * The accesscheck for Admin_Person Types. It is a SimpleAccessCheckOnType with
     * the exception of updating a password.
     *
     * @param _parameter Parameter as past from eFaps to an esjp
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return accessCheck(final Parameter _parameter)
        throws EFapsException
    {
        final SimpleAccessCheckOnType check = new SimpleAccessCheckOnType()
        {

            @Override
            protected boolean checkAccess(final Parameter _parameter,
                                          final Instance _instance,
                                          final AccessType _accessType)
                throws EFapsException
            {
                boolean ret = super.checkAccess(_parameter, _instance, _accessType);
                // in case of modify, check if the user itself wants to change
                // its own password
                if (!ret && _accessType.equals(AccessTypeEnums.MODIFY.getAccessType())
                                && _instance.getId() == Context.getThreadContext().getPersonId()) {
                    // Common_Main_PwdChg
                    final Role setpwdRole = Role.get(UUID.fromString("2c101471-43e3-4c97-9045-f48f5b12b6ed"));
                    // if the user is allowed to change its password, check that
                    // only the password is updated
                    if (Context.getThreadContext().getPerson().isAssigned(setpwdRole)) {
                        final Object values = _parameter.get(ParameterValues.NEW_VALUES);
                        if (values != null && values instanceof Set && ((Set<?>) values).size() == 1) {
                            for (final Object attr : (Set<?>) values) {
                                if (attr != null && attr instanceof Attribute
                                                && ((Attribute) attr).getName()
                                                                .equals(CIAdminUser.Person.Password.name)) {
                                    ret = true;
                                }
                            }
                        }
                    }
                }
                return ret;
            }
        };
        return check.execute(_parameter);
    }


    /**
     * Method called to connect a Person to a Role.
     *
     * @param _parameter Parameter as past from eFaps to an esjp
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return connectUser2UserUI(final Parameter _parameter)
        throws EFapsException
    {

        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final Instance parent = _parameter.getInstance();

        final String[] childOids = _parameter.getParameterValues("selectedRow");

        if (childOids != null) {
            final String type = (String) properties.get("ConnectType");
            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");

            for (final String childOid : childOids) {
                final Instance child = Instance.get(childOid);
                final Insert insert = new Insert(type);
                insert.add(parentAttr, "" + parent.getId());
                insert.add(childAttr, "" + child.getId());
                insert.add("UserJAASSystem", "" + getJAASSystemID());
                insert.execute();
            }
        }
        return new Return();
    }

    /**
     * This method inserts the JAASSystem for a User into the eFaps-Database.<br>
     * It is executed on a INSERT_POST Trigger on the Type User_Person.
     *
     * @param _parameter Parameter as past from eFaps to en esjp
     * @return null
     * @throws EFapsException on error
     */
    public Return insertJaaskeyTrg(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();

        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);

        final Object jaassystemid = getJAASSystemID();
        if (jaassystemid != null) {
            final Object[] key  = (Object[]) values.get(instance.getType().getAttribute("Name"));
            final Insert insert = new Insert("Admin_User_JAASKey");
            insert.add("Key", key[0]);
            insert.add("JAASSystemLink", getJAASSystemID());
            insert.add("UserLink", ((Long) instance.getId()).toString());
            insert.execute();
        }
        return null;
    }

    /**
     * Get the ID of the JAASSYstem for eFaps.
     *
     * @return ID of the JAASSYSTEM, NULL if not found
     * @throws EFapsException on error
     */
    private Object getJAASSystemID()
        throws EFapsException
    {
        Object objId = null;
        //"Admin_User_JAASSystem"
        final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString("28e45c59-946d-4502-94b9-58a1bf23ab88"));
        queryBldr.addWhereAttrEqValue("Name", "eFaps");
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        if (query.next()) {
            objId = query.getCurrentValue().getId();
        }

        return objId;
    }

    /**
     * This method updates the JAASKey for a User/Role into the eFaps-Database.<br>
     * It is executed on a UPDATE_POST Trigger on the Type User_Person.
     *
     * @param _parameter Parameter as past from eFaps
     * @return null
     * @throws EFapsException on error
     */
    public Return updateJaaskeyTrg(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();

        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);

        final Object jaassystemid = getJAASSystemID();
        if (jaassystemid != null) {
            final Object[] key = (Object[]) values.get(instance.getType().getAttribute("Name"));
            if (key != null && key.length > 0) {
                final Type type = CIAdminUser.JAASKey.getType();
                final Long id = getJAASUserID(instance.getId());
                if (id != null && type != null) {
                    final Instance upInst = Instance.get(type, id);
                    if (upInst.isValid()) {
                        final Update update = new Update(upInst);
                        update.add("Key", key[0]);
                        update.execute();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the ID of the JAASKey for eFaps.
     * @param _id id of the instance  the JAASUSerId is searched for
     * @return ID of the JAASKey, NULL if not found
     * @throws EFapsException on error
     */
    private Long getJAASUserID(final Long _id)
        throws EFapsException
    {
        Long objId = null;
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.JAASKey);
        queryBldr.addWhereAttrEqValue(CIAdminUser.JAASKey.UserLink, _id);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        if (query.next()) {
            objId = query.getCurrentValue().getId();
        }
        return objId;
    }


    /**
     * Create a Person by copying from another Person.
     * @param _parameter Parameter as passed by the eFaps API
     * @return new empty Return
     * @throws EFapsException on error
     */
    public Return createFromPerson(final Parameter _parameter)
        throws EFapsException
    {
        final Create create = new Create()
        {
            @Override
            protected void add2basicInsert(final Parameter _parameter,
                                           final Insert _insert)
                throws EFapsException
            {
                final String persId = _parameter.getParameterValue("copyPerson");
                final Person pers = org.efaps.admin.user.Person.get(Long.valueOf(persId));

                final QueryBuilder queryBldr = new QueryBuilder(CIAdmin.Language);
                queryBldr.addWhereAttrEqValue(CIAdmin.Language.Language, pers.getLanguage());
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                query.next();
                _insert.add(CIAdminUser.Person.Language, query.getCurrentValue());
                _insert.add(CIAdminUser.Person.TimeZone, pers.getTimeZone().getID());
                _insert.add(CIAdminUser.Person.Chronology, pers.getChronologyType().getKey());
                _insert.add(CIAdminUser.Person.Locale, pers.getLocale().toString());
            }

            @Override
            public void connect(final Parameter _parameter,
                                final Instance _instance)
                throws EFapsException
            {
                final String persId = _parameter.getParameterValue("copyPerson");
                createConnection(CIAdminUser.Person2Group, persId, _instance);
                createConnection(CIAdminUser.Person2Role, persId, _instance);
                createConnection(CIAdminUser.Person2Company, persId, _instance);
            }

            private void createConnection(final CIType _ciType,
                                          final String _persId,
                                          final Instance _instance)
                throws EFapsException
            {
                final QueryBuilder queryBldr1 = new QueryBuilder(_ciType);
                queryBldr1.addWhereAttrEqValue(CIAdminUser._Abstract2Abstract.UserFromAbstractLink, _persId);
                final MultiPrintQuery multi = queryBldr1.getPrint();
                multi.addAttribute(CIAdminUser._Abstract2Abstract.UserToAbstractLink,
                                CIAdminUser._Abstract2Abstract.UserJAASSystem);
                multi.execute();
                while (multi.next()) {
                    final Insert insert = new Insert(_ciType);
                    insert.add(CIAdminUser._Abstract2Abstract.UserFromAbstractLink, _instance);
                    insert.add(CIAdminUser._Abstract2Abstract.UserToAbstractLink,
                                    multi.<Long>getAttribute(CIAdminUser._Abstract2Abstract.UserToAbstractLink));
                    insert.add(CIAdminUser._Abstract2Abstract.UserJAASSystem,
                                    multi.<Long>getAttribute(CIAdminUser._Abstract2Abstract.UserJAASSystem));
                    insert.execute();
                }
            }
        };
        return create.execute(_parameter);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Autocomplete map
     * @throws EFapsException on error
     */
    public Return autoComplete4Person(final Parameter _parameter)
        throws EFapsException
    {
        final String input = (String) _parameter.get(ParameterValues.OTHERS);
        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (input.length() > 0) {
            final Map<String, Map<String, String>> tmpMap = new TreeMap<String, Map<String, String>>();
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person);
            queryBldr.addWhereAttrMatchValue(CIAdminUser.Person.Name, input + "*").setIgnoreCase(true);

            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUser.Person.ID, CIAdminUser.Person.Name,
                            CIAdminUser.Person.FirstName, CIAdminUser.Person.LastName);
            multi.execute();
            while (multi.next()) {
                final long id = multi.<Long>getAttribute(CIAdminUser.Person.ID);
                final String name = multi.<String>getAttribute(CIAdminUser.Person.Name);
                final String firstname = multi.<String>getAttribute(CIAdminUser.Person.FirstName);
                final String lastname = multi.<String>getAttribute(CIAdminUser.Person.LastName);
                final String fullName = lastname + ", " + firstname;
                final String choice = name + " - " + fullName;
                final Map<String, String> map = new HashMap<String, String>();
                map.put("eFapsAutoCompleteKEY", String.valueOf(id));
                map.put("eFapsAutoCompleteCHOICE", choice);
                map.put("eFapsAutoCompleteVALUE", name);
                tmpMap.put(choice, map);
            }
            list.addAll(tmpMap.values());
        }
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, list);
        return retVal;
    }

}
