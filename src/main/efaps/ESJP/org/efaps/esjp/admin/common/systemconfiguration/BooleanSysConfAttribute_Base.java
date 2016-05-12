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

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Class BooleanSysConfAttribute_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("1ca2e416-d612-4b73-bc1b-94439d073600")
@EFapsApplication("eFaps-Kernel")
public abstract class BooleanSysConfAttribute_Base
    extends AbstractSysConfAttribute<BooleanSysConfAttribute, Boolean>
{

    /**
     * Instantiates a new boolean sys conf attribute_ base.
     */
    public BooleanSysConfAttribute_Base()
    {
        defaultValue(false);
    }

    @Override
    protected BooleanSysConfAttribute getThis()
    {
        return (BooleanSysConfAttribute) this;
    }

    @Override
    public Boolean get()
        throws EFapsException
    {
        final boolean ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            ret = SystemConfiguration.get(getSysConfUUID()).getAttributeValueAsBoolean(getKey());
        } else {
            ret = getDefaultValue();
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                  final Object _value)
    {
        boolean val = getDefaultValue();
        if (_value != null) {
            val = Boolean.parseBoolean((String) _value);
        }
        final StringBuilder ret = new StringBuilder()
                    .append("<label tag=\"rem\"><input type=\"radio\" name=\"value\" value=\"true\"")
                    .append(val ? " checked=\"checked\" "  : "")
                    .append(" >true</label>")
                    .append("<br tag=\"rem\"><label tag=\"rem\"><input type=\"radio\" name=\"value\" ")
                    .append(!val ? " checked=\"checked\" "  : "")
                    .append("value=\"false\">false</label>");
        return ret;
    }
}
