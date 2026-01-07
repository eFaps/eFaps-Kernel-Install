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
package org.efaps.esjp.common.autocomplete;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.eql.EQL;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.esjp.common.properties.PropertiesUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("a6638b88-befb-4d24-aa44-f717ecf5a615")
@EFapsApplication("eFaps-Kernel")
public class BaseAutoComplete
{

    private static final Logger LOG = LoggerFactory.getLogger(BaseAutoComplete.class);

    public Return autoCompleteBySysConf(final Parameter parameter)
        throws EFapsException
    {

        final var systemConfigKey = PropertiesUtil.getProperty(parameter, "SystemConfig");
        if (systemConfigKey == null) {
            LOG.warn("Missing property SystemConfig");
        }
        SystemConfiguration sysConf;
        if (UUIDUtil.isUUID(systemConfigKey)) {
            sysConf = SystemConfiguration.get(UUID.fromString(systemConfigKey));
        } else {
            sysConf = SystemConfiguration.get(systemConfigKey);
        }

        final var systemConfigAttribute = PropertiesUtil.getProperty(parameter, "SystemConfigAttribute");
        if (systemConfigAttribute == null) {
            LOG.warn("Missing property SystemConfigAttribute");
        }
        final var props = sysConf.getAttributeValueAsProperties(systemConfigAttribute);

        String eql = props.getProperty("eql");
        final String valueMsgFormat = props.getProperty("valueMsgFormat");
        final String choiceMsgFormat = props.getProperty("choiceMsgFormat");
        final String keySelect = props.getProperty("keySelect");

        String term = (String) parameter.get(ParameterValues.OTHERS);

        if ("*".equals(term)) {
            term = "";
        }

        eql = eql.replace("TERM", term);

        final var stmt = EQL.parse(eql);

        final List<Map<String, String>> list = new ArrayList<>();
        if (stmt instanceof final IPrintQueryStatement printQueryStmt) {
            final var attributeLength = printQueryStmt.getSelection().getSelectsLength();
            final var eval = PrintStmt.get(printQueryStmt).evaluate();
            while (eval.next()) {
                final var arr = new Object[attributeLength];
                for (int i = 0; i < attributeLength; i++) {
                    final var gg = eval.get(i + 1);
                    arr[i] = gg;
                }
                final Map<String, String> map = new HashMap<>();
                final var value = MessageFormat.format(valueMsgFormat, arr);
                final var choice = MessageFormat.format(choiceMsgFormat, arr);
                map.put("eFapsAutoCompleteKEY", keySelect == null ? eval.inst().getOid() : eval.get(keySelect));
                map.put("eFapsAutoCompleteVALUE", value);
                map.put("eFapsAutoCompleteCHOICE", choice);
                list.add(map);
            }
        } else {
            LOG.warn("IPrintQueryStatement was expected");
        }

        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, list);
        return retVal;
    }
}
