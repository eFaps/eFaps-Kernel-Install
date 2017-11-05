/*
 * Copyright 2003 - 2017 The eFaps Team
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

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Class PropertiesSysConfAttribute_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("a1085143-11c3-409b-a5c5-bcf5c302b2a4")
@EFapsApplication("eFaps-Kernel")
public abstract class PropertiesSysConfAttribute_Base
    extends AbstractSysConfAttribute<PropertiesSysConfAttribute, Properties>
    implements IConcatenate
{

    /** Can be concatenated. */
    private boolean concatenate;

    /**
     * Concatenate.
     *
     * @param _concatenate the _concatenate
     * @return the properties sys conf attribute
     */
    public PropertiesSysConfAttribute concatenate(final boolean _concatenate)
    {
        this.concatenate = _concatenate;
        return getThis();
    }

    /**
     * Getter method for the instance variable {@link #concatenate}.
     *
     * @return value of instance variable {@link #concatenate}
     */
    @Override
    public boolean isConcatenate()
    {
        return this.concatenate;
    }

    @Override
    protected PropertiesSysConfAttribute getThis()
    {
        return (PropertiesSysConfAttribute) this;
    }

    @Override
    public Properties get()
        throws EFapsException
    {
        final Properties ret;
        if (SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey())) {
            ret = SystemConfiguration.get(getSysConfUUID()).getAttributeValueAsProperties(getKey(), this.concatenate);
        } else if (getDefaultValue() != null) {
            ret = getDefaultValue();
        } else {
            ret = new Properties();
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value,
                                final String _fieldName)
    {
        final StringBuilder ret = new StringBuilder()
                        .append("<textarea rows=\"5\" cols=\"80\" name=\"").append(_fieldName).append("\">");
        if (_value != null) {
            ret.append(StringEscapeUtils.escapeHtml4((String) _value));
        } else if (getDefaultValue() != null) {
            final StringBuilder str = new StringBuilder();
            for (final Entry<Object, Object> entry: getDefaultValue().entrySet()) {
                if (str.length() > 0) {
                    str.append("\n");
                }
                str.append(entry.getKey()).append("=").append(entry.getValue());
            }
            ret.append(StringEscapeUtils.escapeHtml4(str.toString()));
        }
        ret.append("</textarea>");
        return ret.toString();
    }

    /**
     * Adds a default value to the properties.
     *
     * @param _key the _key
     * @param _value the _value
     * @return the properties sys conf attribute
     */
    public PropertiesSysConfAttribute addDefaultValue(final String _key,
                                                      final String _value)
    {
        if (getDefaultValue() == null) {
            defaultValue(new Properties());
        }
        getDefaultValue().put(_key, _value);
        return getThis();
    }
}
