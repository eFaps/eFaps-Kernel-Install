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
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Range from the Database.<br>
 * The Class makes a query against the Database, with the "Type" from the
 * Properties of the Parameters and returns a map sorted by the values. The key
 * returned is the value, as specified by the Property "Value". The value of the
 * map is the ID of the Objects. The sorting is done by using a TreeMap, that
 * means that the Objects are sorted by their natural order. Both value and key
 * are String.
 * The values are stored in the request cache to reduce access to the
 * eFaps connected Database.
 * The value can also be an expression as defined for DBProperties.
 * e.g. $&lt;Name&gt; - $&lt;Value&gt;
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("17503d60-c2d2-4856-9d94-87333b45f739")
@EFapsRevision("$Rev$")
public abstract class RangesValue_Base
    extends AbstractCommon
    implements EventExecution
{
    /**
     * Key used to store the values in the request cache.
     */
    public static final String REQUESTCACHEKEY = "org.efaps.esjp.admin.datamodel.RangesValue";

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter    parameter as defined by the eFaps API
     * @return map with value and keys
     * @throws EFapsException on error
     */
    @Override
    @SuppressWarnings("unchecked")
    public Return execute(final Parameter _parameter)
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
            final String type = getProperty(_parameter, "Type");
            final String value = getProperty(_parameter, "Value");

            final QueryBuilder queryBldr = new QueryBuilder(Type.get(type));
            add2QueryBldr(_parameter, queryBldr);
            final MultiPrintQuery multi = queryBldr.getPrint();
            ValueList list = null;
            if (multi.isMarked4execute()) {
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
            }

            final Map<String, String> tmpMap = new TreeMap<String, String>();
            final Map<String, Long> order = new TreeMap<String, Long>();
            while (multi.next()) {
                final String strVal;
                if (list != null) {
                    strVal = list.makeString(multi.getCurrentInstance(), multi,
                                    (TargetMode) _parameter.get(ParameterValues.ACCESSMODE));
                } else {
                    strVal = multi.getAttribute(value).toString();
                }
                tmpMap.put(strVal, ((Long) multi.getCurrentInstance().getId()).toString());
                order.put(strVal,  multi.getCurrentInstance().getId());
            }
            Map retmap;

            if (!attribute.isRequired() && "true".equalsIgnoreCase(getProperty(_parameter, "EmptyValue"))) {
                if (!tmpMap.isEmpty() && !order.isEmpty()) {
                    tmpMap.put("-", "");
                    order.put("-", new Long(0));
                }
            }

            if (_parameter.get(ParameterValues.UIOBJECT) instanceof FieldValue) {
                retmap = tmpMap;
                setSelectedValue(_parameter, retmap);
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

    /**
     * @param _parameter    Parameter as passes by the eFaps API
     * @param _queryBldr    QueryBuilder to add to
     * @throws EFapsException on error
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
    protected void setSelectedValue(final Parameter _parameter,
                                    final Map<?, ?> _map)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)
                        && getProperty(_parameter, "Default") != null) {
            fieldValue.setValue(getProperty(_parameter, "Default"));
        }
    }
}
