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

package org.efaps.esjp.common.uiform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Context;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

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
{
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
        html.append("/>");
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
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
        final StringBuilder html = new StringBuilder();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (props.containsKey("SystemConfigurationUUID")) {
            final UUID uuid = UUID.fromString((String) props.get("SystemConfigurationUUID"));
            final SystemConfiguration config = SystemConfiguration.get(uuid);
            if (config != null) {
                final Properties confProps = config.getObjectAttributeValueAsProperties(_parameter.getInstance());
                html.append("<table>");
                for (final Entry<Object, Object>entry : confProps.entrySet()) {
                    html.append("<tr>")
                        .append("<td>").append(entry.getKey()).append("</td>")
                        .append("<td>").append(entry.getValue()).append("</td>")
                        .append("</tr>");
                }
                html.append("</table>");
            }
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     * Formats the values for a class select field used normally in tables.
     * Properties:
     * <table>
     *  <tr><th>Property</th><th>Value</th><th>Obligatory</th></tr>
     *  <tr><td>Seperator</td><td>Seperator used to seprerate the values, if mor than one</td><td>false</td></tr>
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
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (fieldValue != null) {
            final Object value = fieldValue.getValue();
            if (value instanceof List) {
                final List<String> labels = new ArrayList<String>();
                for (final Object val : (List<?>) value) {
                    labels.add(((Classification) val).getLabel());
                }
                Collections.sort(labels);
                boolean first = true;
                for (final String label : labels) {
                    if (first) {
                        first = false;
                    } else {
                        html.append(seperator);
                    }
                    html.append(label);
                }
            } else if (value instanceof Classification) {
                html.append(((Classification) value).getLabel());
            }
        }

        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, html.toString());
        return ret;
    }


    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing Html Snipplet
     * @throws EFapsException on error
     */
    public Return dropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String typeStr = (String) props.get("Type");
        final Type type = Type.get(typeStr);
        if (type != null) {
            final QueryBuilder queryBldr = new QueryBuilder(type);
            final String where = (String) props.get("WhereAttrEqValue");
            if (where != null) {
                final String[] parts = where.split("\\|");
                queryBldr.addWhereAttrEqValue(parts[0], parts[1]);
            }
            final MultiPrintQuery multi = queryBldr.getPrint();
            final String select = (String) props.get("Select");
            if (select != null) {
                multi.addSelect(select);
            }
            final String phrase = (String) props.get("Phrase");
            if (phrase != null) {
                multi.addPhrase("Phrase", phrase);
            }
            multi.execute();

            final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
            html.append("<select name=\"").append(fieldValue.getField().getName()).append("\" size=\"1\">");
            while (multi.next()) {
                html.append("<option value=\"").append(multi.getCurrentInstance().getId()).append("\"/>");
                if (select != null) {
                    html.append(multi.getSelect(select));
                }
                if (phrase != null) {
                    html.append(multi.getPhrase("Phrase"));
                }
                html.append("</option>");
            }
            html.append("</select>");
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
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
                for (final Dimension dim : Dimension.getTypeCache().getCache4Id().values()) {
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
                    if (entry2.getValue().getId() == uoMID) {
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
}
