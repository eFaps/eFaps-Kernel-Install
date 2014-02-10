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

package org.efaps.esjp.common.uiform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains basic methods used to render standard Fields that are not
 * based on an attribute.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("92337601-c2df-4f78-bb80-9c9b8b81c35c")
@EFapsRevision("$Rev$")
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
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return ReturnValue containing the date
     */
    public Return getDefault4DateFieldValue(final Parameter _parameter)
    {
        final Return ret = new Return();
        final TargetMode mode  = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);
        if (TargetMode.CREATE.equals(mode) || TargetMode.EDIT.equals(mode)) {
            final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            DateTime date = new DateTime();
            if (props.containsKey("withDayOfWeek")) {
                final int dayOfWeek = Integer.parseInt((String) props.get("withDayOfWeek"));
                date = date.withDayOfWeek(dayOfWeek);
            }
            if (props.containsKey("withDayOfMonth")) {
                final int dayOfMonth = Integer.parseInt((String) props.get("withDayOfMonth"));
                date = date.withDayOfMonth(dayOfMonth);
            }
            if (props.containsKey("days")) {
                final int days = Integer.parseInt((String) props.get("days"));
                date = date.minusDays(days);
            }
            if (props.containsKey("weeks")) {
                final int weeks = Integer.parseInt((String) props.get("weeks"));
                date = date.minusWeeks(weeks);
            }
            if (props.containsKey("months")) {
                final int months = Integer.parseInt((String) props.get("months"));
                date = date.plusMonths(months);
            }
            if (props.containsKey("years")) {
                final int years = Integer.parseInt((String) props.get("years"));
                date = date.plusYears(years);
            }
            ret.put(ReturnValues.VALUES, date);
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final boolean checked = "true".equalsIgnoreCase((String) props.get("checked"));
        final String value = props.containsKey("value") ? (String) props.get("value") : "true";

        html.append("<input type=\"checkbox\" name=\"").append(fieldValue.getField().getName()).append("\" ")
                        .append(UIInterface.EFAPSTMPTAG).append(" value=\"").append(value).append("\" ");
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
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((FieldValue) uiObject).getDisplay())) {
            final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
            if (properties.containsKey("values")) {
                final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
                final String valuesStr = String.valueOf(properties.get("values"));
                final String[] values = valuesStr.split(";");
                for (final String value : values) {
                    final StringBuilder propKey = new StringBuilder()
                                    .append(fieldValue.getField().getCollection().getName())
                                    .append(".").append(fieldValue.getField().getName()).append(".")
                                    .append(value).append(".Label");
                    final DropDownPosition pos = new DropDownPosition(value, DBProperties.getProperty(propKey
                                    .toString()));
                    positions.add(pos);
                    if (properties.containsKey("selected") && value.equals(properties.get("selected"))) {
                        pos.setSelected(true);
                    }
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
        Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof FieldValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((FieldValue) uiObject).getDisplay())) {
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
    public Return getRadioList4Enum(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof FieldValue) {
            final String enumName = getProperty(_parameter, "Enum");
            if (enumName != null) {
                final boolean orderByOrdinal = "true".equalsIgnoreCase(getProperty(_parameter, "OrderByOrdinal"));
                try {
                    final Class<?> enumClazz = Class.forName(enumName);
                    if (enumClazz.isEnum()) {
                        final Object[] consts = enumClazz.getEnumConstants();
                        Integer ordinal;
                        if (((FieldValue) uiObject).getValue() != null) {
                            ordinal = (Integer) ((FieldValue) uiObject).getValue();
                        } else {
                            ordinal = -1;
                        }
                        if (org.efaps.admin.ui.field.Field.Display.EDITABLE
                                        .equals(((FieldValue) uiObject).getDisplay())) {
                            final List<DropDownPosition> values = new ArrayList<DropDownPosition>();
                            int i = 0;
                            for (final Object con : consts) {
                                final String label = DBProperties.getProperty(enumName + "." + con.toString());
                                final DropDownPosition pos = new DropDownPosition(i, label, orderByOrdinal
                                                ? new Integer(i) : label);
                                values.add(pos);
                                pos.setSelected(i == ordinal);
                                i++;
                            }
                            Collections.sort(values, new Comparator<DropDownPosition>() {
                                @SuppressWarnings("unchecked")
                                @Override
                                public int compare(final DropDownPosition _o1,
                                                   final DropDownPosition _o2)
                                {
                                    return _o1.getOrderValue().compareTo(_o2.getOrderValue());
                                }
                            });
                            ret.put(ReturnValues.SNIPLETT, getInputField(_parameter, values,
                                            Field_Base.ListType.RADIO));
                        } else {
                            ret.put(ReturnValues.SNIPLETT, ordinal > -1
                                            ? DBProperties.getProperty(enumName + "." + consts[ordinal].toString())
                                                            : "");
                        }
                    }
                } catch (final ClassNotFoundException e) {
                    throw new EFapsException(Field_Base.class, "ClassNotFoundException", e);
                }
            } else {
                throw new EFapsException(Field_Base.class, "No Enum defined");
            }
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
        Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof FieldValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((FieldValue) uiObject).getDisplay())) {
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
                final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
                final Properties confProps = config.getObjectAttributeValueAsProperties(_parameter.getInstance());
                if (Display.EDITABLE.equals(fieldvalue.getDisplay())) {
                    final StringBuilder propStr = new StringBuilder();
                    for (final Entry<Object, Object> entry : confProps.entrySet()) {
                        propStr.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                    }
                    ret.put(ReturnValues.VALUES, propStr.toString());
                } else {
                    final StringBuilder html = new StringBuilder();
                    html.append("<table>");
                    for (final Entry<Object, Object> entry : confProps.entrySet()) {
                        html.append("<tr>")
                             .append("<td>").append(entry.getKey()).append("</td>")
                             .append("<td>").append(entry.getValue()).append("</td>")
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (fieldValue != null) {
            final Object value = fieldValue.getValue();
            if (value instanceof List) {
                final List<Classification> clazzes = new ArrayList<Classification>();
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
                        final Map<Integer, String> map = new TreeMap<Integer, String>();
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
                    final Map<Integer, String> map = new TreeMap<Integer, String>();
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

    public Return getTypeDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        StringBuilder html = new StringBuilder();

        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (Display.EDITABLE.equals(fieldvalue.getDisplay())) {
            if (properties.containsKey("Types")) {
                html = tobeRemoved(_parameter);
            } else {
                final String selected = (String) properties.get("SelectedType");
                final boolean includeAbstract = "true".equalsIgnoreCase((String) properties.get("IncludeAbstract"));
                final Map<Integer, String> types = analyseProperty(_parameter, "Type");
                final Map<Integer, String> excludeTypes = analyseProperty(_parameter, "ExcludeType");

                if (!types.isEmpty()) {
                    final Set<Type> excludes = new HashSet<Type>();
                    if (!excludeTypes.isEmpty()) {
                        for (final Entry<Integer, String> entryExclude : excludeTypes.entrySet()) {
                            final Type type4Exclude = Type.get(entryExclude.getValue());
                            if (type4Exclude != null) {
                                excludes.add(type4Exclude);
                            }
                        }
                    }
                    final Type selectedType = selected != null && !selected.isEmpty() ? Type.get(selected) : null;

                    final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
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
                    html.append(getDropDownField(_parameter, positions));
                }
            }
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    @Deprecated
    private StringBuilder tobeRemoved(final Parameter _parameter)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        Field_Base.LOG.warn("Command: '{}' uses deprecated API defintion for Field.",
                        fieldValue.getField().getCollection().getName());

        final StringBuilder html = new StringBuilder();

        final Map<? , ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String typesStr = (String) props.get("Types");
        final String selected = (String) props.get("SelectedType");
        final boolean includeAbstract = "true".equalsIgnoreCase((String) props.get("IncludeAbstract"));
        final String excludeTypesStr = (String) props.get("ExcludeTypes");

        if (typesStr != null && !typesStr.isEmpty()) {
            final Set<Type>excludes = new HashSet<Type>();
            if (excludeTypesStr != null && !excludeTypesStr.isEmpty()) {
                final String[] excludesStr = excludeTypesStr.split(";");
                for (final String typeStr  : excludesStr) {
                    final Type type = Type.get(typeStr);
                    if (type != null) {
                        excludes.add(type);
                    }
                }
            }
            final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
            final String[] types = typesStr.split(";");
            final Type selType = selected != null && !selected.isEmpty() ? Type.get(selected) : null;
            for (final String typeStr  : types) {
                final Set<Type> typeList = getTypeList(_parameter, Type.get(typeStr));
                for (final Type type : typeList) {
                    if (!excludes.contains(type) && (!type.isAbstract() || includeAbstract)) {
                        final DropDownPosition pos = new DropDownPosition(type.getId(), type.getLabel(),
                                        type.getLabel());
                        positions.add(pos);
                        if (type.equals(selType)) {
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
            html.append(getDropDownField(_parameter, positions));
        }
        return html;
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
        final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (Display.EDITABLE.equals(fieldvalue.getDisplay())) {
            final Map<Integer, String> rootClasses = analyseProperty(_parameter, "Classification");
            final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
            final List<Classification> clazzList = new ArrayList<Classification>();
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
            for (final Classification clazz : clazzList) {
                Classification tmp = clazz;
                String label = tmp.getLabel();
                while (tmp.getParentClassification() != null) {
                    tmp = tmp.getParentClassification();
                    label = tmp.getLabel() + " - " + label;
                }
                final DropDownPosition pos = new DropDownPosition(clazz.getId(), label, label);
                positions.add(pos);
                if (Long.valueOf(clazz.getId()).equals(fieldvalue.getValue())) {
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
        final List<Classification> ret = new ArrayList<Classification>();
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
        Return ret;
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        if (uiObject instanceof FieldValue) {
            if (org.efaps.admin.ui.field.Field.Display.EDITABLE.equals(((FieldValue) uiObject).getDisplay())) {
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
     * Get an empty dropdown.
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return emptyDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, getDropDownField(_parameter, new ArrayList<DropDownPosition>()).toString());
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
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String typeStr = (String) props.get("Type");
        final Type type = Type.get(typeStr);
        if (type != null) {
            final boolean includeChildTypes = !"false".equalsIgnoreCase((String) props.get("ExpandChildTypes"));

            final QueryBuilder queryBldr = new QueryBuilder(type);
            final String linkfrom = (String) props.get("LinkFrom");
            if (linkfrom != null) {
                final Instance instance = _parameter.getInstance() == null
                                                ? _parameter.getCallInstance() : _parameter.getInstance();
                if (instance != null && instance.isValid()) {
                    queryBldr.addWhereAttrEqValue(linkfrom, instance.getId());
                }
            }
            final String where = (String) props.get("WhereAttrEqValue");
            if (where != null) {
                final String[] parts = where.split("\\|");
                queryBldr.addWhereAttrEqValue(parts[0], parts[1]);
            }

            if (props.containsKey("StatusGroup")) {
                final String statiStr = String.valueOf(props.get("Stati"));
                final String[] statiAr = statiStr.split(";");
                final List<Object> statusList = new ArrayList<Object>();
                for (final String stati : statiAr) {
                    final Status status = Status.find((String) props.get("StatusGroup"), stati);
                    if (status != null) {
                        statusList.add(status.getId());
                    }
                }
                queryBldr.addWhereAttrEqValue(type.getStatusAttribute().getName(), statusList.toArray());
            }

            add2QueryBuilder4List(_parameter, queryBldr);

            final InstanceQuery instQuery = queryBldr.getQuery();
            instQuery.setIncludeChildTypes(includeChildTypes);

            final MultiPrintQuery multi = new MultiPrintQuery(instQuery.execute());
            final String select = (String) props.get("Select");
            if (select != null) {
                multi.addSelect(select);
            }
            final String phrase = (String) props.get("Phrase");
            if (phrase != null) {
                multi.addPhrase("Phrase", phrase);
            }
            final String valueSel = (String) props.get("ValueSelect");
            if (valueSel != null) {
                multi.addSelect(valueSel);
            }
            final String orderSel = (String) props.get("OrderSelect");
            if (orderSel != null) {
                multi.addSelect(orderSel);
            }

            multi.execute();
            Object dbValue = null;
            final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
            if (uiObject instanceof FieldValue) {
                dbValue = ((FieldValue) uiObject).getValue();
            }

            final List<DropDownPosition> values = new ArrayList<DropDownPosition>();
            boolean selected = false;
            while (multi.next()) {
                Object value;
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
                }
                final DropDownPosition val = getDropDownPosition(_parameter, value, option);
                values.add(val);
                if (orderSel != null) {
                    val.setOrderValue((Comparable<?>) multi.getSelect(orderSel));
                }
                // evaluate for selected only until the first is found
                if (!selected) {
                    if (dbValue != null && "true".equalsIgnoreCase((String) props.get("SetSelected"))) {
                        if (dbValue.equals(val.value)) {
                            val.setSelected(true);
                            selected = true;
                        }
                    } else if (props.containsKey("Regex4DefaultValue")) {
                        if (String.valueOf(val.getOption()).matches((String) props.get("Regex4DefaultValue"))) {
                            val.setSelected(true);
                            selected = true;
                        }
                    }
                }
            }
            if (props.containsKey("emptyValue")) {
                values.add(0, new DropDownPosition("", DBProperties.getProperty((String) props.get("emptyValue"))));
            }

            if (orderSel != null) {
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        html.append("<select name=\"").append(fieldValue != null ? fieldValue.getField().getName() : "eFapsDropDown")
            .append("\" size=\"1\"").append(UIInterface.EFAPSTMPTAG).append(">");
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
        } else if (uiObject instanceof FieldValue) {
            name = ((FieldValue) uiObject).getField().getName();
        } else if (uiObject instanceof org.efaps.admin.ui.field.Field) {
            name = ((org.efaps.admin.ui.field.Field) uiObject).getName();
        } else {
            name = "eFapsCheckBoxes";
        }

        for (final DropDownPosition value : _values) {
            html.append("<input type=\"").append(_listType.equals(Field_Base.ListType.CHECKBOX) ? "checkbox" : "radio")
                .append("\" value=\"").append(value.getValue()).append("\" name=\"")
                .append(name).append("\"");
            if (value.isSelected()) {
                html.append(" checked=\"checked\"");
            }
            html.append(" />").append(value.getOption());
            if (horizontal == null && !"true".equals(horizontal)) {
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
    protected DropDownPosition getDropDownPosition(final Parameter _parameter,
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        Context.getThreadContext().setSessionAttribute(fieldValue.getField().getName(),
                        _parameter.getParameterValues("selectedRow"));
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
     *
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return getDimensionUoMFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final TargetMode mode  = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final StringBuilder html = new StringBuilder();
        final String dimStr = (String) props.get("DimensionUUIDs");
        final Long uoMID = (Long) fieldValue.getValue();
        if ((TargetMode.EDIT.equals(mode) || TargetMode.CREATE.equals(mode))
                        && fieldValue.getField().isEditableDisplay(mode)) {
            final Map<String, Dimension> group2dim = new TreeMap<String, Dimension>();
            if (dimStr != null && !dimStr.isEmpty()) {
                final String[] dims = dimStr.split(";");
                for (final String dimUUID  : dims) {
                    final Dimension dim = Dimension.get(UUID.fromString(dimUUID));
                    if (dim != null) {
                        group2dim.put(dim.getName(), dim);
                    }
                }
            } else {
                final Cache<String, Dimension> cache = InfinispanCache.get().<String,
                                Dimension>getCache("Dimension4Name");
                for (final Dimension dim : cache.values()) {
                    group2dim.put(dim.getName(), dim);
                }
            }
            html.append("<select name=\"").append(fieldValue.getField().getName()).append("\" ")
                .append(UIInterface.EFAPSTMPTAG).append(" >");
            for (final Entry<String, Dimension> entry1 : group2dim.entrySet()) {
                html.append("<optgroup label=\"").append(entry1.getKey()).append("\">");
                final Map<String, UoM> name2UoM = new TreeMap<String, UoM>();
                for (final UoM uom : entry1.getValue().getUoMs()) {
                    name2UoM.put(uom.getName(), uom);
                }
                for (final Entry<String, UoM> entry2 : name2UoM.entrySet()) {
                    html.append("<option value=\"").append(entry2.getValue().getId()).append("\"");
                    if (((Long) entry2.getValue().getId()).equals(uoMID)) {
                        html.append(" selected=\"selected\"");
                    }
                    html.append(">").append(entry2.getKey()).append("</option>");
                }
                html.append("</optgroup>");
            }
            html.append("</select>");
        } else {
            if (uoMID != null) {
                html.append(Dimension.getUoM(uoMID).getName());
            }
        }
        final Return ret = new Return();
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
        if (_parameter.get(ParameterValues.ACCESSMODE) == TargetMode.EDIT) {
            final StringBuilder html = new StringBuilder();
            final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
            final List<DropDownPosition> positions = new ArrayList<DropDownPosition>();
            if (fieldValue.getValue() != null) {
                final UoM uomValue = Dimension.getUoM((Long) fieldValue.getValue());
                if (uomValue != null) {
                    final Dimension dim = uomValue.getDimension();
                    for (final UoM uom : dim.getUoMs()) {
                        final DropDownPosition position = getDropDownPosition(_parameter, uom.getId(), uom.getName());
                        positions.add(position);
                        position.setSelected(uomValue.equals(uom));
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
            html.append(getDropDownField(_parameter, positions));
            ret.put(ReturnValues.SNIPLETT, html.toString());
        }
        return ret;
    }

    /**
     *
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return getField4Mode(final Parameter _parameter)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final String reuqestKey = fieldValue.getField().getName() + ".getField4Mode";
        final Return ret = new Return();
        if (Context.getThreadContext().containsRequestAttribute(reuqestKey)) {
            ret.put(ReturnValues.VALUES, fieldValue.getValue());
        } else {
            Context.getThreadContext().setRequestAttribute(reuqestKey, true);

            final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final String target = (String) props.get("TargetMode");
            final String displayStr = (String) props.get("Display");

            final TargetMode mode = TargetMode.valueOf(target);
            final org.efaps.admin.ui.field.Field.Display display = org.efaps.admin.ui.field.Field.Display.valueOf(
                            displayStr);

            final String html;
            switch (display) {
                case EDITABLE:
                    html = fieldValue.getEditHtml(mode);
                    break;
                case READONLY:
                    html = fieldValue.getReadOnlyHtml(mode);
                    break;
                case HIDDEN:
                    html = fieldValue.getHiddenHtml(mode);
                    break;
                default:
                    html = "";
                    break;
            }
            ret.put(ReturnValues.SNIPLETT, html);
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Snipplet
     * @throws EFapsException on error
     */
    public Return getHiddenInputField(final Parameter _parameter)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final Object value = fieldValue.getValue() != null ? fieldValue.getValue() : "";

        html.append("<span ").append(UIInterface.EFAPSTMPTAG).append(" name=\"")
            .append(fieldValue.getField().getName()).append("\">").append(value).append("</span>");
        if (fieldValue.getTargetMode().equals(TargetMode.EDIT)) {
            html.append("<input type=\"hidden\" name=\"").append(fieldValue.getField().getName()).append("\"")
                .append(" value=\"").append(value).append("\"/>");
        }

        if (!fieldValue.getDisplay().equals(Display.NONE)) {
            ret.put(ReturnValues.SNIPLETT, html.toString());
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
     * A position in a dropdown.
     */
    public static class DropDownPosition
    {
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
            this.value = _value;
            this.option = _option;
            this.orderValue = _orderValue;
        }

        /**
         * Is this position selected.
         * @return false
         */
        public boolean isSelected()
        {
            return this.selected;
        }

        /**
         * Setter method for instance variable {@link #selected}.
         *
         * @param _selected value for instance variable {@link #selected}
         */
        public void setSelected(final boolean _selected)
        {
            this.selected = _selected;
        }

        /**
         * Setter method for instance variable {@link #option}.
         *
         * @param _option value for instance variable {@link #option}
         */
        public void setOption(final Object _option)
        {
            this.option = _option;
        }

        /**
         * Getter method for the instance variable {@link #option}.
         *
         * @return value of instance variable {@link #option}
         */
        public Object getOption()
        {
            return this.option;
        }

        /**
         * Setter method for instance variable {@link #value}.
         *
         * @param _value value for instance variable {@link #value}
         */

        public void setValue(final Object _value)
        {
            this.value = _value;
        }

        /**
         * Getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public Object getValue()
        {
            return this.value;
        }

        /**
         * Setter method for instance variable {@link #orderValue}.
         *
         * @param _orderValue value for instance variable {@link #orderValue}
         */

        public void setOrderValue(final Comparable<?> _orderValue)
        {
            this.orderValue = _orderValue;
        }

        /**
         * Getter method for the instance variable {@link #orderValue}.
         *
         * @return value of instance variable {@link #orderValue}
         */
        @SuppressWarnings("rawtypes")
        public Comparable getOrderValue()
        {
            return this.orderValue == null ? this.option.toString() : this.orderValue;
        }
    }
}
