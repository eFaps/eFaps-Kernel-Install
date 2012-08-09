/*
 * Copyright 2003 - 2012 The eFaps Team
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
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

@EFapsRevision("$Rev: 1$")
@EFapsUUID("3fbb0b9c-b671-48c5-a399-424e665ed5c8")
public abstract class LocalStorage_Base
{

    public Return localStorageFieldValue(Parameter _parameter)
        throws EFapsException
    {
        Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        html.append("<script type=\"text/javascript\" ><!--/*--><![CDATA[/*><!--*/\n")
        .append(store(_parameter)).append("\n")
        .append(retrieve(_parameter)).append("\n")
        .append("/*-->]]>*/</script>\n")
        .append("<input type=\"button\" name=\"").append(fieldValue.getField().getName() + 1)
        .append("\" ").append(UIInterface.EFAPSTMPTAG)
        .append(" onclick= \"storage()\"")
        .append(" value=\"").append(DBProperties
                        .getProperty(fieldValue.getField().getLabel() + ".storage", "es")).append("\" ").append("/>")
        .append("<input type=\"button\" name=\"").append(fieldValue.getField().getName() + 2)
        .append("\" ").append(UIInterface.EFAPSTMPTAG)
        .append(" onclick= \"retrieve()\" ")
        .append(" value=\"").append(DBProperties
                        .getProperty(fieldValue.getField().getLabel() + ".retrieve", "es")).append("\" ").append("/>");

        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    protected String store(Parameter _parameter)
    {
        Map<?, ?> properties = (Map<?, ?>)_parameter.get(ParameterValues.PROPERTIES);
        List<String[]> storageFields = new ArrayList<String[]>();

        FieldValue fieldLocalStorage = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        String formUUID = fieldLocalStorage.getField().getCollection().getUUID().toString();

        StringBuilder js = new StringBuilder();

        for (int i = 0; i < properties.size(); i++) {
            if (properties.get("PositionField" + i) != null){
                String strPositionFields = (String) properties.get("PositionField" + i);
                storageFields.add(strPositionFields.split(";"));
            }

            if(properties.get("FormField" + i) != null){
                String strFormFields = (String) properties.get("FormField" + i);
                storageFields.add(strFormFields.split(";"));
            }
        }

        js.append("function storage(){\n");
        for (String[] positionField : storageFields) {
            for (int j = 0; j < positionField.length; j++) {
                js.append("var ").append(positionField[j]).append(" = document.getElementsByName('")
                .append(positionField[j]).append("');\n");
            }
        }
        for (String[] positionField : storageFields) {
            for (int i = 0; i < positionField.length; i++) {
                js.append("\nvar json").append(positionField[i]).append(" = [];\n");
                js.append("for(var i = 0; i < ").append(positionField[i]).append(".length ; i++) {\n")
                .append("if(").append(positionField[i]).append("[i].type == 'select-one'){\n")
                .append("var jsonOptions = [];\n")
                .append("for(var j = 0; j < ").append(positionField[i]).append("[i].length; j++){\n")
                .append("jsonOptions.push({index: ").append(positionField[i]).append("[i].options[j].value, ")
                .append("valueText: ").append(positionField[i]).append("[i].options[j].text").append("});\n")
                .append("}\n")
                .append("json").append(positionField[i]).append(".push({pos: i, selected: ").append(positionField[i])
                .append("[i].options[").append(positionField[i]).append("[i].selectedIndex].value, options: jsonOptions});\n")
                .append("}else{")
                .append("\njson").append(positionField[i]).append(".push({ pos: i, value:").append(positionField[i])
                .append("[i].value });\n")
                .append("}\n")
                .append("}\n");
            }
        }

        int count = 0;
        js.append("var form ").append(" = { ");
        for(String[] storageField : storageFields){
            if (count != 0) {
                js.append(", ").append(storageField[0]).append(" : ").append("json").append(storageField[0]);
            }else {
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

    protected String retrieve(Parameter _parameter)
    {
        Map<?, ?> properties = (Map<?, ?>)_parameter.get(ParameterValues.PROPERTIES);
        List<String[]> positionFields = new ArrayList<String[]>();
        List<String[]> formFields = new ArrayList<String[]>();

        FieldValue fieldLocalStorage = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        String formUUID = fieldLocalStorage.getField().getCollection().getUUID().toString();

        StringBuilder js = new StringBuilder();

        for (int i = 0; i < properties.size(); i++) {
            if (properties.get("PositionField" + i) != null) {
                String strPositionFields = (String) properties.get("PositionField" + i);
                positionFields.add(strPositionFields.split(";"));
            }

            if (properties.get("FormField" + i) != null) {
                String strFormFields = (String) properties.get("FormField" + i);
                formFields.add(strFormFields.split(";"));
            }
        }

        js.append("function retrieveRows(){\n")
        .append("var retrievedObject = sessionStorage.getItem('").append(formUUID).append("');\n")
        .append("var data = JSON.parse(retrievedObject);\n");

        for (String[] positionField : positionFields) {
            for (int i = 0; i < positionField.length; i++) {
                js.append("for(var i = 0; i < data.").append(positionField[i]).append(".length ; i++){\n")
                .append("if(typeof data.").append(positionField[i]).append("[i].selected == 'undefined'){\n")
                .append("eFapsSetFieldValue(i, '").append(positionField[i]).append("', data.").append(positionField[i]).append("[i].value);\n")
                .append("}else{\n")
                .append("var options = [];\n")
                .append("options.push('' + data.").append(positionField[i]).append("[i].selected").append(");\n")
                .append("for(var j = 0; j < data.").append(positionField[i]).append("[i].options.length; j++){\n")
                .append("options.push('' + data.").append(positionField[i]).append("[i].options[j].index);\n")
                .append("options.push('' + data.").append(positionField[i]).append("[i].options[j].valueText);\n")
                .append("}\n")
                .append("eFapsSetFieldValue(i, '").append(positionField[i]).append("', options);\n")
                .append("}\n")
                .append("}\n");
            }
        }

        js.append("}\n")

        .append("function retrieve(){\n")
        .append("var retrievedObject = sessionStorage.getItem('").append(formUUID).append("');\n")
        .append("var data = JSON.parse(retrievedObject);\n");
        for (String[] positionField : positionFields) {
            js.append("var numberPositions = data.").append(positionField[0]).append(".length;\n");
            break;
        }
        js.append("Wicket.Event.add(window, \"domready\", function(event) {\n")
        .append("addNewRows_positionTable(numberPositions, retrieveRows, null);\n");
        for (String[] formField : formFields) {
            for (int i = 0; i < formField.length; i++) {
                js.append("eFapsSetFieldValue(0, '").append(formField[i]).append("', data.").append(formField[i]).append("[0].value);\n");
            }
        }
        js.append("})\n")
        .append("}\n");
        return js.toString();
    }
}
