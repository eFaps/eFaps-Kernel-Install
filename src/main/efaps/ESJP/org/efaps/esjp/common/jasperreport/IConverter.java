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
import org.efaps.util.EFapsException;


/**
 * Interface for Converters use to convert  the values for Fields to be
 * conform with jasper.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("28b8c0d5-0cb5-40bd-8cc9-d795d6e6b7d0")
@EFapsApplication("eFaps-Kernel")
public interface IConverter
{
    /**
     * @param _value the value to be converted
     * @return the converted value
     * @throws EFapsException on error
     */
    Object getConvertedValue(Object _value)
        throws EFapsException;
}
