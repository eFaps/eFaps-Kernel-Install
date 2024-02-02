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

import java.util.List;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * Converter that turns a a List of Objects into a String.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("93fceec4-07bd-4227-b187-ccc334abc903")
@EFapsApplication("eFaps-Kernel")
public abstract class List2StringConvert_Base
    implements IConverter
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getConvertedValue(final Object _value)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        if (_value instanceof List) {
            for (final Object obj : (List<?>) _value) {
                ret.append(obj).append("\n");
            }
        } else {
            if (_value != null) {
                ret.append(_value);
            }
        }
        return ret.toString();
    }
}
