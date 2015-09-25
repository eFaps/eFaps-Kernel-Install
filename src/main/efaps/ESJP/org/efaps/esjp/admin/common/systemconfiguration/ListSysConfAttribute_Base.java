/*
 * Copyright 2003 - 2015 The eFaps Team
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
@EFapsUUID("ad2c0d04-d055-402a-891a-6bff9b5848dd")
@EFapsApplication("eFaps-Kernel")
public abstract class ListSysConfAttribute_Base
    extends AbstractSysConfAttribute<ListSysConfAttribute, List<String>>
{

    @Override
    protected ListSysConfAttribute getThis()
    {
        return (ListSysConfAttribute) this;
    }

    @Override
    public List<String> get()
        throws EFapsException
    {
        final List<String> ret = new ArrayList<>();
        final String str = SystemConfiguration.get(getSysConfUUID()).getAttributeValue(getKey());
        if (str != null && !str.isEmpty()) {
            ret.addAll(Arrays.asList(StringUtils.split(str, "\n")));
        }
        return ret;
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value)
    {
        final StringBuilder ret = new StringBuilder()
                        .append("<textarea rows=\"5\" cols=\"80\" name=\"value\">");
        if (_value != null) {
            ret.append(StringEscapeUtils.escapeHtml4((String) _value));
        }
        ret.append("</textarea>");
        return ret;
    }

}