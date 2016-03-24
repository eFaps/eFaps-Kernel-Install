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
package org.efaps.esjp.admin.util;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.admin.common.systemconfiguration.ISysConfAttribute;
import org.efaps.esjp.admin.common.systemconfiguration.SysConfResourceConfig;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SysConfigUtil.
 *
 * @author The eFaps Team
 */
@EFapsUUID("318a73ec-6893-432e-8282-e8b303c0a8d5")
@EFapsApplication("eFaps-Kernel")
public class SysConfigUtil
    extends AbstractUtil
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SysConfigUtil.class);

    /**
     * Validate the used SytemConfigurations.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    public void validateSysConf(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.SystemConfigurationAttribute);
            queryBldr.addType(CIAdminCommon.SystemConfigurationLink);
            queryBldr.addOrderByAttributeAsc(CIAdminCommon.SystemConfigurationAbstract.Key);
            final MultiPrintQuery multi = queryBldr.getPrint();
            final SelectBuilder selUUID = SelectBuilder.get().linkto(
                            CIAdminCommon.SystemConfigurationAbstract.AbstractLink).attribute(
                                            CIAdminCommon.SystemConfiguration.UUID);
            final SelectBuilder selName = SelectBuilder.get().linkto(
                            CIAdminCommon.SystemConfigurationAbstract.AbstractLink).attribute(
                                            CIAdminCommon.SystemConfiguration.Name);
            multi.addSelect(selUUID, selName);
            multi.addAttribute(CIAdminCommon.SystemConfigurationAbstract.Key);
            multi.setEnforceSorted(true);
            multi.execute();
            while (multi.next()) {
                final String uuid = multi.getSelect(selUUID);
                String key = multi.getAttribute(CIAdminCommon.SystemConfigurationAbstract.Key);

                final boolean isLink = multi.getCurrentInstance().getType().isCIType(
                                CIAdminCommon.SystemConfigurationLink);
                ISysConfAttribute attr = isLink ? SysConfResourceConfig.getResourceConfig().getLink(uuid, key)
                                : SysConfResourceConfig.getResourceConfig().getAttribute(uuid, key);

                if (attr == null && key.matches(".*\\d\\d$")) {
                    key = StringUtils.left(key, key.length() - 2);
                    attr = isLink ? SysConfResourceConfig.getResourceConfig().getLink(uuid, key)
                                    : SysConfResourceConfig.getResourceConfig().getAttribute(uuid, key);
                }
                if (attr == null) {
                    LOG.warn("Did not find Key {} defined in {}", key, multi.getSelect(selName));
                } else {
                    LOG.debug("Found Key {}", key);
                }
            }
        }
    }
}
