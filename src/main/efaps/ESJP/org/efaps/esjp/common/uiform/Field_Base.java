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
package org.efaps.esjp.common.uiform;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Status.StatusGroup;
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
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.api.ui.IOption;
import org.efaps.api.ui.IUserInterface;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains basic methods used to render standard Fields that are not
 * based on an attribute.
 *
 * @author The eFaps Team
 */
@EFapsUUID("92337601-c2df-4f78-bb80-9c9b8b81c35c")
@EFapsApplication("eFaps-Kernel")
public abstract class Field_Base
    extends AbstractCommon
{

    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Field.class);

    /** Type of list to be rendered. */
    public enum ListType
    {
        /** CheckBox, DropDown, Simple List, Radio Buttons. */
        CHECKBOX, DROPDOWN, LIST, RADIO;
    }

    /**
     * Method to get a Datevalue for a field on create to set a more "intelligent"
     * value like "monday of current week" etc.
     * Properties:
     * <table>
     *  <tr><th>Property</th><th>Value</th><th>Description</th></tr>
     *  <tr><td>withDayOfWeek</td><td>1,2,3,4,5,6,7</td>
     *      <td>the Integer represents on of the weekdays starting with Monday, Tuesday...</td></tr>
     *  <tr><td>withDayOfMonth</td><td>Integer</td><td>day of month</td></tr>
     *  <tr><td>minusDays</td><td>Integer</td><td>days to subtract</td></tr>
     *  <tr><td>plusDays</td><td>Integer</td><td>days to add</td></tr>
     *  <tr><td>minusWeeks</td><td>Integer</td><td>weeks to subtract</td></tr>
     *  <tr><td>plusWeeks</td><td>Integer</td><td>weeks to add</td></tr>
     * </table>
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return ReturnValue containing the date
     * @throws EFapsException on error
     */
    public Return getDefault4DateFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);
        final Collection<TargetMode> modes = new ArrayList<>();
        for (final String aMode : analyseProperty(_parameter, "TargetMode").values()) {
            modes.add(EnumUtils.getEnum(TargetMode.class, aMode.toUpperCase()));
        }
        if ((TargetMode.CREATE.equals(mode) || TargetMode.EDIT.equals(mode))
                        && (modes.isEmpty() || modes.contains(mode))) {
            DateTime date = new DateTime();
            if (containsProperty(_parameter, "withDayOfWeek")) {
                final int dayOfWeek = Integer.parseInt(getProperty(_parameter, "withDayOfWeek"));
                date = date.withDayOfWeek(dayOfWeek);
            }
            if (containsProperty(_parameter, "withDayOfMonth")) {
                final int dayOfMonth = Integer.parseInt(getProperty(_parameter, "withDayOfMonth"));
                date = date.withDayOfMonth(dayOfMonth);
            }
            if (containsProperty(_parameter, "days")) {
                final int days = Integer.parseInt(getProperty(_parameter, "days"));
                date = date.plusDays(days);
            }
            if (containsProperty(_parameter, "weeks")) {
                final int weeks = Integer.parseInt(getProperty(_parameter, "weeks"));
                date = date.plusWeeks(weeks);
            }
            if (containsProperty(_parameter, "months")) {
                final int months = Integer.parseInt(getProperty(_parameter, "months"));
                date = date.plusMonths(months);
            }
            if (containsProperty(_parameter, "years")) {
                final int years = Integer.parseInt(getProperty(_parameter, "years"));
                date = date.plusYears(years);
            }
            ret.put(ReturnValues.VALUES, date);
        }
        return ret;
    }

    /**
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Boolean value
     * @throws EFapsException on error
     */
    public Return getDefault4BooleanValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, BooleanUtils.toBoolean(getProperty(_parameter, "DefaultValue")));
        return ret;
    }

    /**
    *
    * @param _parameter    Parameter as passed from the eFaps API
    * @return Boolean value
    * @throws EFapsException on error
    */
    public Return getDefault4BooleanSysConfigFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final String config = getProperty(_parameter, "SystemConfiguration");
        final String attribute = getProperty(_parameter, "AttributeConfiguration");
        final String keyValue = getProperty(_parameter, "Key");

        final SystemConfiguration sysConf = SystemConfiguration.get(config);
        final Properties properties = sysConf.getAttributeValueAsProperties(attribute, true);
        final String key = properties.getProperty(keyValue);
        if (key != null) {
            ret.put(ReturnValues.VALUES, BooleanUtils.toBoolean(key));
        }
        return ret;
    }

    /**
     * Gets a lazy field value. "Very Slow!!"
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the lazy field value
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public Return getLazyFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        Map<Instance, Object> values = new HashMap<>();
        if (_parameter.get(ParameterValues.REQUEST_INSTANCES) != null) {
            final UIValue uiValue = (UIValue)_parameter.get(ParameterValues.UIOBJECT);
            final String key = uiValue.getField().getId() + ".LazyFieldValue";
            if (Context.getThreadContext().containsRequestAttribute(key)) {
                values = (Map<Instance, Object>) Context.getThreadContext().getRequestAttribute(key);
            } else {
                Context.getThreadContext().setRequestAttribute(key, values);
                final List<Instance> instances = (List<Instance>) _parameter.get(ParameterValues.REQUEST_INSTANCES);
                final ListValuedMap<Type, Instance> map = MultiMapUtils.newListValuedHashMap();
                final Map<Integer, String> types = analyseProperty(_parameter, "Type");
                final Map<Integer, String> selects4Type = analyseProperty(_parameter, "Select4Type");
                final Map<Integer, String> selects = analyseProperty(_parameter, "Select");
                if (!selects4Type.isEmpty()) {
                    for (final String select4Type : selects4Type.values()) {
                        final MultiPrintQuery multi = CachedMultiPrintQuery.get4Request(instances);
                        multi.addSelect(select4Type);
                        multi.execute();
                        while (multi.next()) {
                            map.put(multi.getSelect(select4Type), multi.getCurrentInstance());
                        }
                    }
                } else {
                    instances.forEach(inst -> map.put(inst.getType(), inst));
                }
                for (final Entry<Type, Collection<Instance>> typeEntry : map.asMap().entrySet()) {
                    for (final Entry<Integer, String> entry : types.entrySet()) {
                        final Type type;
                        if (isUUID(entry.getValue())) {
                            type = Type.get(UUID.fromString(entry.getValue()));
                        } else {
                            type = Type.get(entry.getValue());
                        }
                        if (typeEntry.getKey().isKindOf(type)) {
                            final String select = selects.get(entry.getKey());
                            final MultiPrintQuery multi = CachedMultiPrintQuery.get4Request(
                                            new ArrayList<>(typeEntry.getValue()));
                            multi.addSelect(select);
                            multi.execute();
                            while (multi.next()) {
                                values.put(multi.getCurrentInstance(), multi.getSelect(select));
                            }
                        }
                    }
                }
            }
        }

        Object value = null;
        final Instance instance = _parameter.getInstance();
        if (InstanceUtils.isValid(instance)) {
            if (values.containsKey(instance)) {
                value = values.get(instance);
            } else {
                final Map<Integer, String> types = analyseProperty(_parameter, "Type");
                final Map<Integer, String> selects = analyseProperty(_parameter, "Select");
                for (final Entry<Integer, String> entry : types.entrySet()) {
                    final Type type;
                    if (isUUID(entry.getValue())) {
                        type = Type.get(UUID.fromString(entry.getValue()));
                    } else {
                        type = Type.get(entry.getValue());
                    }
                    if (instance.getType().isKindOf(type)) {
                        final String select = selects.get(entry.getKey());
                        final PrintQuery print = CachedPrintQuery.get4Request(instance);
                        print.addSelect(select);
                        print.execute();
                        value = print.getSelect(select);
                        break;
                    }
                }
            }
        }
        ret.put(ReturnValues.VALUES, value);
        return ret;
    }

    /**
     * Gets the distinct values. Used for UI_FIELD_FORMAT.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the unique
     * @throws EFapsException on error
     */
    public Return distinct(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final UIValue uiValue = (UIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (uiValue.getDbValue() != null && uiValue.getDbValue() instanceof List) {
            final List<?> list = (List<?>) uiValue.getDbValue();
            final List<?> values = list.stream().distinct().collect(Collectors.toList());
            ret.put(ReturnValues.VALUES, values.size() == 1 ? values.get(0) : values);
        }
        return ret;
    }

    /**
     * Render a single checkbox.<br>
     * Properties:
     * <table>
     *  <tr><th>Property</th><th>Value</th><th>Default</th></tr>
     *  <tr><td>checked</td><td>true, false</td><td>false</td></tr>
     *  <tr><td>value</td><td>any String</td><td>"true"</td></tr>
     * </table>
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @return html snipplet for a checkbox
     * @throws EFapsException on error
     */
    public Return checkboxFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);

        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final boolean checked = "true".equalsIgnoreCase((String) props.get("checked"));
        final String value = props.containsKey("value") ? (String) props.get("value") : "true";

        html.append("<input type=\"checkbox\" name=\"").append(uiValue.getField().getName()).append("\" ")
                        .append(IUserInterface.EFAPSTMPTAG).append(" value=\"").append(value).append("\" ");
        if (checked) {
            html.append(" checked=\"checked\" ");
        }
        if (props.containsKey("comment")) {
            html.append(">")
                .append(DBProperties.getProperty((String) props.get("comment")))
                .append("</input>");
        } else {
            html.append("/>");
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @return  Return containing snipplet
     * @throws EFapsException on error
     */
    public Return radioFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((IUIValue) uiObject).getDisplay())) {
            final List<DropDownPosition> positions = new ArrayList<>();
            final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
            final Map<Integer, String> values = analyseProperty(_parameter, "Value");
            for (final String value : values.values()) {
                final StringBuilder propKey = new StringBuilder()
                                .append(uiValue.getField().getCollection().getName())
                                .append(".").append(uiValue.getField().getName()).append(".")
                                .append(value).append(".Label");
                final DropDownPosition pos = new DropDownPosition(value, DBProperties.getProperty(propKey
                                .toString()));
                positions.add(pos);
                if (value.equals(getProperty(_parameter, "Selected"))) {
                    pos.setSelected(true);
                }
            }
            ret.put(ReturnValues.SNIPLETT, getInputField(_parameter, positions, Field_Base.ListType.RADIO));
        } else {
            ret.put(ReturnValues.SNIPLETT, "");
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getRadioList(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof IUIValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((IUIValue) uiObject).getDisplay())) {
                ret = listFieldValue(_parameter, Field_Base.ListType.RADIO);
            } else {
                ret = new Return();
                ret.put(ReturnValues.SNIPLETT, "");
            }
        } else {
            ret = listFieldValue(_parameter, Field_Base.ListType.RADIO);
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getCheckBoxList(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof IUIValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((IUIValue) uiObject).getDisplay())) {
                ret = listFieldValue(_parameter, Field_Base.ListType.CHECKBOX);
            } else {
                ret = new Return();
                ret.put(ReturnValues.SNIPLETT, "");
            }
        } else {
            ret = listFieldValue(_parameter, Field_Base.ListType.CHECKBOX);
        }
        return ret;
    }

    /**
     * Renders a field that contains the values from a
     * SystemConfigurationObjectAttribute.
     * Properties:
     * <table>
     *  <tr><th>Property</th><th>Value</th><th>Obligatory</th></tr>
     *  <tr><td>SystemConfigurationUUID</td><td>UUID of the SystemConfiguration the
     *  ObjectAttribute will be search in.</td><td>true</td></tr>
     * </table>
      * @param _parameter    Parameter as passed from the eFaps API
     * @return html snipplet
     * @throws EFapsException on error
     */
    public Return systemConfigurationObjectFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final String configurationUUID = getProperty(_parameter, "SystemConfigurationUUID");
        if (configurationUUID != null) {
            final UUID uuid = UUID.fromString(configurationUUID);
            final SystemConfiguration config = SystemConfiguration.get(uuid);
            if (config != null) {
                final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
                final Properties confProps = config.getObjectAttributeValueAsProperties(_parameter.getCallInstance());
                final List<String> keys = new ArrayList<>(confProps.stringPropertyNames());
                Collections.sort(keys);
                if (Display.EDITABLE.equals(uiValue.getDisplay())) {
                    final StringBuilder propStr = new StringBuilder();
                    for (final String key : keys) {
                        propStr.append(key).append("=").append(confProps.getProperty(key)).append("\n");
                    }
                    ret.put(ReturnValues.VALUES, propStr.toString());
                } else {
                    final StringBuilder html = new StringBuilder();
                    html.append("<table>");
                    for (final String key : keys) {
                        html.append("<tr>")
                             .append("<td>").append(key).append("</td>")
                             .append("<td>").append(confProps.getProperty(key)).append("</td>")
                             .append("</tr>");
                    }
                    html.append("</table>");
                    ret.put(ReturnValues.SNIPLETT, html.toString());
                }
            }
        }
        return ret;
    }

    /**
     * Formats the values for a class select field used normally in tables.
     * Properties:
     * <table>
     *  <tr><th>Property</th><th>Value</th><th>Obligatory</th></tr>
     *  <tr><td>Seperator</td><td>Seperator used to seprerate the values, if more than one</td><td>false</td></tr>
     *  <tr><td>ClassSequence</td><td>ClassSequence used to show all the parent classifications,
     *  if more than one</td><td>true</td></tr>
     * </table>
      * @param _parameter    Parameter as passed from the eFaps API
     * @return formated object
     * @throws EFapsException on error
     */
    public Return classificationFieldFormat(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String seperator = props.containsKey("Seperator") ? (String) props.get("Seperator") : ", ";
        final boolean clazzSeq = props.containsKey("ClassSequence")
                                            ? Boolean.parseBoolean((String) props.get("ClassSequence")) : false;
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (fieldValue != null) {
            final Object value = fieldValue.getObject();
            if (value instanceof List) {
                final List<Classification> clazzes = new ArrayList<>();
                for (final Object val : (List<?>) value) {
                    clazzes.add((Classification) val);
                }
                //Collections.sort(labels);
                boolean first = true;
                for (Classification clazz : clazzes) {
                    if (first) {
                        first = false;
                    } else {
                        html.append(seperator);
                    }
                    if (clazzSeq) {
                        final Map<Integer, String> map = new TreeMap<>();
                        int cont = 99;
                        while (clazz != null) {
                            if (cont == 99) {
                                map.put(cont, clazz.getLabel());
                            } else {
                                map.put(cont, clazz.getLabel() + " - ");
                            }
                            clazz = clazz.getParentClassification();
                            cont--;
                        }
                        for (final String label : map.values()) {
                            html.append(label);
                        }
                    } else {
                        html.append(clazz.getLabel());
                    }
                }
            } else if (value instanceof Classification) {
                Classification clazz = (Classification) value;
                if (clazzSeq) {
                    final Map<Integer, String> map = new TreeMap<>();
                    int cont = 99;
                    while (clazz != null) {
                        if (cont == 99) {
                            map.put(cont, clazz.getLabel());
                        } else {
                            map.put(cont, clazz.getLabel() + " - ");
                        }
                        clazz = clazz.getParentClassification();
                        cont--;
                    }
                    for (final String label : map.values()) {
                        html.append(label);
                    }
                } else {
                    html.append(clazz.getLabel());
                }
            } else if (value instanceof String) {
                html.append(value);
            }
        }

        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, html.toString());
        return ret;
    }

    public Return getSignumFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue fieldvalue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final BigDecimal value = (BigDecimal) fieldvalue.getObject();
        if (value != null) {
            final Collection<String> types2negate = analyseProperty(_parameter, "Type2Negate").values() ;
            final Set<Type> types = new HashSet<>();
            for (final String type2negate : types2negate) {
                if (isUUID(type2negate)) {
                    types.add(Type.get(UUID.fromString(type2negate)));
                } else {
                    types.add(Type.get(type2negate));
                }
            }
            if (types.contains(fieldvalue.getInstance().getType())) {
                final BigDecimal retValue = value.negate();
                ret.put(ReturnValues.VALUES, retValue);
            }
        }
        return ret;
    }


    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return html snipplet
     * @throws EFapsException on error
     */
    public Return getStatusDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        new StringBuilder();
        final String statusGroupStr = getProperty(_parameter, "StatusGroup");
        final StatusGroup statusGroup;
        if (isUUID(statusGroupStr)) {
            statusGroup = Status.get(UUID.fromString(statusGroupStr));
        } else {
            statusGroup = Status.get(statusGroupStr);
        }
        final List<DropDownPosition> positions = new ArrayList<>();
        if ("true".equalsIgnoreCase(getProperty(_parameter, "AddWildcard4Search"))) {
            positions.add(getDropDownPosition(_parameter, "*", "*"));
        }

        for (final Status status : statusGroup.values()) {
            positions.add(getDropDownPosition(_parameter, status.getId(), status.getLabel()));
        }

        Collections.sort(positions, new Comparator<DropDownPosition>() {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
            }
        });
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, positions);
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return html snipplet
     * @throws EFapsException on error
     */
    public Return getTypeDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (Display.EDITABLE.equals(uiValue.getDisplay())) {
            final String selected = getProperty(_parameter, "SelectedType");
            final boolean includeAbstract = "true".equalsIgnoreCase(getProperty(_parameter, "IncludeAbstract"));
            final Map<Integer, String> types = analyseProperty(_parameter, "Type");
            final Map<Integer, String> excludeTypes = analyseProperty(_parameter, "ExcludeType");

            if (!types.isEmpty()) {
                final Set<Type> excludes = new HashSet<>();
                if (!excludeTypes.isEmpty()) {
                    for (final Entry<Integer, String> entryExclude : excludeTypes.entrySet()) {
                        final Type type4Exclude = Type.get(entryExclude.getValue());
                        if (type4Exclude != null) {
                            excludes.add(type4Exclude);
                        }
                    }
                }
                final Type selectedType = selected != null && !selected.isEmpty() ? Type.get(selected) : null;

                final List<DropDownPosition> positions = new ArrayList<>();
                for (final Entry<Integer, String> entryType  : types.entrySet()) {
                    final Set<Type> typeList = getTypeList(_parameter, Type.get(entryType.getValue()));
                    for (final Type type : typeList) {
                        if (!excludes.contains(type) && (!type.isAbstract() || includeAbstract)) {
                            final DropDownPosition pos = new DropDownPosition(type.getId(), type.getLabel(),
                                            type.getLabel());
                            positions.add(pos);
                            if (type.equals(selectedType)) {
                                pos.setSelected(true);
                            }
                        }
                    }
                }
                Collections.sort(positions, new Comparator<DropDownPosition>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public int compare(final DropDownPosition _o1,
                                       final DropDownPosition _o2)
                    {
                        return _o1.getOrderValue().compareTo(_o2.getOrderValue());
                    }
                });
                ret.put(ReturnValues.VALUES, positions);
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getClassificationDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (Display.EDITABLE.equals(uiValue.getDisplay())) {
            final Map<Integer, String> rootClasses = analyseProperty(_parameter, "Classification");
            final List<DropDownPosition> positions = new ArrayList<>();
            final Set<Classification> clazzList = new HashSet<>();
            for (final String clazzName : rootClasses.values()) {
                final Classification clazz;
                if (isUUID(clazzName)) {
                    clazz = Classification.get(UUID.fromString(clazzName));
                } else {
                    clazz = Classification.get(clazzName);
                }
                clazzList.add(clazz);
                clazzList.addAll(getChildClassifications(clazz));
            }

            final Map<Integer, String> types = analyseProperty(_parameter, "Type");
            for (final String typeName : types.values()) {
                final Type tmpType;
                if (isUUID(typeName)) {
                    tmpType = Type.get(UUID.fromString(typeName));
                } else {
                    tmpType = Type.get(typeName);
                }
                final Set<Type> typelist = getTypeList(_parameter, tmpType);
                for (final Type tmp : typelist) {
                    final Set<Classification> clazzes = tmp.getClassifiedByTypes();
                    for (final Classification clazz  : clazzes) {
                        clazzList.add(clazz);
                        clazzList.addAll(getChildClassifications(clazz));
                    }
                }
            }

            for (final Classification clazz : clazzList) {
                Classification tmp = clazz;
                String label = tmp.getLabel();
                while (tmp.getParentClassification() != null) {
                    tmp = tmp.getParentClassification();
                    label = tmp.getLabel() + " - " + label;
                }
                final DropDownPosition pos = new DropDownPosition(clazz.getId(), label, label);
                positions.add(pos);
                if (Long.valueOf(clazz.getId()).equals(uiValue.getObject())) {
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
            html.append(getDropDownField(_parameter, positions));
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * Get the list of child classifications.
     * @param _parent parent classification
     * @return list of classifications
     * @throws CacheReloadException on error
     */
    protected List<Classification> getChildClassifications(final Classification _parent)
        throws CacheReloadException
    {
        final List<Classification> ret = new ArrayList<>();
        for (final Classification child : _parent.getChildClassifications()) {
            ret.addAll(getChildClassifications(child));
            ret.add(child);
        }
        return ret;
    }
    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return dropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof IUIValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((IUIValue) uiObject).getDisplay())) {
                ret = listFieldValue(_parameter, Field_Base.ListType.DROPDOWN);
            } else {
                ret = new Return();
                ret.put(ReturnValues.SNIPLETT, "");
            }
        } else {
            ret = listFieldValue(_parameter, Field_Base.ListType.DROPDOWN);
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _listType     Type of Lit to be rendered
     * @return Return containing Html Snipplet
     *
     * @throws EFapsException on error
     */
    public Return listFieldValue(final Parameter _parameter,
                                 final ListType _listType)
        throws EFapsException
    {
        final String html;
        final String typeStr = getProperty(_parameter, "Type");
        final Type type = Type.get(typeStr);
        if (type != null) {
            final boolean includeChildTypes = !"false".equalsIgnoreCase(getProperty(_parameter, "ExpandChildTypes"));

            final QueryBuilder queryBldr = new QueryBuilder(type);
            final String linkfrom = getProperty(_parameter, "LinkFrom");
            if (linkfrom != null) {
                final Instance instance = _parameter.getInstance() == null
                                                ? _parameter.getCallInstance() : _parameter.getInstance();

                if (instance != null && instance.isValid()) {
                    queryBldr.addWhereAttrEqValue(linkfrom, instance);
                }
            }
            final String where = getProperty(_parameter, "WhereAttrEqValue");
            if (where != null) {
                final String[] parts = where.split("\\|");
                queryBldr.addWhereAttrEqValue(parts[0], parts[1]);
            }

            final List<Status> statusList = getStatusListFromProperties(_parameter);
            if (!statusList.isEmpty()) {
                queryBldr.addWhereAttrEqValue(type.getStatusAttribute().getName(), statusList.toArray());
            }

            add2QueryBuilder4List(_parameter, queryBldr);

            final InstanceQuery instQuery = queryBldr.getQuery();
            instQuery.setIncludeChildTypes(includeChildTypes);

            final MultiPrintQuery multi = new MultiPrintQuery(instQuery.execute());
            final String select = getProperty(_parameter, "Select");
            if (select != null) {
                multi.addSelect(select);
            }
            final String phrase = getProperty(_parameter, "Phrase");
            if (phrase != null) {
                multi.addPhrase("Phrase", phrase);
            }
            final String msgPhrase = getProperty(_parameter, "MsgPhrase");
            if (msgPhrase != null) {
                if (isUUID(msgPhrase)) {
                    multi.addMsgPhrase(UUID.fromString(msgPhrase));
                } else {
                    multi.addMsgPhrase(msgPhrase);
                }
            }
            final String valueSel = getProperty(_parameter, "ValueSelect");
            if (valueSel != null) {
                multi.addSelect(valueSel);
            }
            final String orderSel = getProperty(_parameter, "OrderSelect");
            if (orderSel != null) {
                multi.addSelect(orderSel);
            }

            multi.execute();
            Object dbValue = null;
            final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
            if (uiObject instanceof IUIValue) {
                dbValue = ((IUIValue) uiObject).getObject();
            }

            final List<DropDownPosition> values = new ArrayList<>();
            boolean selected = false;
            while (multi.next()) {
                final Object value;
                if (valueSel == null) {
                    value = multi.getCurrentInstance().getId();
                } else {
                    value = multi.getSelect(valueSel);
                }
                Object option = null;
                if (select != null) {
                    option = multi.getSelect(select);
                } else if (phrase != null) {
                    option = multi.getPhrase("Phrase");
                } else if (msgPhrase != null) {
                    if (isUUID(msgPhrase)) {
                        option = multi.getMsgPhrase(UUID.fromString(msgPhrase));
                    } else {
                        option = multi.getMsgPhrase(msgPhrase);
                    }
                }
                final DropDownPosition val = getDropDownPosition(_parameter, value, option);
                values.add(val);
                if (orderSel != null) {
                    val.setOrderValue((Comparable<?>) multi.getSelect(orderSel));
                }
                // evaluate for selected only until the first is found
                if (!selected) {
                    if (dbValue != null && "true".equalsIgnoreCase(getProperty(_parameter, "SetSelected"))) {
                        if (dbValue.equals(val.value)) {
                            val.setSelected(true);
                            selected = true;
                        } else if (val.value instanceof String && Instance.get((String) val.value).isValid()
                                        && dbValue.equals(Instance.get((String) val.value).getId())) {
                            val.setSelected(true);
                            selected = true;
                        }
                    } else if (containsProperty(_parameter, "Regex4DefaultValue")) {
                        if (String.valueOf(val.getOption()).matches(getProperty(_parameter, "Regex4DefaultValue"))) {
                            val.setSelected(true);
                            selected = true;
                        }
                    } else if (containsProperty(_parameter, "DefaultSysConf")) {
                        final String sysconf = getProperty(_parameter, "DefaultSysConf");
                        final SystemConfiguration conf;
                        if (isUUID(sysconf)) {
                            conf = SystemConfiguration.get(UUID.fromString(sysconf));
                        } else {
                            conf = SystemConfiguration.get(sysconf);
                        }
                        if (containsProperty(_parameter, "DefaultLink")) {
                            final Instance inst = conf.getLink(getProperty(_parameter, "DefaultLink"));
                            if (inst != null && inst.isValid()) {
                                if (val.value instanceof String && Instance.get((String) val.value).isValid()
                                                && Instance.get((String) val.value).getOid().equals(inst.getOid())) {
                                    val.setSelected(true);
                                    selected = true;
                                } else if (val.value instanceof Long && ((Long) val.value).equals(inst.getId())) {
                                    val.setSelected(true);
                                    selected = true;
                                }
                            }
                        }
                    }
                }
            }
            if (containsProperty(_parameter, "emptyValue")) {
                values.add(0, new DropDownPosition("",
                                DBProperties.getProperty(getProperty(_parameter, "emptyValue"))));
            }

            if (!BooleanUtils.toBoolean(getProperty(_parameter, "NoSort"))) {
                Collections.sort(values, new Comparator<DropDownPosition>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public int compare(final DropDownPosition _o1,
                                       final DropDownPosition _o2)
                    {
                        return _o1.getOrderValue().compareTo(_o2.getOrderValue());
                    }
                });
            }

            updatePositionList(_parameter, values);

            switch (_listType) {
                case DROPDOWN:
                    html = getDropDownField(_parameter, values).toString();
                    break;
                case CHECKBOX:
                    html = getInputField(_parameter, values, _listType).toString();
                    break;
                case RADIO:
                    html = getInputField(_parameter, values, _listType).toString();
                    break;
                default:
                    html = "";
                    break;
            }
        } else {
            html = "";
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html);
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @param _values   list of dropdownposition
     * @throws EFapsException on error
     */
    protected void updatePositionList(final Parameter _parameter,
                                      final List<DropDownPosition> _values)
        throws EFapsException
    {
     // to be implemented by subclasses
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _queryBldr    QueryBuilder the criteria will be added to
     * @throws EFapsException on error
     */
    protected void add2QueryBuilder4List(final Parameter _parameter,
                                         final QueryBuilder _queryBldr)
        throws EFapsException
    {
        // to be implemented by subclasses
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @param _values   list of DropDownValue
     * @return StrignBuilder
     * @throws EFapsException on error
     */
    public StringBuilder getDropDownField(final Parameter _parameter,
                                          final List<DropDownPosition> _values)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String fieldName;
        if (containsProperty(_parameter, "FieldName")) {
            fieldName = getProperty(_parameter, "FieldName");
        } else if (fieldValue != null) {
            fieldName =  fieldValue.getField().getName();
        } else {
            fieldName = "eFapsDropDown";
        }
        html.append("<select name=\"").append(fieldName)
            .append("\" size=\"1\"").append(IUserInterface.EFAPSTMPTAG).append(">");
        for (final DropDownPosition value : _values) {
            html.append("<option value=\"").append(value.getValue()).append("\"");
            if (value.isSelected()) {
                html.append(" selected=\"selected\"");
            }
            html.append(">").append(value.getOption())
                .append("</option>");
        }
        html.append("</select>");
        return html;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @param _values   list of DropDownValue
     * @param _listType type of list
     * @return StrignBuilder
     * @throws EFapsException on error
     */
    public StringBuilder getInputField(final Parameter _parameter,
                                       final List<DropDownPosition> _values,
                                       final ListType _listType)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        final String name;
        final String fieldName = getProperty(_parameter, "FieldName");
        final String horizontal = getProperty(_parameter, "Horizontal");
        if (fieldName != null) {
            name = fieldName;
        } else if (uiObject instanceof IUIValue) {
            name = ((IUIValue) uiObject).getField().getName();
        } else if (uiObject instanceof org.efaps.admin.ui.field.Field) {
            name = ((org.efaps.admin.ui.field.Field) uiObject).getName();
        } else {
            name = "eFapsCheckBoxes";
        }

        for (final DropDownPosition value : _values) {
            final String id = RandomUtil.randomAlphabetic(4);
            html.append("<input id=\"").append(id).append("\" type=\"")
                .append(_listType.equals(Field_Base.ListType.CHECKBOX) ? "checkbox" : "radio")
                .append("\" value=\"").append(value.getValue()).append("\" name=\"")
                .append(name).append("\"");
            if (value.isSelected()) {
                html.append(" checked=\"checked\"");
            }
            html.append(" /><label for=\"").append(id).append("\">").append(value.getOption()).append("</label>");
            if (horizontal == null) {
                html.append("<br/>");
            }
        }
        return html;
    }

    /**
     * Get a new DropDownValue instance.
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _value        value
     * @param _option       option
     * @return new DropDownValue
     * @throws EFapsException on error
     */
    public DropDownPosition getDropDownPosition(final Parameter _parameter,
                                                final Object _value,
                                                final Object _option)
        throws EFapsException
    {
        return new DropDownPosition(_value, _option);
    }
    /**
     * Method can be executed as FieldValue to store the "selectRow" Values in
     * the Context for further use. (e.g a User selects some Objects, a form is
     * opened fur further information and than the information of the selected
     * Object is needed).
     * @param _parameter Parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return getStoreOIDsFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        if (_parameter.getParameterValues("selectedRow") != null) {
            final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
            Context.getThreadContext().setSessionAttribute(fieldValue.getField().getName(),
                        _parameter.getParameterValues("selectedRow"));
        } else if ("true".equalsIgnoreCase(getProperty(_parameter, "RemoveSessionAttr"))) {
            final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
            Context.getThreadContext().removeSessionAttribute(fieldValue.getField().getName());
        }
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Snipplet
     * @throws EFapsException on error
     */
    public Return getJSLinkField(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final StringBuilder html = new StringBuilder();
        html.append("<script type=\"text/javascript\" src=\"")
            .append(Context.getThreadContext().getPath()).append(props.get("link"))
            .append("\"></script>");
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
    * get a drop down of UoM for edit mode.
    *
    * @param _parameter Parameter as passed from the eFaps API
    * @return empty Return
    * @throws EFapsException on error
    */
    public Return getUoMDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        if ((TargetMode.EDIT.equals(mode) || TargetMode.CREATE.equals(mode)) && uiValue.getField().isEditableDisplay(
                        mode)) {
            if (uiValue.getDisplay().equals(Display.EDITABLE)) {
                final List<DropDownPosition> positions = new ArrayList<>();
                if (uiValue.getObject() != null && uiValue.getObject() instanceof Long) {
                    final UoM uomValue = Dimension.getUoM((Long) uiValue.getObject());
                    if (uomValue != null) {
                        final Dimension dim = uomValue.getDimension();
                        for (final UoM uom : dim.getUoMs()) {
                            final DropDownPosition position = getDropDownPosition(_parameter, uom.getId(), uom
                                            .getName());
                            positions.add(position);
                            position.setSelected(uomValue.equals(uom));
                        }
                    }
                } else {
                    final Map<Integer, String> dimensions = analyseProperty(_parameter, "Dimension");
                    for (final String dimension : dimensions.values()) {
                        final Dimension dim;
                        if (isUUID(dimension)) {
                            dim = Dimension.get(UUID.fromString(dimension));
                        } else {
                            dim = Dimension.get(dimension);
                        }
                        for (final UoM uom : dim.getUoMs()) {
                            final DropDownPosition position = getDropDownPosition(_parameter, uom.getId(), uom
                                            .getName());
                            positions.add(position);
                            position.setSelected(dim.getBaseUoM().equals(uom));
                        }
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
                if (!positions.isEmpty()) {
                    ret.put(ReturnValues.VALUES, positions);
                }
            } else {
                if (uiValue.getObject() != null && uiValue.getObject() instanceof Long) {
                    final UoM uomValue = Dimension.getUoM((Long) uiValue.getObject());
                    if (uomValue != null) {
                        ret.put(ReturnValues.VALUES, uomValue.getName());
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Get a new UUID and fill the given field "TargetField" with it.<br/>
     *
     * &lt;trigger program=&quot;org.efaps.esjp.common.uiform.Field&quot;
     * name=&quot;Products_ProductForm.createUUID.UI_FIELD_CMD&quot;
     * event=&quot;UI_FIELD_CMD&quot; method=&quot;createUUID&quot;&gt;<br/>
     * &lt;property name=&quot;TargetField&quot;&gt;uUID&lt;/property&gt;<br/>
     * &lt;/trigger&gt;
     *
     * @param _parameter Parameters as passed by the efapas API
     * @return return containig script with UUID
     */
    public Return createUUID(final Parameter _parameter)
    {
        final Return ret = new Return();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String targetField = (String) props.get("TargetField");

        if (targetField != null && !targetField.isEmpty()) {
            final StringBuilder html = new StringBuilder();
            html.append("document.getElementsByName(\"").append(targetField).append("\")[0].value=\"")
                .append(UUID.randomUUID())
                .append("\";");
            ret.put(ReturnValues.SNIPLETT, html.toString());
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return html snipplet presenting the values
     * @throws EFapsException on error
     */
    public Return getDBPropertiesFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final Map<Integer, String> values = analyseProperty(_parameter, "DBProperty");
        String seperator = getProperty(_parameter, "Seperator");
        if (seperator == null) {
            seperator = "<br\\>";
        }
        boolean first = true;
        for (final String value : values.values()) {
            if (first) {
                first = false;
            } else {
                html.append(seperator);
            }
            html.append(DBProperties.getProperty(value));
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return html snipplet presenting the values
     * @throws EFapsException on error
     */
    public Return getEmptyFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        // if the call instance is the same as the instance do not show
        if ("true".equalsIgnoreCase(getProperty(_parameter, "check4InstanceNotCallInstance"))) {
            final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
            if (uiValue.getInstance().equals(uiValue.getCallInstance())) {
                ret.put(ReturnValues.VALUES, "");
            }
        } else {
            ret.put(ReturnValues.SNIPLETT, "&nbsp;");
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getOptionList4Properties(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> positions = new ArrayList<>();
        final int selected;
        if (containsProperty(_parameter, "Selected")) {
            selected = Integer.parseInt(getProperty(_parameter, "Selected"));
        } else {
            selected = -1;
        }
        final Map<Integer, String> dbProps = analyseProperty(_parameter, "DBProperty");
        final Map<Integer, String> values = analyseProperty(_parameter, "Value");
        for (final Entry<Integer, String> entry : dbProps.entrySet()) {
            final DropDownPosition pos = getDropDownPosition(_parameter, values.get(entry.getKey()),
                            DBProperties.getProperty(entry.getValue()));
            pos.setSelected(entry.getKey() == selected);
            positions.add(pos);
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

        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, positions);
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getOptionList4DateTime(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> positions = new ArrayList<>();
        final String dateFieldType = getProperty(_parameter, "DateFieldType", "YEAR");
        switch (dateFieldType) {
            case "MONTH":
                for (final Month month : Month.values()) {
                    final DropDownPosition pos = getDropDownPosition(_parameter, month.getValue(),
                                    month.getDisplayName(TextStyle.FULL, Context.getThreadContext().getLocale()));
                    pos.setSelected(month.getValue() == new DateTime().getMonthOfYear());
                    positions.add(pos);
                }
                break;
            case "YEAR":
            default:
                final String fromStr = getProperty(_parameter, "From", "-10");
                final String toStr = getProperty(_parameter, "To", "+10");
                LocalDate start;
                if (StringUtils.isNumeric(fromStr)) {
                    start = LocalDate.of(Integer.parseInt(fromStr), 1, 1);
                } else {
                    start = LocalDate.now().plusYears(Integer.parseInt(fromStr));
                }
                final LocalDate end;
                if (StringUtils.isNumeric(toStr)) {
                    end = LocalDate.of(Integer.parseInt(toStr), 1, 1);
                } else {
                    end = LocalDate.now().plusYears(Integer.parseInt(toStr));
                }
                while (start.isBefore(end)) {
                    final DropDownPosition pos = getDropDownPosition(_parameter, start.getYear(), start.getYear());
                    pos.setSelected(start.getYear() == new DateTime().getYear());
                    positions.add(pos);
                    start = start.plusYears(1);
                }
                break;
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, positions);
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getOptionList4Enum(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> values = new ArrayList<>();
        final String enumName = getProperty(_parameter, "Enum");
        if (enumName != null) {
            final boolean orderByOrdinal = "true".equalsIgnoreCase(getProperty(_parameter, "OrderByOrdinal"));
            try {
                final Class<?> enumClazz = Class.forName(enumName);

                if (enumClazz.isEnum()) {
                    final Object[] consts = enumClazz.getEnumConstants();
                    Integer selected = -1;

                    final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
                    if (uiObject instanceof IUIValue && ((IUIValue) uiObject).getObject() != null) {
                        selected = (Integer) ((IUIValue) uiObject).getObject();
                    } else if (containsProperty(_parameter, "DefaultValue")) {
                        final String defaultValue = getProperty(_parameter, "DefaultValue");
                        for (final Object con : consts) {
                            if (((Enum<?>) con).name().equals(defaultValue)) {
                                selected = ((Enum<?>) con).ordinal();
                                break;
                            }
                        }
                    }
                    if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((IUIValue) uiObject).getDisplay())) {
                        for (final Object con : consts) {
                            final Enum<?> enumVal = (Enum<?>) con;
                            final Integer ordinal = enumVal.ordinal();
                            final String label = DBProperties.getProperty(enumName + "." + enumVal.name());
                            final DropDownPosition pos = new DropDownPosition(ordinal, label, orderByOrdinal ? ordinal
                                            : label);
                            values.add(pos);
                            pos.setSelected(ordinal == selected);
                        }
                        Collections.sort(values, new Comparator<DropDownPosition>()
                        {

                            @SuppressWarnings("unchecked")
                            @Override
                            public int compare(final DropDownPosition _o1,
                                               final DropDownPosition _o2)
                            {
                                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
                            }
                        });
                    }
                }
            } catch (final ClassNotFoundException e) {
                Field_Base.LOG.error("ClassNotFoundException", e);
            }
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return getLabel4Enum(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final String enumName = getProperty(_parameter, "Enum");
        if (enumName != null) {
            try {
                final Class<?> enumClazz = Class.forName(enumName);
                if (enumClazz.isEnum()) {
                    final Object[] consts = enumClazz.getEnumConstants();
                    final Integer ordinal;
                    final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
                    if (uiObject instanceof IUIValue && ((IUIValue) uiObject).getObject() != null) {
                        ordinal = (Integer) ((IUIValue) uiObject).getObject();
                        final String label = DBProperties.getProperty(enumName + "." + consts[ordinal].toString());
                        ret.put(ReturnValues.VALUES, label);
                    }
                }
            } catch (final ClassNotFoundException e) {
                Field_Base.LOG.error("ClassNotFoundException", e);
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     *
     * @throws EFapsException on error
     */
    public Return getOptionListFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
        add2QueryBuilder4List(_parameter, queryBldr);

        final MultiPrintQuery multi = queryBldr.getCachedPrint4Request();
        final String select = getProperty(_parameter, "Select");
        if (select != null) {
            multi.addSelect(select);
        }
        final String phrase = getProperty(_parameter, "Phrase");
        if (phrase != null) {
            multi.addPhrase("Phrase", phrase);
        }
        final String msgPhrase = getProperty(_parameter, "MsgPhrase");
        if (msgPhrase != null) {
            if (isUUID(msgPhrase)) {
                multi.addMsgPhrase(UUID.fromString(msgPhrase));
            } else {
                multi.addMsgPhrase(msgPhrase);
            }
        }
        final String valueSel = getProperty(_parameter, "ValueSelect");
        if (valueSel != null) {
            multi.addSelect(valueSel);
        }
        final String orderSel = getProperty(_parameter, "OrderSelect");
        if (orderSel != null) {
            multi.addSelect(orderSel);
        }

        multi.execute();
        Object dbValue = null;
        final IUIValue uiObject = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof UIValue) {
            dbValue = ((UIValue) uiObject).getDbValue();
        } else {
            dbValue = _parameter.get(ParameterValues.OTHERS);
        }

        final List<DropDownPosition> values = new ArrayList<>();
        boolean selected = false;
        while (multi.next()) {
            final Object value;
            if (valueSel == null) {
                value = multi.getCurrentInstance().getId();
            } else {
                value = multi.getSelect(valueSel);
            }
            Object option = null;
            if (select != null) {
                option = multi.getSelect(select);
            } else if (phrase != null) {
                option = multi.getPhrase("Phrase");
            } else if (msgPhrase != null) {
                if (isUUID(msgPhrase)) {
                    option = multi.getMsgPhrase(UUID.fromString(msgPhrase));
                } else {
                    option = multi.getMsgPhrase(msgPhrase);
                }
            }
            final DropDownPosition val = getDropDownPosition(_parameter, value, option);
            values.add(val);
            if (orderSel != null) {
                val.setOrderValue((Comparable<?>) multi.getSelect(orderSel));
            }
            // evaluate for selected only until the first is found
            if (!selected) {
                if (dbValue != null && "true".equalsIgnoreCase(getProperty(_parameter, "SetSelected"))) {
                    if (dbValue.equals(val.value)) {
                        val.setSelected(true);
                        selected = true;
                    } else if (val.value instanceof String && Instance.get((String) val.value).isValid()
                                    && dbValue.equals(Instance.get((String) val.value).getId())) {
                        val.setSelected(true);
                        selected = true;
                    }
                } else if (containsProperty(_parameter, "Regex4DefaultValue")) {
                    if (String.valueOf(val.getOption()).matches(getProperty(_parameter, "Regex4DefaultValue"))) {
                        val.setSelected(true);
                        selected = true;
                    }
                }  else if (containsProperty(_parameter, "DefaultSysConf")) {
                    final String sysconf = getProperty(_parameter, "DefaultSysConf");
                    final SystemConfiguration conf;
                    if (isUUID(sysconf)) {
                        conf = SystemConfiguration.get(UUID.fromString(sysconf));
                    } else {
                        conf = SystemConfiguration.get(sysconf);
                    }
                    if (containsProperty(_parameter, "DefaultLink")) {
                        final Instance inst = conf.getLink(getProperty(_parameter, "DefaultLink"));
                        if (inst != null && inst.isValid()) {
                            if (val.value instanceof String && Instance.get((String) val.value).isValid()
                                            && Instance.get((String) val.value).getOid().equals(inst.getOid())) {
                                val.setSelected(true);
                                selected = true;
                            } else if (val.value instanceof Long && ((Long) val.value).equals(inst.getId())) {
                                val.setSelected(true);
                                selected = true;
                            }
                        }
                    }
                }
            }
        }
        if (containsProperty(_parameter, "EmptyValue")) {
            values.add(0, new DropDownPosition("",
                            DBProperties.getProperty(getProperty(_parameter, "EmptyValue"))));
        }

        if (!BooleanUtils.toBoolean(getProperty(_parameter, "NoSort"))) {
            Collections.sort(values, new Comparator<DropDownPosition>()
            {

                @SuppressWarnings("unchecked")
                @Override
                public int compare(final DropDownPosition _o1,
                                   final DropDownPosition _o2)
                {
                    return _o1.getOrderValue().compareTo(_o2.getOrderValue());
                }
            });
        }
        updatePositionList(_parameter, values);
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * Auto complete.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return autoComplete(final Parameter _parameter) throws EFapsException
    {
        final List<Map<String, String>> list = new ArrayList<>();
        final String input = (String) _parameter.get(ParameterValues.OTHERS);

        final String searchAttribute = getProperty(_parameter, "SearchAttribute");
        final String select4KEY = getProperty(_parameter, "Select4KEY", "attribute[OID]");

        final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
        queryBldr.addWhereAttrMatchValue(searchAttribute, input + "*").setIgnoreCase(true);

        final MultiPrintQuery multi = queryBldr.getPrint();

        String select4VALUE = null;
        String phrase4VALUE = null;
        if (containsProperty(_parameter, "Select4VALUE")) {
            select4VALUE = getProperty(_parameter, "Select4VALUE");
            multi.addSelect(select4VALUE);
        } else if (containsProperty(_parameter, "Phrase4VALUE")) {
            phrase4VALUE = getProperty(_parameter, "Phrase4VALUE");
            multi.addPhrase("Phrase4VALUE", phrase4VALUE);
        }
        String select4CHOICE = select4VALUE;
        String phrase4CHOICE = phrase4VALUE;
        if (containsProperty(_parameter, "Select4CHOICE")) {
            select4CHOICE = getProperty(_parameter, "Select4CHOICE");
        } else if (containsProperty(_parameter, "Phrase4CHOICE")) {
            phrase4CHOICE = getProperty(_parameter, "Phrase4CHOICE");
        }

        if (select4CHOICE != null) {
            multi.addSelect(select4CHOICE);
        } else if (phrase4CHOICE != null) {
            multi.addPhrase("Phrase4CHOICE", phrase4CHOICE);
        }

        multi.addSelect(select4KEY);
        multi.execute();
        while (multi.next()) {
            final String keyVal = multi.getSelect(select4KEY);
            final String valueVal = select4VALUE == null ? multi.getPhrase("Phrase4VALUE")
                            : multi.getSelect(select4VALUE);
            final String choiceVal = select4CHOICE == null ? multi.getPhrase("Phrase4CHOICE")
                            : multi.getSelect(select4CHOICE);
            final Map<String, String> map = new HashMap<>();
            map.put("eFapsAutoCompleteKEY", keyVal);
            map.put("eFapsAutoCompleteVALUE", valueVal);
            map.put("eFapsAutoCompleteCHOICE", choiceVal);
            list.add(map);
        }
        Collections.sort(list, (_arg0, _arg1) -> _arg0.get("eFapsAutoCompleteCHOICE").compareTo(_arg1.get(
                        "eFapsAutoCompleteCHOICE")));
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, list);
        return retVal;
    }


    /**
     * A position in a dropdown.
     */
    public static class DropDownPosition
        implements IOption
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Value of the dropdown position.
         */
        private Object value;

        /**
         * Option of the dropdown position.
         */
        private Object option;

        /**
         * Value to be used to order the dropdown.
         */
        private Comparable<?> orderValue;

        /**
         * Is this position selected.
         */
        private boolean selected = false;

        /**
         * @param _value value
         * @param _option option
         */
        public DropDownPosition(final Object _value,
                                final Object _option)
        {
            this(_value, _option, null);
        }

        /**
         * @param _value value
         * @param _option option
         * @param _orderValue value to be ordered by
         */
        public DropDownPosition(final Object _value,
                                final Object _option,
                                final Comparable<?> _orderValue)
        {
            value = _value;
            option = _option;
            orderValue = _orderValue;
        }

        /**
         * Is this position selected.
         * @return false
         */
        @Override
        public boolean isSelected()
        {
            return selected;
        }

        /**
         * Setter method for instance variable {@link #selected}.
         *
         * @param _selected value for instance variable {@link #selected}
         * @return the drop down position
         */
        public DropDownPosition setSelected(final boolean _selected)
        {
            selected = _selected;
            return this;
        }

        /**
         * Setter method for instance variable {@link #option}.
         *
         * @param _option value for instance variable {@link #option}
         */
        public void setOption(final Object _option)
        {
            option = _option;
        }

        /**
         * Getter method for the instance variable {@link #option}.
         *
         * @return value of instance variable {@link #option}
         */
        public Object getOption()
        {
            return option;
        }

        /**
         * Setter method for instance variable {@link #value}.
         *
         * @param _value value for instance variable {@link #value}
         */

        public void setValue(final Object _value)
        {
            value = _value;
        }

        /**
         * Getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        @Override
        public Object getValue()
        {
            return value;
        }

        /**
         * Setter method for instance variable {@link #orderValue}.
         *
         * @param _orderValue value for instance variable {@link #orderValue}
         */

        public void setOrderValue(final Comparable<?> _orderValue)
        {
            orderValue = _orderValue;
        }

        /**
         * Getter method for the instance variable {@link #orderValue}.
         *
         * @return value of instance variable {@link #orderValue}
         */
        @SuppressWarnings("rawtypes")
        public Comparable getOrderValue()
        {
            return orderValue == null ? option.toString() : orderValue;
        }

        @Override
        public String getLabel()
        {
            return String.valueOf(getOption());
        }
    }
}
