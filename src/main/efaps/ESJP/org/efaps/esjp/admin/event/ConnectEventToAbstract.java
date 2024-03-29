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
package org.efaps.esjp.admin.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ci.CIAdminEvent;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * ESJP used to create and update events.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3e6ba860-22ff-486b-b424-27a0ee9e72f2")
@EFapsApplication("eFaps-Kernel")
public class ConnectEventToAbstract
{
    /**
     * UUID of the program type.
     */
    private static final UUID UUID_PROGRAM = UUID.fromString("11043a35-f73c-481c-8c77-00306dbce824");

    /**
     * Shows a drop down list of all allowed event types depending on the parent
     * type.
     *
     * @param _parameter parameters from the field type4NotView of the form
     *            Admin_Event_Definition
     * @return HTML string with the drop down list
     * @throws EFapsException on error
     */
    public Return getEventTypesUI(final Parameter _parameter)
        throws EFapsException
    {
        final UIValue fieldvalue = (UIValue) _parameter.get(ParameterValues.UIOBJECT);
        final TargetMode mode = fieldvalue.getTargetMode();

        final Instance callInstance = _parameter.getCallInstance();

        // get parent instance and current selected instance
        Instance parentInstance = null;
        long selectedId = 0;
        if (mode.equals(TargetMode.CREATE)) {
            parentInstance = callInstance;
        } else if (mode.equals(TargetMode.EDIT)) {
            final PrintQuery print = new PrintQuery(callInstance);
            final SelectBuilder sel = new SelectBuilder().linkto(CIAdminEvent.Definition.Abstract).instance();
            print.addSelect(sel);
            print.execute();
            parentInstance = print.<Instance>getSelect(sel);
            selectedId = callInstance.getType().getId();
        }

        // evaluate all possible event types
        final Map<String, Long> map = new TreeMap<String, Long>();
        for (final Type type : parentInstance.getType().getAllowedEventTypes()) {
            collectAllowedEventTypes(map, type);
        }

        final String fieldName = fieldvalue.getField().getName();
        final StringBuilder ret = new StringBuilder();
        ret.append("<select name=\"").append(fieldName).append("\" size=\"1\">");

        for (final Map.Entry<String, Long> entry : map.entrySet()) {
            ret.append("<option value=\"").append(entry.getValue()).append("\"");
            if (selectedId == entry.getValue()) {
                ret.append(" selected=\"selected\"");
            }
            ret.append(">").append(entry.getKey()).append("</option>");
        }

        ret.append("</select>");

        final Return retVal = new Return();
        if (ret != null) {
            retVal.put(ReturnValues.SNIPLETT, ret);
        }
        return retVal;
    }

    /**
     * Evaluates given event type and adds them to the allowed event types map.
     *
     * @param _map map in which the information about the event types are stored
     * @param _type current event type to collect to the map
     * @throws CacheReloadException on error
     */
    protected void collectAllowedEventTypes(final Map<String, Long> _map,
                                            final Type _type)
        throws CacheReloadException
    {
        if (!_type.isAbstract()) {
            final String labelName = new StringBuilder(_type.getName()).append(".Label").toString();
            _map.put(DBProperties.getProperty(labelName), _type.getId());
        }
        for (final Type childType : _type.getChildTypes()) {
            collectAllowedEventTypes(_map, childType);
        }
    }

    /**
     * Search for all existing programs in eFaps and returns them as drop down
     * list so that the user could select one.
     *
     * @param _parameter parameters from the field program4NotView of the form
     *            Admin_Event_Definition
     * @return HTML with the drop down list for all existing programs within
     *         eFaps
     * @throws EFapsException on error
     * @see #UUID_PROGRAM
     */
    public Return getProgramsUI(final Parameter _parameter)
        throws EFapsException
    {
        final IUIValue fieldvalue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        long selectedId = 0;
        if (mode.equals(TargetMode.EDIT)) {
            final Instance callInstance = _parameter.getCallInstance();
            final PrintQuery print = new PrintQuery(callInstance);
            print.addAttribute(fieldvalue.getAttribute().getName());
            if (print.execute()) {
                selectedId = print.<Long>getAttribute(fieldvalue.getAttribute().getName());
            }
        }

        // search for all programs
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(ConnectEventToAbstract.UUID_PROGRAM));
        final MultiPrintQuery print = queryBldr.getPrint();
        print.addAttribute("Name", "ID");
        print.execute();
        new TreeMap<String, Long>();
        final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
        while (print.next()) {
            final Long id = print.<Long>getAttribute("ID");
            final String name = print.<String>getAttribute("Name");
            final DropDownPosition pos = new DropDownPosition(id, name, name);
            positions.add(pos);
            if (selectedId == id) {
                pos.setSelected(true);
            }
        }
        Collections.sort(positions, new Comparator<DropDownPosition>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
            }
        });
        // and return the string
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, positions);
        return retVal;
    }

    /**
     * Creates a new event for given type and program depending on the user
     * input on the web form Admin_Event_Definition.
     *
     * @param _parameter from the form Admin_Event_Definition in mode create
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return createNewEventUI(final Parameter _parameter)
        throws EFapsException
    {
        final Instance callInstance = _parameter.getCallInstance();
        final String eventTypeId = _parameter.getParameterValue("type4NotView");
        final String name = _parameter.getParameterValue("name");
        final String index = _parameter.getParameterValue("index");
        final String programId = _parameter.getParameterValue("program4NotView");
        final String method = _parameter.getParameterValue("method");

        final Type eventType = Type.get(Long.parseLong(eventTypeId));

        final Insert insert = new Insert(eventType);
        insert.add("Name", name);
        insert.add("IndexPosition", index);
        insert.add("Abstract", "" + callInstance.getId());
        insert.add("JavaProg", programId);
        insert.add("Method", method);
        insert.execute();
        return new Return();
    }
}
