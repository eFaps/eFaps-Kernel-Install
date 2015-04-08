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
 * Revision:        $Rev: 7794 $
 * Last Changed:    $Date: 2012-07-17 17:35:24 -0500 (mar, 17 jul 2012) $
 * Last Changed By: $Author: luis.estrada@efaps.org $
 */

package org.efaps.esjp.common.uiform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 *
 * Class contains method for generate javascript to storage and retrieve Fields
 * from forms.
 *
 * @author The eFaps Team
 * @version $Id: LocalStorage_Base.java 7794 2012-08-10 22:35:24Z
 *          luis.estrada@efaps.org $
 *
 */
@EFapsUUID("3fbb0b9c-b671-48c5-a399-424e665ed5c8")
@EFapsApplication("eFaps-Kernel")
public abstract class LocalStorage_Base
{

    /**
     *
     * @param _parameter from EFaps API.
     * @return Return code paint buttons to storage and retrieve.
     * @throws EFapsException on error.
     */
    public Return localStorageFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        html.append("<script type=\"text/javascript\" ><!--/*--><![CDATA[/*><!--*/\n")
                        .append(store(_parameter)).append("\n")
                        .append(retrieve(_parameter)).append("\n")
                        .append("/*-->]]>*/</script>\n")
                        .append("<input type=\"button\" name=\"").append(fieldValue.getField().getName() + 1)
                        .append("\" ").append(UIInterface.EFAPSTMPTAG)
                        .append(" onclick= \"storage()\"")
                        .append(" value=\"").append(DBProperties
                                        .getProperty(fieldValue.getField().getLabel()
                                                        + ".storage", "es")).append("\" ").append("/>")
                        .append("<input type=\"button\" name=\"").append(fieldValue.getField().getName() + 2)
                        .append("\" ").append(UIInterface.EFAPSTMPTAG)
                        .append(" onclick= \"retrieve()\" ")
                        .append(" value=\"").append(DBProperties
                                        .getProperty(fieldValue.getField().getLabel()
                                                        + ".retrieve", "es")).append("\" ").append("/>");

        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     *
     * @param _parameter from EFaps API
     * @return javascript to storage fields
     * @throws CacheReloadException on error
     */
    protected String store(final Parameter _parameter)
        throws CacheReloadException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final List<String[]> storageFields = new ArrayList<String[]>();

        final FieldValue fieldLocalStorage = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final String formUUID = fieldLocalStorage.getField().getCollection().getUUID().toString();

        final StringBuilder js = new StringBuilder();

