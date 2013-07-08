/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.history;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * TODO description
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("be48d384-c721-470c-b099-7141874ca537")
@EFapsRevision("$Rev$")
public abstract class History_Base
{

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
        final QueryBuilder queryBldr = new QueryBuilder(CICommon.HistoryAbstract);
        queryBldr.addWhereAttrEqValue(CICommon.HistoryAbstract.GeneralInstanceLink, _parameter.getInstance()
                        .getGeneralId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CICommon.HistoryAbstract.Value, CICommon.HistoryAbstract.Created,
                        CICommon.HistoryAbstract.Creator);
        multi.execute();
        while (multi.next()) {
            html.append("<tr>");
            final DateTime created = multi.<DateTime>getAttribute(CICommon.HistoryAbstract.Created);
            html.append("<td>").append(created).append("</td>");

            final Person person = multi.<Person>getAttribute(CICommon.HistoryAbstract.Creator);
            html.append("<td>").append(person.getLastName()).append(", ").append(person.getFirstName()).append("</td>");

            final String xml = multi.<String>getAttribute(CICommon.HistoryAbstract.Value);
            try {
                final JAXBContext jc = JAXBContext.newInstance(AbstractHistoryTrigger_Base.getClasses(_parameter));
                final Unmarshaller unmarshaller = jc.createUnmarshaller();
                final StringReader reader = new StringReader(xml);
                final Object obj = unmarshaller.unmarshal(reader);
                if (obj instanceof IHistoryHtml) {
                    final IHistoryHtml hObj = (IHistoryHtml) obj;
                    html.append("<td>").append(hObj.getTypeColumnValue()).append("</td>");
                    html.append("<td>").append(hObj.getDescriptionColumnValue()).append("</td>");
                }
            } catch (final JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            html.append("</tr>");
        }
        html.append("</table>");
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }
}
