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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.file;

import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The ESJP could be used to show an image in a field.
 * <br>
 * <br>
 * <b>Properties:</b><br>
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value</th>
 * <th>Default</th>
 * <th>mandatory</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>min-height</td>
 * <td>String. e.g. 200px, 20%</td>
 * <td>-</td>
 * <td>no</td>
 * <td>Value for the stylesheet parameter for minimum height. </td>
 * </tr>
 * <tr>
 * <td>max-height</td>
 * <td>String. e.g. 200px, 20%</td>
 * <td>-</td>
 * <td>no</td>
 * <td>Value for the stylesheet parameter for maximum height. </td>
 * </tr>
 * <tr>
 * <td>min-width</td>
 * <td>String. e.g. 200px, 20%</td>
 * <td>-</td>
 * <td>no</td>
 * <td>Value for the stylesheet parameter for minimum width. </td>
 * </tr>
 * <tr>
 * <td>max-width</td>
 * <td>String. e.g. 200px, 20%</td>
 * <td>-</td>
 * <td>no</td>
 * <td>Value for the stylesheet parameter  for maximum width. </td>
 * </tr>
 * </table><br>
 *
 * To use this ESJP configure a field in a form like this following example:
 * <pre>
 * <code>
 * &lt;field name="image"&gt;
 *     &lt;property name="Label"&gt;Admin_UI_File/File.Label&lt;/property&gt;
 *     &lt;trigger name="Admin_UI_File/file"
 *              event="UI_FIELD_VALUE"
 *              program="org.efaps.esjp.common.file.ImageField"
 *              method="getViewFieldValueUI"/&gt;
 * &lt;/field&gt;
 * </code>
 * </pre>
 * Exmaple setting the min-height.
 *
 * <pre>
 * <code>
 * &lt;field name="image"&gt;
 *     &lt;property name="Label"&gt;Admin_UI_File/File.Label&lt;/property&gt;
 *     &lt;trigger name="Admin_UI_File/file"
 *              event="UI_FIELD_VALUE"
 *              program="org.efaps.esjp.common.file.ImageField"
 *              method="getViewFieldValueUI"&gt;
 *       &lt;property name="min-height"&gt;200px&lt;/property&gt;
 *     &lt;/trigger&gt;
 * &lt;/field&gt;
 * </code>
 * </pre>
 *
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("62d23b78-76ec-4caf-9484-1e5aa560540e")
@EFapsApplication("eFaps-Kernel")
public abstract class ImageField_Base
{
    /**
     * Render a field with an image.
     * @param _parameter parameter as defined by the eFaps esjp API
     * @return Return containing a snipplet
     * @throws EFapsException on error
     */
    public Return getViewFieldValueUI(final Parameter _parameter)
        throws EFapsException
    {
        _parameter.getCallInstance();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final StringBuilder ret = new StringBuilder();
        ret.append("<img style=\"height:auto; width:auto; cursor:pointer;");

        if (properties.containsKey("min-height")) {
            ret.append("min-height:").append((String) properties.get("min-height")).append(";");
        }
        if (properties.containsKey("max-height")) {
            ret.append("max-height:").append((String) properties.get("max-height")).append(";");
        }
        if (properties.containsKey("min-width")) {
            ret.append("min-width:").append((String) properties.get("min-width")).append(";");
        }
        if (properties.containsKey("max-width")) {
            ret.append("max-width:").append((String) properties.get("max-width")).append(";");
        }

        ret.append("\" ")
            .append("id=\"").append(fieldValue.getField().getName()).append(fieldValue.getField().getId()).append("\" ")
            .append("onClick=\"window.open('").append(Context.getThreadContext().getPath())
            .append("/servlet/checkout?oid=").append(getTargetOid(_parameter)).append("','_blank');\" ")
            .append("src=\"").append(Context.getThreadContext().getPath()).append("/servlet/checkout?oid=")
            .append(getImageOid(_parameter)).append("\" ").append("/>");

        final Return retVal = new Return();
        retVal.put(ReturnValues.SNIPLETT, ret);
        return retVal;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @return  instance used as targetlink
     * @throws EFapsException on error
     */
    protected String getTargetOid(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getCallInstance();
        return instance.getOid();
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @return instance used for the image actually shown in the UI
     * @throws EFapsException on error
     */
    protected String getImageOid(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getCallInstance();
        return instance.getOid();
    }
}
