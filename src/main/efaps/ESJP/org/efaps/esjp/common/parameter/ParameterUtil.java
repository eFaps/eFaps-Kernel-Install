/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.esjp.common.parameter;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("395d1934-0fed-480b-ae66-8367ac72348a")
@EFapsRevision("$Rev$")
public class ParameterUtil
    extends ParameterUtil_Base
{
    public static Parameter clone(final Parameter _parameter,
                                  final Object... _tuplets)
    {
        return ParameterUtil_Base.clone(_parameter, _tuplets);
    }

    /**
     * @param _string
     * @param _string2
     */
    public static void setProperty(final Parameter _parameter,
                                   final String _key,
                                   final String _value)
    {
        ParameterUtil_Base.setProperty(_parameter, _key, _value);
    }
}
