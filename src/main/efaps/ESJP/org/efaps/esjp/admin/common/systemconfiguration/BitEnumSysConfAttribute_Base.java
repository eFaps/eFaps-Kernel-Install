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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
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
@EFapsUUID("25370b32-1673-4cca-8ec1-14880e0e0749")
@EFapsApplication("eFaps-Kernel")
public abstract class BitEnumSysConfAttribute_Base<E extends Enum<E>>
    extends AbstractSysConfAttribute<BitEnumSysConfAttribute<E>, Set<E>>
{

    /** The enum clazz. */
    private Class<E> clazz;

    /**
     * Set the enum Clazz.
     *
     * @param _clazz the _clazz
     * @return this EnumSysConfAttribute
     */
    public BitEnumSysConfAttribute<E> clazz(final Class<E> _clazz)
    {
        this.clazz = _clazz;
        return getThis();
    }

    @Override
    protected BitEnumSysConfAttribute<E> getThis()
    {
        return (BitEnumSysConfAttribute<E>) this;
    }

    @Override
    public Set<E> get()
        throws EFapsException
    {
        final Set<E> ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            ret = new HashSet<>();
            final String val = SystemConfiguration.get(getSysConfUUID()).getAttributeValue(getKey());
            if (StringUtils.isNotEmpty(val)) {
                for (final String cv : val.split("\\r?\\n")) {
                    ret.add(EnumUtils.getEnum(this.clazz, cv));
                }
            }
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
        final Set<E> val = new HashSet<>();
        if (_value != null) {
            for (final String cv : ((String) _value).split("\\r?\\n")) {
                val.add(EnumUtils.getEnum(this.clazz, cv));
            }
        }
        final StringBuilder ret = new StringBuilder();
        for (final E e :  EnumUtils.getEnumList(this.clazz)) {
            ret.append("<label tag=\"rem\"><input type=\"checkbox\" name=\"").append(_fieldName).append("\" value=\"")
                .append(e).append("\"");
            if (val.contains(e)) {
                ret.append(" checked=\"checked\" ");
            }
            ret.append(" >").append(e).append("</label><br/>");
        }
        return ret;
    }

    /**
     * Adds a default value to the properties.
     *
     * @param _class the class
     * @return the properties sys conf attribute
     */
    public BitEnumSysConfAttribute<E> addDefaultValue(final E _class)
    {
        if (getDefaultValue() == null) {
            defaultValue(new HashSet<E>());
        }
        getDefaultValue().add(_class);
        return getThis();
    }
}
