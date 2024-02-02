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
package org.efaps.esjp.admin.datamodel;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for the BaseUoM.
 *
 * @author The eFaps Team
 */
@EFapsUUID("c3d4e45f-cb46-4abf-9aa3-67703140b74b")
@EFapsApplication("eFaps-Kernel")
public abstract class DimensionUI_Base
    implements EventExecution
{

    /**
     * Method is called from within the form Admin_Datamodel_DimensionForm to
     * retrieve the value for the BaseUoM.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        final List<DropDownPosition> values = new ArrayList<>();
        if (instance != null) {
            final Dimension dim = Dimension.get(instance.getId());
            for (final UoM uoM : dim.getUoMs()) {
                values.add(new DropDownPosition(uoM.getId(), uoM.getName())
                                .setSelected(dim.getBaseUoM() != null && dim.getBaseUoM().getId() == uoM.getId()));
            }
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * Add2 query bldr.
     *
     * @param _parameter the _parameter
     * @param _queryBldr the _query bldr
     * @throws EFapsException the e faps exception
     */
    protected void add2QueryBldr(final Parameter _parameter,
                                 final QueryBuilder _queryBldr)
        throws EFapsException
    {

    }

    /**
     * Set the default value.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @throws EFapsException on error
     */
    protected void setSelectedValue(final Parameter _parameter)
        throws EFapsException
    {
        _parameter.get(ParameterValues.UIOBJECT);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)
                        && properties.containsKey("Default")) {
            //fieldValue. Value(properties.get("Default"));
        }

    }

    /**
     * Execute4 base uo m.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    @SuppressWarnings("unchecked")
    public Return execute4BaseUoM(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object tmp = _parameter.get(ParameterValues.UIOBJECT);

        final UIValue fieldValue = (UIValue) tmp;
        final Attribute attribute = fieldValue.getAttribute();

        Map<Attribute, Map<Object, Object>> values;
        if (Context.getThreadContext().containsRequestAttribute(RangesValue_Base.REQUESTCACHEKEY)) {
            values = (Map<Attribute, Map<Object, Object>>) Context.getThreadContext()
                            .getRequestAttribute(RangesValue_Base.REQUESTCACHEKEY);
        } else {
            values = new HashMap<>();
            Context.getThreadContext().setRequestAttribute(RangesValue_Base.REQUESTCACHEKEY, values);
        }

        if (values.containsKey(attribute)) {
            ret.put(ReturnValues.VALUES, values.get(attribute));
        } else {
            final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final String type = (String) properties.get("Type");
            final String value = (String) properties.get("Value");

            final QueryBuilder queryBldr = new QueryBuilder(Type.get(type));
            add2QueryBldr(_parameter, queryBldr);
            final MultiPrintQuery multi = queryBldr.getPrint();

            ValueList list = null;
            if (value.contains("$<")) {
                final ValueParser parser = new ValueParser(new StringReader(value));
                try {
                    list = parser.ExpressionString();
                } catch (final ParseException e) {
                    throw new EFapsException(RangesValue_Base.class.toString(), e);
                }
                list.makeSelect(multi);
            } else {
                multi.addAttribute(value);
            }
            multi.execute();

            final Map<String, String> tmpMap = new TreeMap<>();
            final Map<String, Long> order = new TreeMap<>();
            while (multi.next()) {
                String strVal;
                if (list != null) {
                    strVal = list.makeString(multi.getCurrentInstance(), multi,
                                    (TargetMode) _parameter.get(ParameterValues.ACCESSMODE));
                } else {
                    strVal = multi.getAttribute(value).toString();
                }
                if ("true".equals(properties.get("DefaultBaseUoM"))) {
                    strVal = strVal + " - " + Dimension.get(multi.getCurrentInstance().getId()).getBaseUoM().getName();
                }
                tmpMap.put(strVal, ((Long) multi.getCurrentInstance().getId()).toString());
                order.put(strVal, multi.getCurrentInstance().getId());
            }
            @SuppressWarnings("rawtypes")
            final Map retmap = new LinkedHashMap<Long, String>();
            for (final Entry<String, Long> entry : order.entrySet()) {
                retmap.put(entry.getValue(), entry.getKey());
            }
            values.put(attribute, retmap);
            ret.put(ReturnValues.VALUES, retmap);
        }
        return ret;
    }
}
