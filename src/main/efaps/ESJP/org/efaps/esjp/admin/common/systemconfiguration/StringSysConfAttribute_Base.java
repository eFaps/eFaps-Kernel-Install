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
package org.efaps.esjp.admin.common.systemconfiguration;

import org.apache.commons.text.StringEscapeUtils;
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
@EFapsUUID("325e841e-21ee-41cc-9deb-f8018545e68e")
@EFapsApplication("eFaps-Kernel")
public abstract class StringSysConfAttribute_Base
    extends AbstractSysConfAttribute<StringSysConfAttribute, String>
{

    @Override
    protected StringSysConfAttribute getThis()
    {
        return (StringSysConfAttribute) this;
    }

    @Override
    public String get()
        throws EFapsException
    {
        final String ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            ret = SystemConfiguration.get(getSysConfUUID()).getAttributeValue(getKey());
        } else {
            ret = getDefaultValue();
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value,
                                final String _fieldName)
    {
        final StringBuilder ret = new StringBuilder()
                        .append("<input type=\"text\" name=\"").append(_fieldName).append("\" size=\"70\"");
        if (_value != null || getDefaultValue() != null) {
            ret.append(" value=\"").append(
                            StringEscapeUtils.escapeHtml4(_value == null
                                ? getDefaultValue()
                                : (String) _value)).append("\"");
        }
        ret.append(">");
        return ret;
    }
}
