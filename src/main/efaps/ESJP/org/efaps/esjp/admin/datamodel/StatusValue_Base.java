/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.esjp.admin.datamodel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.Update;
import org.efaps.esjp.common.uiform.Edit;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Status from the Database.<br>
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c0e0547a-a39f-4741-ae3d-c196e8cb2f60")
@EFapsRevision("$Rev$")
public abstract class StatusValue_Base
    implements EventExecution
{

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter    parameter as defined by the efaps api
     * @return map with value and keys
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();

        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final Map<String, String> map = new TreeMap<String, String>();

        if (fieldValue.getTargetMode().equals(TargetMode.VIEW) || fieldValue.getTargetMode().equals(TargetMode.PRINT)
                         || fieldValue.getTargetMode().equals(TargetMode.UNKNOWN)) {
            final Status status = Status.get((Long) fieldValue.getValue());
            map.put(status.getLabel(), ((Long) status.getId()).toString());
        } else {
            final Type type = fieldValue.getInstance().getType().getStatusAttribute().getLink();
            final StatusGroup group = Status.get(type.getName());
            for (final Status status : group.values()) {
                map.put(status.getLabel(), ((Long) status.getId()).toString());
            }
        }
        ret.put(ReturnValues.VALUES, map);
        return ret;
    }

    /**
     * Method to set the Status for an Instance.<br>
     *
     * <pre>
     * <code>
     *  &lt;execute program=&quot;org.efaps.esjp.admin.datamodel.StatusValue&quot; method=&quot;setStatus&quot;&gt;
     *      &lt;property name=&quot;Status&quot;&gt;Open&lt;/property&gt;
     *  &lt;/execute&gt;
     *  <br>
     * </code>
     * </pre>
     * @param _parameter parameter as defined by the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return setStatus(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String statusName = (String) props.get("Status");
        final boolean updateInstance = "true".equalsIgnoreCase((String) props.get("UpdateInstance"));
        final boolean evalOID = "true".equalsIgnoreCase((String) props.get("EvalOID"));
        final String noUpdateStatiStr = (String) props.get("NoUpdateStatus");

        final Set<Instance> instances = new HashSet<Instance>();
        if (evalOID) {
            final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
            for (final String oid : oids) {
                final Instance inst = Instance.get(oid);
                if (inst.isValid()) {
                    instances.add(inst);
                }
            }
        } else {
            instances.add(_parameter.getInstance());
        }

        for (final Instance inst : instances) {
            final Status status = Status.find(inst.getType().getStatusAttribute().getLink().getName(), statusName);
            if (status != null) {
                boolean doUpdate = true;
                if (noUpdateStatiStr != null && !noUpdateStatiStr.isEmpty()) {
                    final PrintQuery print = new PrintQuery(inst);
                    print.addAttribute(inst.getType().getStatusAttribute());
                    print.execute();
                    final Long currStatusId = print.<Long>getAttribute(inst.getType().getStatusAttribute());
                    final String[] noUpdateStati = noUpdateStatiStr.split(";");
                    for (final String noUpdateStatus : noUpdateStati) {
                        final Status noUp = Status.find(inst.getType().getStatusAttribute().getLink().getName(),
                                        noUpdateStatus);
                        if (noUp != null && currStatusId == noUp.getId()) {
                            doUpdate = false;
                            break;
                        }
                    }
                }
                if (doUpdate) {
                    // the instance must be updated first, because after changing the status the
                    //user might not have the right to update any more
                    if (updateInstance) {
                        _parameter.put(ParameterValues.INSTANCE, inst);
                        updateInstance(_parameter);
                    }
                    final Update update = new Update(inst);
                    update.add(inst.getType().getStatusAttribute(), ((Long) status.getId()).toString());
                    update.execute();
                }
            }
        }
        final Return ret = new Return();
        return ret;
    }

    /**
     * @param _parameter parameter as defined by the eFaps API
     * @throws EFapsException on error
     */
    protected void updateInstance(final Parameter _parameter)
        throws EFapsException
    {
        new Edit().execute(_parameter);
    }
}
