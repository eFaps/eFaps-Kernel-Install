/*
 * Copyright 2003 - 2019 The eFaps Team
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


package org.efaps.esjp.admin.access;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.user.Company;
import org.efaps.admin.user.Role;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.store.Resource;
import org.efaps.db.store.Store;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.update.AppDependency;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class contains some smaller helper method for checking access to the
 * UserInterface.
 *
 * @author The eFaps Team
 */
@EFapsUUID("8e51edcd-791e-4815-b24b-2b1ded9bd167")
@EFapsApplication("eFaps-Kernel")
public abstract class AccessCheck4UI_Base
    extends AbstractCommon
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessCheck4UI.class);

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
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Status&quot;&gt;Open&lt;/property&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Status01&quot;&gt;Closed&lt;/property&gt;<br>
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
        if (check4Status(_parameter, _parameter.getInstance())) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Check for status.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _instance the instance
     * @return true, if successful
     * @throws EFapsException on error
     */
    protected boolean check4Status(final Parameter _parameter,
                                   final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        final boolean createStatus = containsProperty(_parameter, "Check4CreateStatus");
        if (InstanceUtils.isValid(_instance)) {
            Instance instance = null;
            final String select4Instance = getProperty(_parameter, "Select4Instance");
            if (select4Instance != null) {
                final PrintQuery print = new CachedPrintQuery(_instance, getRequestKey())
                                .setLifespanUnit(TimeUnit.SECONDS).setLifespan(30);
                print.addSelect(select4Instance);
                print.executeWithoutAccessCheck();
                final Object obj = print.getSelect(select4Instance);
                if (obj instanceof Instance) {
                    instance = (Instance) obj;
                } else {
                    AccessCheck4UI_Base.LOG.error("UI_ACCESSCHECK Event for Cmd '{}' is not "
                                    + "returning an instance for 'Select4Instance'", getCmd(_parameter).getName());
                }
            } else {
                instance = _instance;
            }

            final Long statusID;
            if (instance != null && instance.isValid() && instance.getType().isCheckStatus()) {
                final Attribute statusAttr = instance.getType().getStatusAttribute();
                final PrintQuery print = new CachedPrintQuery(instance, getRequestKey())
                                .setLifespanUnit(TimeUnit.SECONDS).setLifespan(30);
                print.addAttribute(statusAttr);
                print.executeWithoutAccessCheck();
                statusID = print.<Long>getAttribute(statusAttr);
            } else {
                statusID = null;
                AccessCheck4UI_Base.LOG.error("UI_ACCESSCHECK Event for Cmd '{}' is executed on"
                                + " type that does not depend on Status.", getCmd(_parameter).getName());
            }

            if (statusID != null) {
                final Parameter para = ParameterUtil.clone(_parameter, ParameterValues.INSTANCE, instance);
                final List<Status> statusList = getStatusListFromProperties(para);
                if (createStatus) {
                    // Commons-Configuration
                    final SystemConfiguration config = SystemConfiguration.get(UUID
                                    .fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3"));
                    if (config != null) {
                        final Properties properties = config.getAttributeValueAsProperties(
                                        "org.efaps.commons.DocumentStatus4Create", true);
                        final String key = properties.getProperty(instance.getType().getName() + ".Status");
                        if (key != null) {
                            final Status status = Status.find(
                                            instance.getType().getStatusAttribute().getLink().getUUID(), key);
                            if (status != null) {
                                statusList.clear();
                                statusList.add(status);
                            }
                        }
                    }
                }
                for (final Status status : statusList) {
                    if (status != null && statusID.equals(status.getId())) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return command that called the function
     * @throws EFapsException on error
     */
    protected AbstractCommand getCmd(final Parameter _parameter)
        throws EFapsException
    {
        AbstractCommand ret = null;
        final Object obj = _parameter.get(ParameterValues.UIOBJECT);
        if (obj instanceof AbstractCommand) {
            ret = (AbstractCommand) obj;
        } else {
            final Object obj2 = _parameter.get(ParameterValues.CALL_CMD);
            if (obj2 instanceof AbstractCommand) {
                ret = (AbstractCommand) obj2;
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

            if (!inverse && access || inverse && !access) {
                ret.put(ReturnValues.TRUE, true);
            }
        } else {
            AccessCheck4UI_Base.LOG.error("Cannot evaluate Access for UI due to missing Instance for {}",
                            _parameter.get(ParameterValues.UIOBJECT));
        }
        return ret;
    }

    /**
     * Method is used to control access based on a Property of esjp.
     * Used for e.g. hiding a field in a form depending on a property of
     * the calling command.<br>
     * Example with automatic property ( "{FIELDNAME}_UIAccessCheck"):<br><code>
     * &nbsp;&nbsp;&lt;trigger program=&quot;org.efaps.esjp.admin.access.AccessCheck4UI&quot;
     *           method=&quot;propertyCheck&quot;
     *           name=&quot;field.UI_ACCESSCHECK&quot;
     *           event=&quot;UI_ACCESSCHECK&quot; &gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;CheckCallingCommand&quot;&gt;true&lt;/property&gt;<br>
     * &nbsp;&nbsp;&lt;/trigger&gt;
     * </code><br>
     * Example with explicit property name:<br><code>
     * &nbsp;&nbsp;&lt;trigger program=&quot;org.efaps.esjp.admin.access.AccessCheck4UI&quot;
     *           method=&quot;propertyCheck&quot;
     *           name=&quot;field.UI_ACCESSCHECK&quot;
     *           event=&quot;UI_ACCESSCHECK&quot; &gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;CheckCallingCommand&quot;&gt;true&lt;/property&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=&quot;Property&quot;&gt;PROPERTYNAME&lt;/property&gt;<br>
     * &nbsp;&nbsp;&lt;/trigger&gt;
     * </code>
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return propertyCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Field field = (Field) _parameter.get(ParameterValues.UIOBJECT);
        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
        final boolean access;
        if ("true".equalsIgnoreCase(getProperty(_parameter, "CheckCallingCommand"))) {
            final AbstractCommand cmd = (AbstractCommand) _parameter.get(ParameterValues.CALL_CMD);
            if (cmd != null) {
                access = "true".equalsIgnoreCase(cmd.getProperty(getProperty(_parameter, "Property")))
                                || "true".equalsIgnoreCase(cmd.getProperty(field.getName() + "_UIAccessCheck"));
            } else {
                access = false;
            }
        } else {
            access = "true".equalsIgnoreCase(getProperty(_parameter, "UIAccessCheck"));
            AccessCheck4UI_Base.LOG.error("Could not get Calling Command for: {}", field);
        }
        if (!inverse && access || inverse && !access) {
            ret.put(ReturnValues.TRUE, true);
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
        final String sysConfstr = getProperty(_parameter, "SystemConfig");
        final SystemConfiguration config;
        if (isUUID(sysConfstr)) {
            config = SystemConfiguration.get(UUID.fromString(sysConfstr));
        } else {
            config = SystemConfiguration.get(sysConfstr);
        }
        if (config != null) {
            boolean access = false;
            final String defaultValue = getProperty(_parameter, "Default");
            for (final String attribute : analyseProperty(_parameter, "Attribute").values()) {
                if (defaultValue == null) {
                    if (attribute.startsWith("!")) {
                        access = !config.getAttributeValueAsBoolean(attribute.substring(1));
                    } else {
                        access = config.getAttributeValueAsBoolean(attribute);
                    }
                } else {
                    String attrValue = config.getAttributeValue(attribute.startsWith("!")
                                    ? attribute.substring(1)
                                    : attribute);
                    if (attrValue == null) {
                        attrValue = defaultValue;
                    }
                    access = attribute.startsWith("!")
                                    ? !Boolean.parseBoolean(attrValue)
                                    : Boolean.parseBoolean(attrValue);
                }
                if (access) {
                    break;
                }
            }
            for (final String attribute : analyseProperty(_parameter, "AttributeExists").values()) {
                if (attribute.startsWith("!")) {
                    access = config.getAttributeValue(attribute.substring(1)) == null;
                } else {
                    access = config.getAttributeValue(attribute) != null;
                }
                if (access) {
                    break;
                }
            }

            final Map<Integer, String> propAttrs = analyseProperty(_parameter, "PropertiesAttribute");
            final Map<Integer, String> propKeys = analyseProperty(_parameter, "PropertiesKey");
            final Map<Integer, String> propValues = analyseProperty(_parameter, "PropertiesValue");
            for (final Entry<Integer, String> entry : propAttrs.entrySet()) {
                final Properties props = config.getAttributeValueAsProperties(entry.getValue(), true);
                final String propKey = propKeys.get(entry.getKey());
                final String propKeyValue = props.getProperty(propKey.startsWith("!") ? propKey.substring(1) : propKey);
                if (propValues.containsKey(entry.getKey())) {
                    access = propValues.get(entry.getKey()).equals(propKeyValue);
                } else {
                    access = propKey.startsWith("!") ? !Boolean.parseBoolean(propKeyValue)
                                    : Boolean.parseBoolean(propKeyValue);
                }
                if (access) {
                    break;
                }
            }

            final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
            if (!inverse && access || inverse && !access) {
                ret.put(ReturnValues.TRUE, true);
            }
        }
        return ret;
    }

    /**
     * Method is used to control access based on a ObjectSystemConfiguration.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return configObjectCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();
        if (containsProperty(_parameter, "SystemConfig")) {
            final String sysConfstr = getProperty(_parameter, "SystemConfig");
            final SystemConfiguration config;
            if (isUUID(sysConfstr)) {
                config = SystemConfiguration.get(UUID.fromString(sysConfstr));
            } else {
                config = SystemConfiguration.get(sysConfstr);
            }
            if (config != null && instance != null && instance.isValid()) {
                final Properties objProps = config.getObjectAttributeValueAsProperties(instance);
                final Boolean access;
                if ("true".equals(getProperty(_parameter, "CheckOnContains"))) {
                    access = objProps.containsKey(getProperty(_parameter, "Key"));
                } else {
                    access = Boolean.valueOf((String) objProps.get(getProperty(_parameter, "Key")));
                }
                final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
                if (!inverse && access || inverse && !access) {
                    ret.put(ReturnValues.TRUE, true);
                }
            }
        } else {
            AccessCheck4UI_Base.LOG.error("Wrong configuration");
        }
        return ret;
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
     * Method is used to control access based on a list of given Companies.
     *
     * @param _parameter Parameter as passed from eFaps to an esjp
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return companyCheck(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Map<Integer, String> companies = analyseProperty(_parameter, "Companies");
        for (final String companyStr : companies.values()) {
            final Company company;
            if (isUUID(companyStr)) {
                company = Company.get(UUID.fromString(companyStr));
            } else {
                company = Company.get(companyStr);
            }
            if (company != null) {
                final boolean assigned = company.equals(Context.getThreadContext().getCompany());
                if (assigned) {
                    ret.put(ReturnValues.TRUE, true);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Method is used to control access based on a checks to commands.
     *
     * @param _parameter Parameter as passed from  the eFaps API.
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return check4Cmd(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        if (!TargetMode.VIEW.equals(_parameter.get(ParameterValues.ACCESSMODE))) {
            final Command cmd = (Command) _parameter.get(ParameterValues.CALL_CMD);
            final Map<Integer, String> cmdUUIDs = analyseProperty(_parameter, "CmdUUID");
            final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));

            if (cmdUUIDs.isEmpty() && !inverse) {
                ret.put(ReturnValues.TRUE, true);
            } else {
                Boolean access = null;
                for (final Entry<Integer, String> cmdUUID : cmdUUIDs.entrySet()) {
                    if (isUUID(cmdUUID.getValue())) {
                        final UUID uuid = UUID.fromString(cmdUUID.getValue());
                        if (cmd.getUUID().equals(uuid)) {
                            access = true;
                            break;
                        }
                    }
                }
                if (BooleanUtils.isFalse(access) && inverse || BooleanUtils.isTrue(access) && !inverse) {
                    ret.put(ReturnValues.TRUE, true);
                }
            }
        } else {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Method is used to control access based on the possibility to perform
     * a checkout. Meaning the it is checked if actually a checkedin file
     * exists.
     *
     * @param _parameter Parameter as passed from  the eFaps API.
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return check4Checkout(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();
        Boolean access = null;
        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
        if (instance != null && instance.isValid() && instance.getType().hasStore()) {
            final Resource resource = Store.get(instance.getType().getStoreId()).getResource(instance);
            access = resource.exists();
        }
        if (BooleanUtils.isFalse(access) && inverse || BooleanUtils.isTrue(access) && !inverse) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Method is used to control access based on Applications installed.
     *
     * @param _parameter Parameter as passed from  the eFaps API.
     * @return Return with True if VIEW, else false
     * @throws EFapsException on error
     */
    public Return check4Application(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        Boolean access = null;
        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
        for (final String application : analyseProperty(_parameter, "Application").values()) {
            try {
                access =  AppDependency.getAppDependency(application).isMet();
            } catch (final InstallationException e) {
                throw new EFapsException("", e);
            }
            if (access) {
                break;
            }
        }
        if (BooleanUtils.isFalse(access) && inverse || BooleanUtils.isTrue(access) && !inverse) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Check for selected on status.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return check4SelectedOnStatus(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final boolean inverse = "true".equalsIgnoreCase(getProperty(_parameter, "Inverse"));
        Boolean access = null;
        for (final Instance instance : getSelectedInstances(_parameter)) {
            if (access == null) {
                access = check4Status(_parameter, instance);
            } else {
                access = access && check4Status(_parameter, instance);
            }
        }
        if (BooleanUtils.isFalse(access) && inverse || BooleanUtils.isTrue(access) && !inverse) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }
}
