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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.bpm.BPM;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
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
}
