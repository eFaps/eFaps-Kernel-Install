/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.esjp.admin.datamodel;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for the BaseUoM.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c3d4e45f-cb46-4abf-9aa3-67703140b74b")
@EFapsRevision("$Rev$")
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
        final Return retVal = new Return();
        final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        Long actual = new Long(0);
        final TreeMap<String, Long> map = new TreeMap<String, Long>();
        if (instance != null) {
            final Dimension dim = Dimension.get(instance.getId());
            if (dim.getBaseUoM() != null) {
                actual = dim.getBaseUoM().getId();
            }
            for (final UoM uoM : dim.getUoMs()) {
                map.put(uoM.getName(), uoM.getId());
            }
        }

        final StringBuilder ret = new StringBuilder();

        ret.append("<select size=\"1\" name=\"baseOuM4Edit\">");
        for (final Map.Entry<String, Long> entry : map.entrySet()) {
            ret.append("<option");

            if (entry.getValue().equals(actual)) {
                ret.append(" selected=\"selected\" ");
            }
            ret.append(" value=\"").append(entry.getValue()).append("\">").append(entry.getKey())
                            .append("</option>");
        }

        ret.append("</select>");
        retVal.put(ReturnValues.SNIPLETT, ret.toString());

        return retVal;
    }

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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)
                        && properties.containsKey("Default")) {
            fieldValue.setValue(properties.get("Default"));
        }

    }

    @SuppressWarnings("unchecked")
    public Return execute4BaseUoM(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object tmp = _parameter.get(ParameterValues.UIOBJECT);
        Attribute attribute;
        // just for backward compatibility
        if (tmp instanceof FieldValue) {
            final FieldValue fieldValue = (FieldValue) tmp;
            attribute = fieldValue.getAttribute();
        } else {
            final UIValue fieldValue = (UIValue) tmp;
            attribute = fieldValue.getAttribute();
        }

        Map<Attribute, Map<Object, Object>> values;
        if (Context.getThreadContext().containsRequestAttribute(RangesValue_Base.REQUESTCACHEKEY)) {
            values = (Map<Attribute, Map<Object, Object>>)
                            Context.getThreadContext().getRequestAttribute(RangesValue_Base.REQUESTCACHEKEY);
        } else {
            values = new HashMap<Attribute, Map<Object, Object>>();
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

            final Map<String, String> tmpMap = new TreeMap<String, String>();
            final Map<String, Long> order = new TreeMap<String, Long>();
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
            Map retmap;
            if (_parameter.get(ParameterValues.UIOBJECT) instanceof FieldValue) {
                retmap = tmpMap;
                setSelectedValue(_parameter);
            } else {
                retmap = new LinkedHashMap<Long, String>();
                for (final Entry<String, Long> entry : order.entrySet()) {
                    retmap.put(entry.getValue(), entry.getKey());
                }
            }
            values.put(attribute, retmap);
            ret.put(ReturnValues.VALUES, retmap);
        }
        return ret;
    }
}
