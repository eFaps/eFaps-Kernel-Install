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
 */

package org.efaps.esjp.admin.datamodel;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
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
import org.efaps.api.ui.IOption;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
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
 */
@EFapsUUID("17503d60-c2d2-4856-9d94-87333b45f739")
@EFapsApplication("eFaps-Kernel")
public abstract class RangesValue_Base
    extends AbstractCommon
    implements EventExecution, Serializable
{
    /**
     * Key used to store the values in the request cache.
     */
    protected static final String REQUESTCACHEKEY = RangesValue.class.getName() + ".CacheKey";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Execute.
     *
     * @param _parameter    parameter as defined by the eFaps API
     * @return map with value and keys
     * @throws EFapsException on error
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final UIValue uiValue = (UIValue) _parameter.get(ParameterValues.UIOBJECT);
        final Attribute attribute = uiValue.getAttribute();

        Map<Attribute, List<IOption>> cachedValues;
        if (Context.getThreadContext().containsRequestAttribute(RangesValue.REQUESTCACHEKEY)) {
            cachedValues = (Map<Attribute, List<IOption>>)
                            Context.getThreadContext().getRequestAttribute(RangesValue.REQUESTCACHEKEY);
        } else {
            cachedValues = new HashMap<Attribute, List<IOption>>();
            Context.getThreadContext().setRequestAttribute(RangesValue.REQUESTCACHEKEY, cachedValues);
        }

        if (cachedValues.containsKey(attribute)) {
            ret.put(ReturnValues.VALUES, cachedValues.get(attribute));
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

            final List<IOption> values = new ArrayList<>();
            while (multi.next()) {
                final String strVal;
                if (list != null) {
                    strVal = list.makeString(multi.getCurrentInstance(), multi, (TargetMode) _parameter.get(
                                    ParameterValues.ACCESSMODE));
                } else {
                    strVal = multi.getAttribute(value).toString();
                }
                final RangeValueOption option = getOption(_parameter)
                                .setLabel(strVal)
                                .setValue(multi.getCurrentInstance().getId());
                if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
                    option.setSelected(isSelected(_parameter, option));
                } else {
                    option.setSelected(Long.valueOf(
                                    multi.getCurrentInstance().getId()).equals(uiValue.getObject()));
                }
                values.add(option);
            }

            if (!attribute.isRequired() && "true".equalsIgnoreCase(getProperty(_parameter, "EmptyValue"))) {
                values.add(getOption(_parameter)
                                .setLabel("-")
                                .setValue(0));
            }

            Collections.sort(values, new Comparator<IOption>()
            {

                @Override
                public int compare(final IOption _arg0,
                                   final IOption _arg1)
                {
                    return _arg0.getLabel().compareTo(_arg1.getLabel());
                }
            });
            ret.put(ReturnValues.VALUES, values);
        }
        return ret;
    }

    /**
     * Gets the option.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the option
     */
    protected RangeValueOption getOption(final Parameter _parameter)
    {
        return new RangeValueOption();
    }

    /**
     * Add2 query bldr.
     *
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
     * @param _option the option
     * @return true, if is selected
     * @throws EFapsException on error
     */
    protected boolean isSelected(final Parameter _parameter,
                                 final RangeValueOption _option)
        throws EFapsException
    {
        boolean ret = false;
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
            if (containsProperty(_parameter, "SystemConfig") && containsProperty(_parameter, "SysConfLink")) {
                final String configStr = getProperty(_parameter, "SystemConfig");
                final SystemConfiguration config = isUUID(configStr) ? SystemConfiguration.get(UUID.fromString(
                                configStr)) : SystemConfiguration.get(configStr);
                final Instance inst = config.getLink(getProperty(_parameter, "SysConfLink"));
                ret = _option.getValue().equals(inst.getId());
            } else if (containsProperty(_parameter, "Default")) {
                ret = _option.getLabel().equals(getProperty(_parameter, "Default"));
            } else if (containsProperty(_parameter, "DefaultRegex")) {
                final String regex = getProperty(_parameter, "DefaultRegex");
                ret = _option.getLabel().matches(regex);
            }
        }
        return ret;
    }

    /**
     * The Class RangeValueOption.
     */
    public static class RangeValueOption
        implements IOption
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The label. */
        private String label;

        /** The object. */
        private Object value;

        /** The selected. */
        private boolean selected;

        @Override
        public String getLabel()
        {
            return this.label;
        }

        @Override
        public Object getValue()
        {
            return this.value;
        }

        @Override
        public boolean isSelected()
        {
            return this.selected;
        }

        /**
         * Sets the label.
         *
         * @param _label the label
         * @return the range value option
         */
        public RangeValueOption setLabel(final String _label)
        {
            this.label = _label;
            return this;
        }

        /**
         * Sets the selected.
         *
         * @param _selected the selected
         * @return the range value option
         */
        public RangeValueOption setSelected(final boolean _selected)
        {
            this.selected = _selected;
            return this;
        }

        /**
         * Sets the value.
         *
         * @param _value the value
         * @return the range value option
         */
        public RangeValueOption setValue(final Object _value)
        {
            this.value = _value;
            return this;
        }
    }
}
