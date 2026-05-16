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

import java.util.Properties;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("7f32d03c-d0d1-4cf3-959d-c917a0800433")
@EFapsApplication("eFaps-Kernel")
public class SystemConfigUtil
{

    private static final Logger LOG = LoggerFactory.getLogger(SystemConfigUtil.class);

    public static Properties getProperties(final String systemConfigKey,
                                           final String attribute)
        throws EFapsException
    {
        Properties props = new Properties();
        SystemConfiguration sysConf;
        if (UUIDUtil.isUUID(systemConfigKey)) {
            sysConf = SystemConfiguration.get(UUID.fromString(systemConfigKey));
        } else {
            sysConf = SystemConfiguration.get(systemConfigKey);
        }
        if (sysConf == null) {
            LOG.warn("Could not find SystemConfiguration for: {}", systemConfigKey);
            return null;
        }
        if (sysConf.containsAttributeValue(attribute)) {
            props = sysConf.getAttributeValueAsProperties(attribute);
        } else {
            final var rsrcConfig = SysConfResourceConfig.getResourceConfig();
            final var confAttr = (PropertiesSysConfAttribute) rsrcConfig
                            .getAttribute(sysConf.getUUID().toString(), attribute);
            if (confAttr != null && confAttr.getDefaultValue() != null) {
                props = confAttr.getDefaultValue();
            }
        }
        return props;
    }

}
