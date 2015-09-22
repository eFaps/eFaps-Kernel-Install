/*
 * Copyright 2003 - 2015 The eFaps Team
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
package org.efaps.esjp.common.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uitable.MultiPrint;
import org.efaps.util.EFapsException;

/**
 * The Class DashboardElement_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("e13d55a2-5b1e-4e75-bc9c-c204d642f8bf")
@EFapsApplication("eFaps-Kernel")
public abstract class DashboardElement_Base
    extends AbstractCommon
{

    /**
     * Auto complete4 program.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException on error
     */
    public Return autoComplete4Esjp(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, Object>> tmpMap = new TreeMap<>();
        final List<Instance> instances = new MultiPrint()
        {

            @Override
            protected void add2QueryBldr(final Parameter _parameter,
                                         final QueryBuilder _queryBldr)
                                             throws EFapsException
            {
                final String input = (String) _parameter.get(ParameterValues.OTHERS);
                _queryBldr.addWhereAttrMatchValue(CIAdminProgram.Abstract.Name, input + "*").setIgnoreCase(true);

            };
        }.getInstances(_parameter);

        final MultiPrintQuery multi = new MultiPrintQuery(instances);
        multi.addAttribute(CIAdminProgram.Abstract.Name);
        multi.execute();
        while (multi.next()) {
            final String name = multi.<String>getAttribute(CIAdminProgram.Abstract.Name);
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("eFapsAutoCompleteKEY", String.valueOf(multi.getCurrentInstance().getId()));
            map.put("eFapsAutoCompleteVALUE", name);
            map.put("eFapsAutoCompleteCHOICE", name);
            tmpMap.put(name, map);
        }
        final Return ret = new Return();
        final List<Map<String, Object>> list = new ArrayList<>();
        list.addAll(tmpMap.values());
        ret.put(ReturnValues.VALUES, list);
        return ret;
    }

    /**
     * Esjp field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return esjpFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (value.getObject() instanceof String) {
            ret.put(ReturnValues.VALUES, value.getObject());
        } else {
            final PrintQuery print = new PrintQuery(Instance.get(CIAdminProgram.Java.getType(), (Long) value
                            .getObject()));
            print.addAttribute(CIAdminProgram.Java.Name);
            print.execute();
            ret.put(ReturnValues.VALUES, print.getAttribute(CIAdminProgram.Java.Name));
        }
        return ret;
    }
}
