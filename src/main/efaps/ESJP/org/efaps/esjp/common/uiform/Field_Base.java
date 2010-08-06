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

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
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
     * Renders a field taht contains the values from a
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
}
