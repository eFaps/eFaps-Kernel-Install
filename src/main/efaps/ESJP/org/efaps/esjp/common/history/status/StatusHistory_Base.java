/*
 * Copyright 2003 - 2020 The eFaps Team
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
package org.efaps.esjp.common.history.status;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("1d135493-bc9e-4cb9-93b2-87860993281f")
@EFapsApplication("eFaps-Kernel")
public abstract class StatusHistory_Base
{
    private static final Logger LOG = LoggerFactory.getLogger(StatusHistory.class);

    public Return getFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance objectInst = InstanceUtils.isValid(_parameter.getInstance()) ? _parameter.getInstance()
                        : _parameter.getCallInstance();

        final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminCommon.GeneralInstance);
        attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.InstanceTypeID, objectInst.getType().getId());
        attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.InstanceID, objectInst.getId());

        final QueryBuilder queryBldr = new QueryBuilder(CICommon.HistoryStatus);
        queryBldr.addWhereAttrInQuery(CICommon.HistoryStatus.GeneralInstanceLink,
                        attrQueryBldr.getAttributeQuery(CIAdminCommon.GeneralInstance.ID));
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CICommon.HistoryStatus.Created, CICommon.HistoryStatus.Creator,
                        CICommon.HistoryStatus.StatusLink);
        multi.executeWithoutAccessCheck();
        final StringBuilder html = new StringBuilder()
                        .append("<ul style=\"list-style: disclosure-closed;")
                        .append("display: flex;flex-direction: row;flex-wrap: wrap;\">");
        boolean first = true;
        final DateTimeFormatter formatter = DateTimeFormat.shortDateTime()
                        .withLocale(Context.getThreadContext().getLocale());
        while (multi.next()) {
            final DateTime created = multi.getAttribute(CICommon.HistoryStatus.Created);
            final Status status = Status.get(multi.<Long>getAttribute(CICommon.HistoryStatus.StatusLink));
            final Person creator = multi.getAttribute(CICommon.HistoryStatus.Creator);
            html.append("<li style=\"border: 2px solid;border-radius: 20px;padding: 5px;")
                    .append("display: flex;flex-direction: column;text-align: center;position: relative");
            if (first) {
                html.append("margin: 0 0 10px 0;\">");
                first = false;
            } else {
                html.append("margin: 0 0 10px 30px;\">");
                html.append("<span style=\"border-top: 6px solid;position: absolute;left: -36px;top: 26px;")
                    .append("width: 20px;height: 20px;border-right: 6px solid;transform: rotate(45deg);\"></span>");
            }
            html.append("<span style=\"font-weight: bold;padding: 5px;font-size: large;\">")
                    .append(status.getLabel()).append("</span>")
                .append("<span style=\"padding: 5px;\">")
                    .append(creator.getFirstName()).append(" ").append(creator.getLastName()).append("</span>")
                .append("<span style=\"padding: 5px;\">").append(created.toString(formatter)).append("</span>")
                .append("</li>");
        }
        html.append("</ul>");

        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    /**
     *
     * @param _parameter Parameter
     * @param _date    the date to be evaluated, mining what status did the object have onthe given date
     * @param _minDate just a min value to be included in the evaluation
     * @param typeIds ids to be filterd for
     * @return mapping
     * @throws EFapsException on error
     */
    public Map<Instance, Long> getStatusUpdatesByDateAndTypes(final Parameter _parameter,
                                                              final DateTime _date,
                                                              final DateTime _minDate,
                                                              final Long... typeIds)
        throws EFapsException
    {
        // security treshhold
        final LocalDate minDate = _minDate == null ? _date.minusYears(1).toLocalDate() : _minDate.toLocalDate();

        final Map<Instance, Long> inst2status = new HashMap<>();
        final Context context = Context.getThreadContext();
        final StringBuilder cmd = new StringBuilder()
                        .append("SELECT")
                        .append(" DISTINCT ON (geninstid) geninstid,")
                        .append(" statusid, insttypeid, instid")
                        .append(" FROM")
                        .append(" t_cmhistorystatus")
                        .append(" LEFT JOIN t_cmgeninst ON t_cmgeninst.id = t_cmhistorystatus.genInstId")
                        .append(" WHERE t_cmhistorystatus.created < '")
                            .append(_date.withTimeAtStartOfDay().plusDays(1).toLocalDate()).append("'")
                        .append(" AND t_cmhistorystatus.created > '").append(minDate).append("'")
                        .append(" AND t_cmgeninst.insttypeid IN (").append(StringUtils.join(typeIds, ",")).append(")")
                        .append(" ORDER BY geninstid, created");

        ConnectionResource con = null;
        try {
            con = context.getConnectionResource();
            LOG.debug("Querying StatusHistory with: {}", cmd);
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                while (rs.next()) {
                    final long statusid = rs.getLong(2);
                    final long insttypeid = rs.getLong(3);
                    final long instid = rs.getLong(4);
                    final Instance inst = Instance.get(Type.get(insttypeid), instid);
                    inst2status.put(inst, statusid);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (final SQLException e) {
            LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        }
        return inst2status;
    }
}
