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
 */
package org.efaps.esjp.admin.common.systemconfiguration;

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Class StringSysConfAttribute_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("e0f6ee47-2a52-4ef8-88a3-955d9f31d5b5")
@EFapsApplication("eFaps-Kernel")
public abstract class IntegerSysConfAttribute_Base
    extends AbstractSysConfAttribute<IntegerSysConfAttribute, Integer>
{

    /**
     * Instantiates a new integer sys conf attribute_ base.
     */
    public IntegerSysConfAttribute_Base()
    {
        defaultValue(0);
    }

    @Override
    protected IntegerSysConfAttribute getThis()
    {
        return (IntegerSysConfAttribute) this;
    }

    @Override
    public Integer get()
        throws EFapsException
    {
        final int ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            ret = SystemConfiguration.get(getSysConfUUID()).getAttributeValueAsInteger(getKey());
        } else {
            ret = getDefaultValue();
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value)
    {
        final StringBuilder ret = new StringBuilder()
                        .append("<input type=\"number\" name=\"value\" size=\"5\"");
        if (_value != null || getDefaultValue() != null) {
            ret.append(" value=\"").append(StringEscapeUtils.escapeHtml4(
                            _value == null
                            ? String.valueOf(getDefaultValue())
                            : String.valueOf(_value))).append("\"");
        }
        ret.append(">");
        return ret;
    }
}
