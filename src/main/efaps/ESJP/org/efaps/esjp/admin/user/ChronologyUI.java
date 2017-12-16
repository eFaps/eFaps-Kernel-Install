/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.esjp.admin.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.ChronologyType;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for chronology.
 *
 * @author The eFaps Team
 */
@EFapsUUID("d24a6606-95ac-427d-9689-31182dd71cd8")
@EFapsApplication("eFaps-Kernel")
public class ChronologyUI
    implements EventExecution
{

    /**
     * Method is called from within the form Admin_User_Person to retrieve the
     * value for the chronology.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return containing the timezone
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return retVal = new Return();
        final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);

        String actualChrono = ChronologyType.ISO8601.getKey();

        if (instance != null && instance.getType().getUUID().equals(CIAdminUser.Person.uuid)) {
            final PrintQuery print = new PrintQuery(instance);
            print.addAttribute(CIAdminUser.Person.Chronology);
            if (print.execute()) {
                final String chronoStr = print.<String>getAttribute(CIAdminUser.Person.Chronology);
                if (StringUtils.isNotEmpty(chronoStr)) {
                    actualChrono = chronoStr;
                }
            }
        }
        retVal.put(ReturnValues.SNIPLETT, ChronologyType.getByKey(actualChrono).getLabel());
        return retVal;
    }

    /**
     * Method is called from within the form Admin_User_Person to render a drop
     * down field with all Chronologies.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return containing a drop down
     * @throws EFapsException on error
     */
    public Return get4Edit(final Parameter _parameter)
        throws EFapsException
    {
        final Return retVal = new Return();

        final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        // set a default
        String actualChrono = ChronologyType.ISO8601.getKey();
        if (instance != null && instance.getType().getUUID().equals(CIAdminUser.Person.uuid)) {
            final PrintQuery print = new PrintQuery(instance);
            print.addAttribute(CIAdminUser.Person.Chronology);
            if (print.execute()) {
                final String chronoStr = print.<String>getAttribute(CIAdminUser.Person.Chronology);
                if (StringUtils.isNotEmpty(chronoStr)) {
                    actualChrono = chronoStr;
                }
            }
        }
        retVal.put(ReturnValues.VALUES, getValues(_parameter, actualChrono));
        return retVal;
    }

    /**
     * Method is called from within the form Admin_User_SettingChgForm to render
     * a drop down field with all Chronologies.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return containing a drop down
     * @throws EFapsException on error
     */
    public Return get4Setting(final Parameter _parameter)
        throws EFapsException
    {
        final Return retVal = new Return();
        final String actualChrono = Context.getThreadContext().getPerson().getChronologyType().getKey();
        retVal.put(ReturnValues.VALUES, getValues(_parameter, actualChrono));
        return retVal;
    }

    /**
     * Method to build a drop down field for html containing all timezone.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _currentChrono the current chrono
     * @return StringBuilder with drop down
     * @throws EFapsException on error
     */
    private List<DropDownPosition> getValues(final Parameter _parameter,
                                             final String _currentChrono)
        throws EFapsException
    {
        final List<DropDownPosition> ret = new ArrayList<>();
        final Field field = new Field();
        for (final ChronologyType chronoType : ChronologyType.values()) {
            final DropDownPosition val = field.getDropDownPosition(_parameter, chronoType.getKey(), chronoType
                            .getLabel());
            val.setSelected(chronoType.getKey().equals(_currentChrono));
            ret.add(val);
        }
        Collections.sort(ret, new Comparator<DropDownPosition>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _arg0,
                               final DropDownPosition _arg1)
            {
                return _arg0.getOrderValue().compareTo(_arg1.getOrderValue());
            }
        });
        return ret;
    }
}
