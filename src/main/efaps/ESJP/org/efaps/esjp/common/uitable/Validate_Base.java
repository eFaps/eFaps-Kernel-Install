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
package org.efaps.esjp.common.uitable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("51b643c3-6f97-4e89-96d6-d58db84f7f72")
@EFapsApplication("eFaps-Kernel")
public abstract class Validate_Base
    extends AbstractCommon
{

    /**
     * Validate status 4 selected.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the eFaps exception
     */
    public Return validateStatus4Selected(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final List<Instance> selected = getSelectedInstances(_parameter);
        if (CollectionUtils.isNotEmpty(selected)) {
            final List<Instance> instances;
            if (containsProperty(_parameter, "Select4Instance")) {
                instances = new ArrayList<>();
                final String select = getProperty(_parameter, "Select4Instance");
                final MultiPrintQuery multi = new MultiPrintQuery(selected);
                multi.addSelect(select);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    instances.add(multi.getSelect(select));
                }
            } else {
                instances = selected;
            }
            final List<Status> statusList;
            if (containsProperty(_parameter, "SystemConfig")) {
                final String sysConfstr = getProperty(_parameter, "SystemConfig");
                final SystemConfiguration config;
                if (isUUID(sysConfstr)) {
                    config = SystemConfiguration.get(UUID.fromString(sysConfstr));
                } else {
                    config = SystemConfiguration.get(sysConfstr);
                }
                statusList = getStatusListFromProperties(_parameter,
                                config.getAttributeValueAsProperties(getProperty(_parameter, "Attribute")));
            } else {
                statusList = getStatusListFromProperties(_parameter);
            }
            boolean valid = true;
            for (final Instance inst : instances) {
                final Attribute statusAttr = inst.getType().getStatusAttribute();
                final PrintQuery print = new PrintQuery(inst);
                print.addAttribute(statusAttr);
                if (print.execute()) {
                    boolean validTmp = false;
                    final Long statusid = print.getAttribute(statusAttr);
                    for (final Status status : statusList) {
                        if (status.getId() == statusid) {
                            validTmp = true;
                            break;
                        }
                    }
                    valid = valid && validTmp;
                }
            }
            if (valid) {
                ret.put(ReturnValues.TRUE, true);
            } else {
                final StringBuilder html = new StringBuilder()
                                .append(getDBProperty("InvalidObjectSelected"))
                                .append("<br/><br/>");
                html.append(statusList.stream().map(m -> m.getLabel()).distinct().collect(Collectors.joining("<br/>")));
                ret.put(ReturnValues.SNIPLETT, html);
            }
        }
        return ret;
    }
}
