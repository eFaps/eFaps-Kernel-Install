/*
 * Copyright 2003 - 2010 The eFaps Team
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
import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
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
    @SuppressWarnings("unchecked")
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        Map<Attribute, Map<String, String>> values;
        if (Context.getThreadContext().containsRequestAttribute(RangesValue_Base.REQUESTCACHEKEY)) {
            values = (Map<Attribute, Map<String, String>>)
                            Context.getThreadContext().getRequestAttribute(RangesValue_Base.REQUESTCACHEKEY);
        } else {
            values = new HashMap<Attribute, Map<String, String>>();
            Context.getThreadContext().setRequestAttribute(RangesValue_Base.REQUESTCACHEKEY, values);
        }

        if (values.containsKey(fieldValue.getAttribute())) {
            ret.put(ReturnValues.VALUES, values.get(fieldValue.getAttribute()));
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

            final Map<String, String> map = new TreeMap<String, String>();

            while (multi.next()) {
                final String strVal;
                if (list != null) {
                    strVal = list.makeString(multi.getCurrentInstance(), multi,
                                    (TargetMode) _parameter.get(ParameterValues.ACCESSMODE));
                } else {
                    strVal = multi.getAttribute(value).toString();
                }
                map.put(strVal, ((Long) multi.getCurrentInstance().getId()).toString());
            }
            setSelectedValue(_parameter);
            values.put(fieldValue.getAttribute(), map);
            ret.put(ReturnValues.VALUES, map);
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
}
