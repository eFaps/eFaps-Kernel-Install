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

package org.efaps.esjp.bpm;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.bpm.BPM;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CIBPM;
import org.efaps.util.EFapsException;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("19f2abbe-4632-4ad6-a3aa-c26c133e7f26")
@EFapsRevision("$Rev$")
public abstract class BProcess_Base
{

    public List<TaskSummary> getTaskSummary4Instance(final Parameter _parameter,
                                                     final String _taskName,
                                                     final List<org.jbpm.task.Status> _status)
        throws EFapsException
    {
        final List<TaskSummary> ret = new ArrayList<TaskSummary>();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String taskName = _taskName == null ? (String) properties.get("TaskName") : _taskName;
        final List<org.jbpm.task.Status> status;
        if (_status == null) {
            status = new ArrayList<org.jbpm.task.Status>();
            for (final org.jbpm.task.Status statusTmp : org.jbpm.task.Status.values()) {
                status.add(statusTmp);
            }
        } else {
            status = _status;
        }

        final QueryBuilder queryBldr = new QueryBuilder(CIBPM.GeneralInstance2ProcessId);
        queryBldr.addWhereAttrEqValue(CIBPM.GeneralInstance2ProcessId.GeneralInstanceLink, _parameter.getInstance()
                        .getGeneralId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIBPM.GeneralInstance2ProcessId.ProcessId);
        multi.executeWithoutAccessCheck();

        while (multi.next()) {
            final Long processInstanceId = multi.<Long>getAttribute(CIBPM.GeneralInstance2ProcessId.ProcessId);
            ret.addAll(BPM.getTasksByStatusByProcessIdByTaskName(processInstanceId,
                            status,
                            taskName));
        }
        return ret;
    }


    public Return getImageFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final Instance instance = _parameter.getInstance();

        if (instance.getType().isKindOf(CIAdminProgram.BPM.getType())) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.BPM2Image);
            queryBldr.addWhereAttrEqValue(CIAdminProgram.BPM2Image.FromLink, instance);
            final MultiPrintQuery multi = queryBldr.getPrint();
            final SelectBuilder sel = SelectBuilder.get().linkto(CIAdminProgram.BPM2Image.ToLink).instance();
            multi.addSelect(sel);
            multi.execute();
            while (multi.next()) {
                final Instance imageInst = multi.<Instance>getSelect(sel);
                final Checkout checkout = new Checkout(imageInst);
                final InputStream ins = checkout.execute();
                final StringWriter strb = new StringWriter();
                try {
                    IOUtils.copy(ins, strb);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                html.append(strb);
            }
        }

        if (html.length() < 1) {
            html.append(DBProperties.getProperty("org.efaps.esjp.bpm.BProcess.NoImage"));
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }
}
