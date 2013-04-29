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


package org.efaps.esjp.admin.access;

import java.util.List;
import java.util.Map;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3b81f1a7-8ec0-4979-859e-0bd64e4d50d7")
@EFapsRevision("$Rev$")
public abstract class AccessCheckAbstract_Base
    implements EventExecution
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AccessCheckAbstract_Base.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
        final Instance instance = _parameter.getInstance();

        final Return ret = new Return();
        if (instance != null) {
            if (Context.getThreadContext().getPerson() == null || checkAccess(_parameter, instance, accessType)) {
                ret.put(ReturnValues.TRUE, true);
            }
        } else {
            final List<?> instances = (List<?>) _parameter.get(ParameterValues.OTHERS);
            ret.put(ReturnValues.VALUES, checkAccess(_parameter, instances, accessType));
        }
        return ret;
    }

    /**
     * Check for the instance object if the current context user has the access
     * defined in the list of access types.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instance     instance to check for access for
     * @param _accessType   accesstyep to check the access for
     * @return true if access is granted, else false
     * @throws EFapsException on error
     */
    protected abstract boolean checkAccess(final Parameter _parameter,
                                           final Instance _instance,
                                           final AccessType _accessType)
        throws EFapsException;


    /**
     * Method to check the access for a list of instances.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _instances    instances to be checked
     * @param _accessType   type of access
     * @return map of access to boolean
     * @throws EFapsException on error
     */
    protected abstract Map<Instance, Boolean> checkAccess(final Parameter _parameter,
                                                          final List<?> _instances,
                                                          final AccessType _accessType)
        throws EFapsException;
}
