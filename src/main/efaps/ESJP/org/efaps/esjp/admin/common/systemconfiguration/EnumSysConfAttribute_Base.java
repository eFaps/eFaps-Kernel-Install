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

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Class EnumSysConfAttribute_Base.
 *
 * @author The eFaps Team
 * @param <E> the element type
 */
@EFapsUUID("12fbca5f-94c4-4fa7-92dc-f82a5a8c330a")
@EFapsApplication("eFaps-Kernel")
public abstract class EnumSysConfAttribute_Base<E extends Enum<E>>
    extends AbstractSysConfAttribute<EnumSysConfAttribute<E>, E>
{

    /** The enum clazz. */
    private Class<E> clazz;

    /**
     * Set the enum Clazz.
     *
     * @param _clazz the _clazz
     * @return this EnumSysConfAttribute
     */
    public EnumSysConfAttribute<E> clazz(final Class<E> _clazz)
    {
        this.clazz = _clazz;
        return getThis();
    }

    @Override
    protected EnumSysConfAttribute<E> getThis()
    {
        return (EnumSysConfAttribute<E>) this;
    }

    @Override
    public E get()
        throws EFapsException
    {
        E ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            final String val = SystemConfiguration.get(getSysConfUUID()).getAttributeValue(getKey());
            ret = EnumUtils.getEnum(this.clazz, val);
        } else {
            ret = getDefaultValue();
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value)
    {
        E val = null;
        if (_value != null) {
            val = EnumUtils.getEnum(this.clazz, (String) _value);
        }
        final StringBuilder ret = new StringBuilder();
        for (final E e :  EnumUtils.getEnumList(this.clazz)) {
            ret.append("<label tag=\"rem\"><input type=\"radio\" name=\"value\" value=\"")
                .append(e).append("\"");
            if (e.equals(val)) {
                ret.append(" checked=\"checked\" ");
            }
            ret.append(" >").append(e).append("</label><br/>");
        }
        return ret;
    }
}
