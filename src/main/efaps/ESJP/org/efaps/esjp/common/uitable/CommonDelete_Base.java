/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.esjp.common.uitable;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to delete selected objects in a row of a web table.<br/>
 * <b>Example:</b><br/> <code>
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
@EFapsRevision("$Rev$")
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
                final Instance instance = getInstance(_parameter, oid);
                if (instance.isValid()) {
                    if (getValidate4Instance(_parameter, instance)) {
                        new Delete(oid).execute();
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
     * @param _oid of the  instance that will be deleted
     * @return Instance for the oid
     * @throws EFapsException on error.
     */
    protected Instance getInstance(final Parameter _parameter,
                                   final String _oid) throws EFapsException
    {
        Instance ret = Instance.get(_oid);
        if (ret.isValid() && containsProperty(_parameter, "Select4Delete")) {
            final String select = getProperty(_parameter, "Select4Delete");
            final PrintQuery print = new PrintQuery(ret);
            print.addSelect(select);
            print.execute();
            final Object selObj = print.getSelect(select);
            if (selObj instanceof Instance) {
                ret = (Instance) selObj;
            } else if (selObj instanceof String) {
                ret = Instance.get((String) selObj);
            }
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