        // retrieve properties PositionsField or FormField
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get("PositionField" + i) != null) {
                final String strPositionFields = (String) properties.get("PositionField" + i);
                storageFields.add(strPositionFields.split(";"));
            }

            if (properties.get("FormField" + i) != null) {
                final String strFormFields = (String) properties.get("FormField" + i);
                storageFields.add(strFormFields.split(";"));
            }
        }

        js.append("function storage(){\n")
                        .append("//attributes  to storage")
                        .append("\n");
        // create variable var for every field
        for (final String[] storageField : storageFields) {
            for (int j = 0; j < storageField.length; j++) {
                js.append("var ").append(storageField[j]).append(" = document.getElementsByName('")
                                .append(storageField[j]).append("');\n");
            }
        }

        for (final String[] storageField : storageFields) {
            for (int i = 0; i < storageField.length; i++) {
                js.append("//object json to attribute")
                                .append("\nvar json").append(storageField[i]).append(" = [];\n")
                                .append("for(var i = 0; i < ").append(storageField[i]).append(".length ; i++) {\n")
                                .append("if(").append(storageField[i]).append("[i].type == 'select-one'){\n")
                                .append("var jsonOptions = [];\n")
                                .append("for(var j = 0; j < ").append(storageField[i]).append("[i].length; j++){\n")
                                .append("jsonOptions.push({index: ")
                                .append(storageField[i]).append("[i].options[j].value, ")
                                .append("valueText: ").append(storageField[i])
                                .append("[i].options[j].text").append("});\n")
                                .append("}\n")
                                .append("json").append(storageField[i]).append(".push({pos: i, selected: ")
                                .append(storageField[i])
                                .append("[i].options[").append(storageField[i])
                                .append("[i].selectedIndex].value, options: jsonOptions});\n")
                                .append("}else{")
                                .append("\njson").append(storageField[i]).append(".push({ pos: i, value:")
                                .append(storageField[i])
                                .append("[i].value });\n")
                                .append("}\n")
                                .append("}\n");
            }
        }

        int count = 0;
        js.append("var form ").append(" = { ");

        for (final String[] storageField : storageFields) {
            if (count != 0) {
                // when storageFields has more than one element
                js.append(", ").append(storageField[0]).append(" : ").append("json").append(storageField[0]);
            } else {
                js.append(storageField[0]).append(" : ").append("json").append(storageField[0]);
            }

            for (int i = 1; i < storageField.length; i++) {
                js.append(", ").append(storageField[i]).append(" : ").append("json").append(storageField[i]);
            }
            count++;
        }
        js.append("};\n")
                        .append("sessionStorage.setItem('").append(formUUID)
                        .append("', JSON.stringify(form));\n")
                        .append("alert(\"").append(DBProperties
                                        .getProperty(fieldLocalStorage.getField().getLabel() + ".storage.alert", "es"))
                        .append("\" );\n")
                        .append("}\n");

        return js.toString();
    }

    /**
     *
     * @param _parameter from EFaps API
     * @return String of javascript to retrieve fields.
     * @throws CacheReloadException on erro
     */
    protected String retrieve(final Parameter _parameter)
        throws CacheReloadException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final List<String[]> positionFields = new ArrayList<String[]>();
        final List<String[]> formFields = new ArrayList<String[]>();

        final FieldValue fieldLocalStorage = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final String formUUID = fieldLocalStorage.getField().getCollection().getUUID().toString();

        final StringBuilder js = new StringBuilder();

        // retrieve properties PositionsField or FormField
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get("PositionField" + i) != null) {
                final String strPositionFields = (String) properties.get("PositionField" + i);
                positionFields.add(strPositionFields.split(";"));
            }

            if (properties.get("FormField" + i) != null) {
                final String strFormFields = (String) properties.get("FormField" + i);
                formFields.add(strFormFields.split(";"));
            }
        }

        js.append("function retrieveRows(){\n")
                        .append("var retrievedObject = sessionStorage.getItem('").append(formUUID).append("');\n")
                        .append("var data = JSON.parse(retrievedObject);\n");

        for (final String[] positionField : positionFields) {
            for (int i = 0; i < positionField.length; i++) {
                js.append("//verified if type of field is text or select")
                    .append("\nfor(var i = 0; i < data.").append(positionField[i])
                    .append(".length ; i++){\n")
                    .append("if(typeof data.").append(positionField[i])
                    .append("[i].selected == 'undefined'){\n")
                    .append("eFapsSetFieldValue(i, '").append(positionField[i])
                    .append("', data.").append(positionField[i]).append("[i].value);\n")
                    .append("}else{\n")
                    .append("var options = [];\n")
                    .append("options.push('' + data.").append(positionField[i])
                    .append("[i].selected").append(");\n")
                    .append("for(var j = 0; j < data.").append(positionField[i])
                    .append("[i].options.length; j++){\n")
                    .append("options.push('' + data.").append(positionField[i])
                    .append("[i].options[j].index);\n")
                    .append("options.push('' + data.").append(positionField[i])
                    .append("[i].options[j].valueText);\n")
                    .append("}\n")
                    .append("eFapsSetFieldValue(i, '").append(positionField[i]).append("', options);\n")
                    .append("}\n")
                    .append("}\n");
                js.append(add2Script(_parameter, positionField[i]));
            }
        }

        js.append("}\n")
                .append("function retrieve(){\n")
                .append("var retrievedObject = sessionStorage.getItem('").append(formUUID).append("');\n")
                .append("var data = JSON.parse(retrievedObject);\n");
        for (final String[] positionField : positionFields) {
            js.append("var numberPositions = data.").append(positionField[0]).append(".length;\n");
            break;
        }
        js.append("Wicket.Event.add(window, \"domready\", function(event) {\n")
                        .append("addNewRows_positionTable(numberPositions, retrieveRows, null);\n");
        for (final String[] formField : formFields) {
            for (int i = 0; i < formField.length; i++) {
                js.append("eFapsSetFieldValue(0, '").append(formField[i])
                                .append("', data.").append(formField[i]).append("[0].value);\n");
            }
        }
        js.append("})\n")
                        .append("}\n");
        return js.toString();
    }

    /**
     * @param _parameter        Parameter as passed by the eFaps API
     * @param _positionField    position
     * @return additional script
     */
    protected String add2Script(final Parameter _parameter,
                                final String _positionField)
    {
        return "";
    }
}
