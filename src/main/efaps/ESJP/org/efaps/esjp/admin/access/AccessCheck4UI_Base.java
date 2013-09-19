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

import java.util.Collection;
import java.util.Map;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class contains some smaller helper method for checking access to the
 * UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("8e51edcd-791e-4815-b24b-2b1ded9bd167")
@EFapsRevision("$Rev$")
public abstract class AccessCheck4UI_Base
    extends AbstractCommon
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AccessCheck4UI.class);

    /**
     * Method is used for access checks on commands depending on the status of
     * instance called by the command.
     * The allowed status must be set as a property in the trigger. It can
     * contain a comma separated list of status. (<code>Open,Closed</code>).<br>
     * <code>
     * &lt;trigger program=&quot;org.efaps.esjp.admin.access.AccessCheck4UI&quot;<br>
     * &nbsp;&nbsp;method=&quot;check4Status&quot;<br>
     * &nbsp;&nbsp;name=&quot;Sales_ReservationTree_Menu_Action_SetClosed.UI_ACCESSCHECK&quot;<br>
     * &nbsp;&nbsp;event=&quot;UI_ACCESSCHECK&quot;&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Stati&quot;&gt;Open&lt;/property&gt;<br>
     * &lt;/trigger&gt;<br>
     * </code>
     * Additionally an explicit StatusGroup Property can be set, to define the StatusGroup. If not set the
     * StatusGroup will be evaluated from the given instance.<br/>
     * <code>
     * &lt;property name=&quot;StatusGroup&quot;&gt;Sales_QuotationStatus&lt;/property&gt;<br>
     * </code>
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @throws EFapsException never
     * @return Return with true if access is granted
     */
    public Return check4Status(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String statiStr = (String) props.get("Stati");
        final Instance instance = _parameter.getInstance();
        if (instance != null && instance.getType().isCheckStatus() && statiStr != null && statiStr.length() > 0) {
            final Attribute statusAttr = instance.getType().getStatusAttribute();
            final PrintQuery print = new PrintQuery(instance);
            print.addAttribute(statusAttr);
            if (print.execute()) {
                final Long statusID = print.<Long> getAttribute(statusAttr);
                final String[] stati = statiStr.split(",");
                for (final String status : stati) {
                    final String statusGrpStr = (String) props.get("StatusGroup");
                    Status stat;
                    if (statusGrpStr != null && !statusGrpStr.isEmpty()) {
                        stat = Status.find(statusGrpStr, status.trim());
                    } else {
                        stat = Status.find(statusAttr.getLink().getUUID(), status.trim());
                    }
                    if (stat != null && statusID.equals(stat.getId())) {
                        ret.put(ReturnValues.TRUE, true);
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Method is used for access checks on commands depending on the AccessTypes
     * granted to the related Instance.<br/>
     * The allowed AccessTypes must be set as a properties in the trigger and are
     * the values of enumeration <code>org.efaps.admin.access.AccessTypeEnums</code><br>
     * Optional the property <code>Inverse</code> can be set to inverse the result.
     * Optional the property <code>Conjunction</code> can be set to define if the
     * AccessSets are used with "AND" or with "OR" the result. Default is "OR".<br/>
     * <code>
     * &lt;trigger program=&quot;org.efaps.esjp.admin.access.AccessCheck4UI&quot;<br>
     * &nbsp;&nbsp;method=&quot;check4InstanceAccess&quot;<br>
     * &nbsp;&nbsp;name=&quot;Archives_ArchiveRootTree_Menu_Action_Edit.UI_ACCESSCHECK&quot;<br>
     * &nbsp;&nbsp;event=&quot;UI_ACCESSCHECK&quot;&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;AccessType&quot;&gt;MODIFY&lt;/property&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;AccessType01&quot;&gt;CHECKIN&lt;/property&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Inverse&quot;&gt;true&lt;/property&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Conjunction&quot;&gt;true&lt;/property&gt;<br>
     * &lt;/trigger&gt;<br>
     * </code>
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @throws EFapsException never
     * @return Return with true if access is granted
     */
    public Return check4InstanceAccess(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();
        if (instance.isValid()) {
            final Collection<String> accessTypes = analyseProperty(_parameter, "AccessType").values();
            final boolean conjunction = "true".equalsIgnoreCase(getProperty(_parameter, "Conjunction"));
            final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
            Boolean access = null;
            for (final String accessType : accessTypes) {
                final AccessTypeEnums accessTypeEnum = AccessTypeEnums.valueOf(accessType);
                final boolean accessTmp = instance.getType().hasAccess(instance, accessTypeEnum.getAccessType());
                if (access == null) {
                    access = accessTmp;
                } else if (conjunction) {
                    access = access && accessTmp;
                } else {
                    access = access || accessTmp;
                }
            }

            if ((!inverse && access) || (inverse && !access)) {
                ret.put(ReturnValues.TRUE, true);
            }
        } else {
            AccessCheck4UI_Base.LOG.error("Cannot evaluate Access for UI due to missing Instance for {}",
                            _parameter.get(ParameterValues.UIOBJECT));
        }
        return ret;
    }
}