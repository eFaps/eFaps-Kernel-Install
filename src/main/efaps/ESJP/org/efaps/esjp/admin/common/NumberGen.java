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

package org.efaps.esjp.admin.common;

import org.efaps.admin.common.NumberGenerator;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d8c2a262-f697-43b8-a7c7-ab4a08f1f996")
@EFapsApplication("eFaps-Kernel")
public class NumberGen
{
    /**
     * Method used to set the value for the NumberGenerator.
     * @param _parameter Parameter as passed from the eFaps API.
     * @return new empty Return
     * @throws EFapsException
     */
    public final Return setValue(final Parameter _parameter)
        throws EFapsException
    {
        final String value = _parameter.getParameterValue("value");
        if (value != null && value.length() > 0) {
            final NumberGenerator gen = NumberGenerator.get(_parameter.getInstance().getId());
            gen.setVal(value);
        }
        return new Return();
    }
}
