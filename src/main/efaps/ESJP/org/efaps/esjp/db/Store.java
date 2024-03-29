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
package org.efaps.esjp.db;

import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ci.CIDB;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("0f824311-3747-4cfc-85ce-a5268a6ba9c9")
@EFapsApplication("eFaps-Kernel")
public class Store
{
    /**
     * Execute.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Insert insert = new Insert(CIDB.Store);
        insert.add("Name", _parameter.getParameterValue("name"));
        insert.add("UUID", _parameter.getParameterValue("uuid"));
        insert.add("Revision", _parameter.getParameterValue("revision"));
        insert.execute();
        final Instance instance = insert.getInstance();

        final Insert resourceInsert = new Insert(CIDB.Resource);
        resourceInsert.add("Name", _parameter.getParameterValue("resource4create"));
        resourceInsert.execute();
        final Instance resource = resourceInsert.getInstance();

        final Insert connect = new Insert(CIDB.Store2Resource);
        connect.add("From", ((Long) instance.getId()).toString());
        connect.add("To", ((Long) resource.getId()).toString());
        connect.execute();
        return ret;
    }

    /**
     * This method is called first to render simple inputfields.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     */
    public Return getResourceFieldValueUI(final Parameter _parameter)
    {
        final StringBuilder ret = new StringBuilder();
        final IUIValue fieldvalue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);

        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        final Return retVal = new Return();

        if (mode.equals(TargetMode.CREATE)) {
            ret.append("<input name=\"").append(fieldvalue.getField().getName()).append("\" type=\"text\" ").append(
                            " size=\"").append(fieldvalue.getField().getCols()).append("\">");
        }
        if (ret != null) {
            retVal.put(ReturnValues.SNIPLETT, ret);
        }
        return retVal;
    }
}
