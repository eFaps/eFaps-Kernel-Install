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
package org.efaps.esjp.common.jasperreport;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * Class is used to change the xhtml stored in eFaps for a FormatedStringType
 * Attribute into html that jasper can read correctly.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1397b978-52f5-4753-b366-132a2b41f339")
@EFapsApplication("eFaps-Kernel")
public abstract class HtmlMarkupConverter_Base
{
    /**
     * @param _value value to be converted
     * @return String
     */
    public String getConvertedString(final String _value)
    {
        return _value.replaceAll("<br />", "<br>").replaceAll("<br/>", "<br>");
    }
}
