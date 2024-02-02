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
import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to delete selected objects in a row of a web table.<br/>
 * <b>Example:</b><br/>
 * <code>
 *   &lt;execute program="org.efaps.esjp.common.uitable.CommonDelete"&gt;
 *     &lt;property name="Select4Delete"&gt;SLEECT&lt;/property&gt;
 *   &lt;/execute&gt;
 * </code>
 *
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c0972b10-33c2-46e0-ba81-53a0341aaa35")
@EFapsApplication("eFaps-Kernel")
public abstract class CommonDelete_Base
    extends AbstractCommon
    implements EventExecution
{

    /**
     * @param _parameter parameters from the submitted web table
     * @throws EFapsException if a delete of the selected oids is not possible
     * @return new Return()
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
        if (oids != null) {
            for (final String oid : oids) {
                for (final Instance instance : getInstancesToDelete(_parameter, oid)) {
                    if (instance.isValid()) {
                        if (getValidate4Instance(_parameter, instance)) {
                            new Delete(instance).execute();
                        }
                    }
                }
            }
        }
        return new Return();
    }

    /**
     * Method to get the instance to be deleted.
     *
     * @param _parameter Parameter as passed from the eFaps API.
     * @param _oid of the instance that will be deleted
     * @return Instance for the oid
     * @throws EFapsException on error.
     */
    protected List<Instance> getInstancesToDelete(final Parameter _parameter,
                                                  final String _oid)
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<>();
        final Instance inst = Instance.get(_oid);
        final Map<Integer, String> properties = analyseProperty(_parameter, "Select4Delete");
        if (inst.isValid() && !properties.isEmpty()) {
            for (final String select : properties.values()) {
                final PrintQuery print = new PrintQuery(inst);
                print.addSelect(select);
                print.execute();
                final Object selObj = print.getSelect(select);
                if (selObj instanceof Instance) {
                    ret.add((Instance) selObj);
                } else if (selObj instanceof String) {
                    ret.add(Instance.get((String) selObj));
                }
            }
        } else {
            ret.add(inst);
        }
        return ret;
    }

    /**
     * Method to validate instance a delete.
     *
     * @param _parameter Parameter as passed from the eFaps API.
     * @param _instance instance that will be deleted
     * @return boolean.
     * @throws EFapsException on error.
     */
    protected boolean getValidate4Instance(final Parameter _parameter,
                                           final Instance _instance)
        throws EFapsException
    {
        return true;
    }
}
