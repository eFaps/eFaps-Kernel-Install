/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.esjp.admin.user;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.admin.access.SimpleAccessCheckOnType;
import org.efaps.util.EFapsException;

/**
 * Class contains some method need to create a Person and to connect the person
 * to a role.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1b777261-6da1-4003-87e4-2937e44ff269")
@EFapsRevision("$Rev$")
public class Person
{
    /**
     * The accesscheck for Admin_Person Types. It is a SimpleAccessCheckOnType with
     * the exception of updating a password.
     *
     * @param _parameter Parameter as past from eFaps to an esjp
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return accessCheck(final Parameter _parameter) throws EFapsException
    {
        final SimpleAccessCheckOnType check = new SimpleAccessCheckOnType(){

            @Override
            protected boolean checkAccess(final Parameter _parameter,
                                          final Instance _instance,
                                          final AccessType _accessType)
                throws EFapsException
            {
                boolean ret = super.checkAccess(_parameter, _instance, _accessType);
                // in case of modify, check if the user itself wants to change its own password
                if (!ret && _accessType.equals(AccessTypeEnums.MODIFY.getAccessType())
                                && _instance.getId() == Context.getThreadContext().getPersonId()) {
                    // Common_Main_PwdChg
                    final Role setpwdRole = Role.get(UUID.fromString("2c101471-43e3-4c97-9045-f48f5b12b6ed"));
                    // if the user is allowed to change its password, check that only the password is updated
                    if (Context.getThreadContext().getPerson().isAssigned(setpwdRole)) {
                        final Object values = _parameter.get(ParameterValues.NEW_VALUES);
                        if (values != null && values instanceof Set && ((Set<?>) values).size() == 1) {
                           for (final Object attr : (Set<?>) values) {
                               if (attr != null && attr instanceof Attribute
                                             && ((Attribute) attr).getName().equals(CIAdminUser.Person.Password.name)) {
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
     * This method updates the JAASKey for a User into the eFaps-Database.<br>
     * It is executed on a UPDATE_POST Trigger on the Type User_Person.
     *
     * @param _parameter Parameter as past from eFaps to en esjp
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
                final Instance upInst = Instance.get(type, id);
                if (upInst.isValid()) {
                final Update update = new Update(upInst);
                    update.add("Key", key[0]);
                    update.execute();
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
}
