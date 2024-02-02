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
package org.efaps.esjp.admin.util;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.Command;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class UserAttrUtil.
 */
@EFapsUUID("348f85cf-16f6-4575-a13e-90cbaa832839")
@EFapsApplication("eFaps-Kernel")
public class UserAttributeUtil
    extends AbstractUtil
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserAttributeUtil.class);

    /**
     * Validate user attributes.
     *
     * @param _parameter the parameter
     * @throws EFapsException the e faps exception
     */
    public void validateUserAttributes(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            final Pattern pattern = Pattern.compile(UUIDUtil.UUID_REGEX);
            final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString("67c4f961-74b2-466a-81ae-80659b0a43a1"));
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute("Modified", "Key");
            multi.execute();
            while (multi.next()) {
                final String key = multi.getAttribute("Key");
                LOG.info("Validating {}", key);
                if (key.matches("^" + UUIDUtil.UUID_REGEX + ".*")) {
                    final Matcher matcher = pattern.matcher(key);
                    matcher.find();
                    final String uuid = matcher.group();
                    final Command command = Command.get(UUID.fromString(uuid));
                    if (command != null) {
                        LOG.info("Found command {}", command);
                        if (command.getTargetTable() != null) {
                            LOG.info("--> Found table {}", command.getTargetTable());
                            final PrintQuery print = new PrintQuery(CIAdminUserInterface.Table.getType(),
                                            command.getTargetTable().getId());
                            final SelectBuilder sel = SelectBuilder.get().linkto("RevisionLink")
                                            .attribute("Modified");
                            print.addSelect(sel);
                            print.execute();
                            final DateTime revDate = print.getSelect(sel);
                            final DateTime attrDate = multi.getAttribute("Modified");
                            if (attrDate.isBefore(revDate)) {
                                LOG.info("    - Removing old info");
                                new Delete(multi.getCurrentInstance()).execute();
                            }
                        }
                    }
                }
            }
        }
    }
}
