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
package org.efaps.esjp.common.history;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIType;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.eql.EQL;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO description.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("be48d384-c721-470c-b099-7141874ca537")
@EFapsApplication("eFaps-Kernel")
public abstract class History_Base
    extends AbstractCommon
{
    private static final Logger LOG = LoggerFactory.getLogger(History.class);

    /**
     * @param _parameter Parameter as passed by the efasp API
     * @return filed for the history
     * @throws EFapsException on error
     */
    public Return getLogFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        html.append("<table>")
                        .append("<tr>")
                        .append("<th>").append(DBProperties.getProperty("org.efaps.esjp.common.history.History.Date"))
                        .append("</th>")
                        .append("<th>").append(DBProperties.getProperty("org.efaps.esjp.common.history.History.User"))
                        .append("</th>")
                        .append("<th>").append(DBProperties.getProperty("org.efaps.esjp.common.history.History.Type"))
                        .append("</th>")
                        .append("<th>").append(DBProperties.getProperty("org.efaps.esjp.common.history.History.Desc"))
                        .append("</th>")
                        .append("</tr>");

        final Map<Integer, String> types = analyseProperty(_parameter, "Type");
        final QueryBuilder queryBldr;
        if (types.isEmpty()) {
            queryBldr = new QueryBuilder(CICommon.HistoryAbstractUpdate);
        } else {
            final Iterator<String> iter = types.values().iterator();
            String typeStr = iter.next();
            queryBldr = new QueryBuilder(isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr));
            while (iter.hasNext()) {
                typeStr = iter.next();
                queryBldr.addType(isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr));
            }
        }
        queryBldr.addWhereAttrEqValue(CICommon.HistoryAbstract.GeneralInstanceLink, _parameter.getInstance()
                        .getGeneralId());
        queryBldr.addOrderByAttributeDesc(CICommon.HistoryAbstract.Created);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.setEnforceSorted(true);
        multi.addAttribute(CICommon.HistoryAbstract.Value, CICommon.HistoryAbstract.Created,
                        CICommon.HistoryAbstract.Creator);
        multi.execute();
        StringBuilder group = new StringBuilder();
        String compare = "";
        int count = 1;
        while (multi.next()) {
            group.append("<tr>");
            final DateTime created = multi.<DateTime>getAttribute(CICommon.HistoryAbstract.Created);
            final Person person = multi.<Person>getAttribute(CICommon.HistoryAbstract.Creator);
            final String name = person.getLastName() + ", " + person.getFirstName();
            final String compareKey = created + name;

            if (!compare.equals(compareKey)) {
                final String groupStr = group.toString().replace("ROWSPAN", "rowspan=\"" + count + "\"");
                html.append(groupStr);
                compare = compareKey;
                group = new StringBuilder();
                count = 1;
                group.append("<td ROWSPAN>").append(created).append("</td>");
                group.append("<td ROWSPAN>").append(name).append("</td>");
            } else {
                count++;
            }

            final IHistoryHtml hObj = multi.<IHistoryHtml>getAttribute(CICommon.HistoryAbstract.Value);
            group.append("<td>").append(hObj.getTypeColumnValue()).append("</td>");
            group.append("<td>").append(hObj.getDescriptionColumnValue()).append("</td>");
            group.append("</tr>");
        }
        final String groupStr = group.toString().replace("ROWSPAN", "rowspan=\"" + count + "\"");
        html.append(groupStr);
        html.append("</table>");
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    public HashSet<Instance> getLatest(final OffsetDateTime afterDateTime,
                                       final CIType... ciTypes)
        throws EFapsException
    {
        final var typeIds = new HashSet<Long>();
        if (ArrayUtils.isNotEmpty(ciTypes)) {
            for (final var ciType : ciTypes) {
                final var type = ciType.getType();
                typeIds.add(type.getId());
                type.getChildTypes().forEach(child -> {
                    typeIds.add(child.getId());
                });
            }
        }
        return getLatest(afterDateTime, typeIds.toArray(new Long[typeIds.size()]));
    }

    public HashSet<Instance> getLatest(final OffsetDateTime afterDateTime,
                                       final Long... typeIds)
        throws EFapsException
    {
        LOG.debug("Call for latest by history with {} and {}", afterDateTime, typeIds);
        final var instancesTmp = new HashSet<Instance>();
        final var after = afterDateTime.atZoneSameInstant(DateTimeUtil.getDBZoneId()).toLocalDateTime().toString();

        final var query = EQL.builder().print()
                        .query(CICommon.HistoryLatest)
                        .where().attribute(CICommon.HistoryLatest.Latest).greater(after);

        if (ArrayUtils.isNotEmpty(typeIds)) {
            query.and().attribute(CICommon.HistoryLatest.InstTypeId).in(typeIds);
        }

        final var eval = query.select()
                        .attribute(CICommon.HistoryLatest.InstTypeId, CICommon.HistoryLatest.InstId)
                        .evaluate();
        while (eval.next()) {
            final Long instTypeId = eval.get(CICommon.HistoryLatest.InstTypeId);
            final Long instId = eval.get(CICommon.HistoryLatest.InstId);
            instancesTmp.add(Instance.get(Type.get(instTypeId), instId));
        }

        LOG.debug("Found instances {}", instancesTmp);

        final var instances = new HashSet<Instance>();

        if (!instancesTmp.isEmpty()) {
            // check access to the instances
            final var verifyEval = EQL.builder().print(instancesTmp.toArray(new Instance[instancesTmp.size()]))
                            .oid()
                            .evaluate();
            while (verifyEval.next()) {
                instances.add(verifyEval.inst());
            }
        }
        return instances;
    }
}
