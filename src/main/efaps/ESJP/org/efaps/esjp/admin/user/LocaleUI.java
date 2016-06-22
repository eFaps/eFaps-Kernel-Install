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

package org.efaps.esjp.admin.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for Locale.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("5eb52b3e-3384-4675-9a55-396cdb5228be")
@EFapsApplication("eFaps-Kernel")
public class LocaleUI
{

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
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);

        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);
        final String localeStr;
        if (mode.equals(TargetMode.CREATE)) {
            localeStr = Locale.getDefault().toString();
        } else {
            localeStr = (String) uiValue.getObject();
        }
        retVal.put(ReturnValues.VALUES, getValues(_parameter, localeStr));
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
                                             final String _currentLocale)
        throws EFapsException
    {
        final List<DropDownPosition> ret = new ArrayList<DropDownPosition>();
        final Field field = new Field();
        for (final Locale locale : Locale.getAvailableLocales()) {
            final DropDownPosition val = field.getDropDownPosition(_parameter, locale.toString(), locale
                            .getDisplayName());
            val.setSelected(locale.toString().equals(_currentLocale));
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
