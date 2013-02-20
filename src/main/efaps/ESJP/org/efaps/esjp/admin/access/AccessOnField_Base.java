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

import java.util.Map;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.user.Company;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class is used as an esjp to give access on a field. It contains methods that
 * allows to hide fields when necessary.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ed8d307f-faed-4e4b-9251-1280658d2945")
@EFapsRevision("$Rev$")
public abstract class AccessOnField_Base
    implements EventExecution
{

    /**
     * Not used here.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @throws EFapsException never
     * @return null
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        return null;
    }

    /**
     * Method is used to control access based on a list of given Roles.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return roleCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String roleStr = (String) properties.get("Roles");
        final String[] roles = roleStr.split(";");
        for (final String role : roles) {
            final Role aRol = Role.get(role);
            if (aRol != null) {
                final boolean assigned = Context.getThreadContext().getPerson().isAssigned(aRol);
                if (assigned) {
                    ret.put(ReturnValues.TRUE, true);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Method is used to control access based the current Company.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return companyCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String companyStr = (String) properties.get("Companies");
        final String[] companies = companyStr.split(";");
        for (final String company : companies) {
            final Company aCompany = Company.get(company);
            if (aCompany != null) {
                if (Context.getThreadContext().getCompany().equals(aCompany)) {
                    ret.put(ReturnValues.TRUE, true);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Method is used to control access based on a Property of esjp.
     * Used for e.g. hiding a field in a form depending on a property of
     * the calling command.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return propertyCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> porps = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final Field field = (Field) _parameter.get(ParameterValues.UIOBJECT);
        if ("true".equalsIgnoreCase((String) porps.get("CheckCallingCommand"))) {
            final AbstractCommand cmd = (AbstractCommand) _parameter.get(ParameterValues.CALL_CMD);
            if (cmd != null) {
                if ("true".equalsIgnoreCase(cmd.getProperty(field.getName() + "_UIAccessCheck"))) {
                    ret.put(ReturnValues.TRUE, true);
                }
            }
        } else {
            if ("true".equalsIgnoreCase((String) porps.get("UIAccessCheck"))) {
                ret.put(ReturnValues.TRUE, true);
            }
        }
        return ret;
    }


    /**
     * Method is used to control access based on a SystemConfiguration.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return configCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final SystemConfiguration config = SystemConfiguration.get((String) props.get("SystemConfig"));
        if (config != null) {
            final Boolean access = config.getAttributeValueAsBoolean((String) props.get("Attribute"));
            final boolean inverse = "true".equalsIgnoreCase((String) props.get("Inverse"));
            if ((!inverse && access) || (inverse && !access)) {
                ret.put(ReturnValues.TRUE, true);
            }
        }
        return ret;
    }

    /**
     * Method is used to deny access in EDITMODE.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     */
    public Return hideOnEdit(final Parameter _parameter)
    {
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        final Return ret = new Return();

        if (!mode.equals(TargetMode.EDIT)) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Method is used to deny access in SearchMode.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     */
    public Return hideOnSearch(final Parameter _parameter)
    {
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        final Return ret = new Return();

        if (!mode.equals(TargetMode.SEARCH)) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Method is used to give access only on VIEWMODE.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     */
    public Return viewOnly(final Parameter _parameter)
    {
        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

        final Return ret = new Return();

        if (mode.equals(TargetMode.VIEW)) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }
}
