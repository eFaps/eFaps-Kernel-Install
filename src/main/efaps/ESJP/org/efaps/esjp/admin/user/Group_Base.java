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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: Association_Base.java 9233 2016-04-22 15:16:22Z jan@moxter.net
 *          $
 */
@EFapsUUID("30482e5e-08df-47bc-b683-9993636643b3")
@EFapsApplication("eFaps-Kernel")
public abstract class Group_Base
{

    /**
     * Key to be used to store the id of a goup in the session.
     */
    public static final String SESSIONVAR = "org.efaps.esjp.admin.user.Group.selected";

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing snipplet
     * @throws EFapsException on error
     */
    public Return getFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final boolean setSessionVar = "true".equalsIgnoreCase((String) properties.get("SetSessionVariable"));
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final Object value = fieldValue.getObject();

        final List<DropDownPosition> dropDownList = new ArrayList<DropDownPosition>();
        for (final Long groupId : Context.getThreadContext().getPerson().getGroups()) {
            final org.efaps.admin.user.Group group = org.efaps.admin.user.Group.get(groupId);
            final DropDownPosition ddPos = new DropDownPosition(groupId, group.getName(), group.getName());
            ddPos.setSelected(value != null && ((org.efaps.admin.user.Group) value).equals(group));
            dropDownList.add(ddPos);
        }
        if (dropDownList.isEmpty()) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Group);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            while (query.next()) {
                final org.efaps.admin.user.Group group = org.efaps.admin.user.Group
                                .get(query.getCurrentValue().getId());
                final DropDownPosition ddPos = new DropDownPosition(group.getId(), group.getName(), group.getName());
                ddPos.setSelected(value != null && ((org.efaps.admin.user.Group) value).equals(group));
                dropDownList.add(ddPos);
            }
        }

        Collections.sort(dropDownList, new Comparator<DropDownPosition>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
            }
        });
        if (setSessionVar && !dropDownList.isEmpty()) {
            boolean set = true;
            for (final DropDownPosition ddPos: dropDownList) {
                if (ddPos.isSelected()) {
                    Context.getThreadContext().setSessionAttribute(Group_Base.SESSIONVAR, ddPos.getValue());
                    set = false;
                    break;
                }
            }
            if (set) {
                Context.getThreadContext().setSessionAttribute(Group_Base.SESSIONVAR, dropDownList.get(0).getValue());
            }
        }
        ret.put(ReturnValues.SNIPLETT, new Field().getDropDownField(_parameter, dropDownList).toString());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing true if no Group assigned or more than one
     * @throws EFapsException on error
     */
    public Return checkAccess4MoreThanOne(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final boolean inverse = "true".equalsIgnoreCase((String) props.get("Inverse"));
        boolean access = false;

        if (Context.getThreadContext().getPerson().getGroups().isEmpty()
                        || Context.getThreadContext().getPerson().getGroups().size() > 1) {
            access  = true;
        }
        if (!inverse && access || inverse && !access) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

}
