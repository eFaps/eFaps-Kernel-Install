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

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The Class SysConfLink_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("4bca5e6d-b085-456a-a814-4e90e0fce254")
@EFapsApplication("eFaps-Kernel")
public abstract class SysConfLink_Base
    extends AbstractSysConfAttribute<SysConfLink, Instance>
    implements ISysConfLink
{
    @Override
    protected SysConfLink getThis()
    {
        return (SysConfLink) this;
    }

    @Override
    public Instance get()
        throws EFapsException
    {
        return SystemConfiguration.get(getSysConfUUID()).getLink(getKey());
    }

    @Override
    public CharSequence getHtml(final Parameter _parameter,
                                final Object _value)
    {
        final StringBuilder ret = new StringBuilder()
                        .append("<input type=\"text\" name=\"value\" size=\"70\"");
        if (_value != null) {
            ret.append(" value=\"").append(StringEscapeUtils.escapeHtml4((String) _value)).append("\"");
        }
        ret.append(">");
        return ret;
    }
}
