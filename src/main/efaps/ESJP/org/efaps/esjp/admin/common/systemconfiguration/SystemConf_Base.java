/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.esjp.admin.common.systemconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.util.EFapsException;

/**
 * Contains some funtionalities related to SystemConfigurations.
 *
 * @author The eFaps Team
 */
@EFapsUUID("aa441e81-fd9f-4462-99c2-2d27bd37f137")
@EFapsApplication("eFaps-Kernel")
public abstract class SystemConf_Base
    extends AbstractCommon
{

    /**
     * Add an ObjectAttribute for the Instance to the SystemConfiguration
     * specified by the given UUID.
     *
     * @param _sysConfUUID UUID of the related SystemConfiguration
     * @param _instance INstance the ObjectAttribute belongs to
     * @param _value value to add
     * @throws EFapsException on error
     */
    public void addObjectAttribute(final UUID _sysConfUUID,
                                   final Instance _instance,
                                   final String _value)
                                       throws EFapsException
    {
        final SystemConfiguration config = SystemConfiguration.get(_sysConfUUID);
        if (config != null) {
            if (config.getObjectAttributeValue(_instance) == null) {
                final Insert insert = new Insert(CIAdminCommon.SystemConfigurationObjectAttribute);
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.AbstractLink, config.getId());
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.Key, _instance.getOid());
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.Value, _value);
                insert.execute();
                config.reload();
            }
        }
    }

    /**
     * Update object attribute.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return new empty Return
     * @throws EFapsException on error
     */
    public Return updateObjectAttribute(final Parameter _parameter)
        throws EFapsException
    {
        final String systemConfigurationUUID = getProperty(_parameter, "SystemConfigurationUUID");
        if (systemConfigurationUUID != null) {
            final SystemConfiguration config = SystemConfiguration.get(UUID.fromString(systemConfigurationUUID));
            if (config != null && config.getObjectAttributeValue(_parameter.getInstance()) != null) {
                final String fieldName = getProperty(_parameter, "FieldName");
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.SystemConfigurationObjectAttribute);
                queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationObjectAttribute.AbstractLink,
                                config.getId());
                queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationObjectAttribute.Key, _parameter
                                .getInstance().getOid());
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(CIAdminCommon.SystemConfigurationObjectAttribute.CompanyLink);
                multi.executeWithoutAccessCheck();

                Instance instance = null;
                // only one found
                if (multi.getInstanceList().size() == 1) {
                    multi.next();
                    instance = multi.getCurrentInstance();
                } else {
                    while (multi.next()) {
                        final Long companyId = multi.<Long>getAttribute(
                                        CIAdminCommon.SystemConfigurationObjectAttribute.CompanyLink);
                        if (companyId == null || (companyId != null && companyId == 0)) {
                            instance = multi.getCurrentInstance();
                        } else if (companyId == Context.getThreadContext().getCompany().getId()) {
                            instance = multi.getCurrentInstance();
                            break;
                        }
                    }
                }
                if (instance != null) {
                    final Update update = new Update(instance);
                    update.add(CIAdminCommon.SystemConfigurationObjectAttribute.Value,
                                    _parameter.getParameterValue(fieldName));
                    update.execute();
                }
                config.reload();
            }
        }
        return new Return();
    }

    /**
     * Drop down field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing list map for dropdown
     * @throws EFapsException on error
     */
    public Return dropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Field field = new Field()
        {

            @Override
            public StringBuilder getDropDownField(final Parameter _parameter,
                                                  final List<DropDownPosition> _values)
                                                      throws EFapsException
            {
                final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
                for (final DropDownPosition dropPos : _values) {
                    if (fieldValue != null && fieldValue.getValue() != null
                                    && fieldValue.getValue().equals(dropPos.getValue())) {
                        dropPos.setSelected(true);
                        break;
                    }
                }
                _values.set(0, new DropDownPosition("0", "--"));
                return super.getDropDownField(_parameter, _values);
            }
        };
        return field.dropDownFieldValue(_parameter);
    }

    /**
     * Key auto complete.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing list map for dropdown
     * @throws EFapsException on error
     */
    public Return autoComplete4Key(final Parameter _parameter)
        throws EFapsException
    {
        final String input = (String) _parameter.get(ParameterValues.OTHERS);
        final List<Map<String, String>> list = new ArrayList<>();
        if (input != null && !input.isEmpty()) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put("eFapsAutoCompleteKEY", input);
            map.put("eFapsAutoCompleteVALUE", input);
            list.add(map);
        }
        final Return ret = new Return();

        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        SelectBuilder sel;
        if (_parameter.getInstance().getType().isCIType(CIAdminCommon.SystemConfiguration)) {
            sel = SelectBuilder.get().attribute(CIAdminCommon.SystemConfiguration.UUID);
        } else {
            sel = SelectBuilder.get().linkto(CIAdminCommon.SystemConfigurationAttribute.AbstractLink)
                            .attribute(CIAdminCommon.SystemConfiguration.UUID);
        }
        print.addSelect(sel);
        print.execute();
        final String uuid = print.getSelect(sel);

        final List<ISysConfAttribute> attrs = SysConfResourceConfig.getResourceConfig().getAttributes(uuid);
        if (attrs != null) {
            Collections.sort(attrs, new Comparator<ISysConfAttribute>()
            {

                @Override
                public int compare(final ISysConfAttribute _arg0,
                                   final ISysConfAttribute _arg1)
                {
                    return _arg0.getKey().compareTo(_arg1.getKey());
                }
            });
            for (final ISysConfAttribute attr : attrs) {
                final Map<String, String> map = new HashMap<String, String>();
                map.put("eFapsAutoCompleteKEY", attr.getKey());
                map.put("eFapsAutoCompleteVALUE", attr.getKey());
                list.add(map);
            }
        }
        ret.put(ReturnValues.VALUES, list);
        return ret;
    }

    /**
     * Update field4 key.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException on error
     */
    public Return updateFields4Key(final Parameter _parameter)
        throws EFapsException
    {
        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        SelectBuilder sel;
        if (_parameter.getInstance().getType().isCIType(CIAdminCommon.SystemConfiguration)) {
            sel = SelectBuilder.get().attribute(CIAdminCommon.SystemConfiguration.UUID);
        } else {
            sel = SelectBuilder.get().linkto(CIAdminCommon.SystemConfigurationAttribute.AbstractLink)
                            .attribute(CIAdminCommon.SystemConfiguration.UUID);
        }
        print.addSelect(sel);
        print.execute();
        final String uuid = print.getSelect(sel);

        final String key = _parameter.getParameterValue("key");

        final ISysConfAttribute attr = SysConfResourceConfig.getResourceConfig().getAttribute(uuid, key);
        CharSequence node;
        if (attr == null) {
            node = "<textarea rows=\"5\" name=\"value\"></textarea>";
        } else {
            node = attr.getHtml(_parameter, null);
        }

        final Return ret = new Return();
        final List<Map<String, Object>> values = new ArrayList<>();
        final Map<String, Object> map = new HashMap<String, Object>();
        values.add(map);
        final StringBuilder js = new StringBuilder()
                .append("require(['dojo/query', 'dojo/dom-construct'], function (query, domConstruct) {")
                .append("var first = true;")
                .append("query('[name=value],[tag=rem]').forEach(function (node) {")
                .append("if (first) {")
                .append("first = false;")
                .append("var newNode = '").append(node).append("';")
                .append("domConstruct.place(newNode, node, 'replace');")
                .append("} else {")
                .append("domConstruct.destroy(node);")
                .append("}")
                .append("});")
                .append("});");

        map.put("eFapsFieldUpdateJS", js.toString());
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * Value field value.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return valueFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        if (TargetMode.CREATE.equals(_parameter.get(ParameterValues.ACCESSMODE))
                        || TargetMode.EDIT.equals(_parameter.get(ParameterValues.ACCESSMODE))) {

            if (_parameter.getInstance() != null && _parameter.getInstance().isValid()
                            && _parameter.getInstance().getType()
                                            .isCIType(CIAdminCommon.SystemConfigurationAttribute)) {
                final PrintQuery print = new PrintQuery(_parameter.getInstance());
                final SelectBuilder sel = SelectBuilder.get()
                                .linkto(CIAdminCommon.SystemConfigurationAttribute.AbstractLink)
                                .attribute(CIAdminCommon.SystemConfiguration.UUID);
                print.addSelect(sel);
                print.addAttribute(CIAdminCommon.SystemConfigurationAttribute.Key);
                print.execute();
                final String uuid = print.getSelect(sel);
                final String key = print.getAttribute(CIAdminCommon.SystemConfigurationAttribute.Key);
                final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
                final ISysConfAttribute attr = SysConfResourceConfig.getResourceConfig().getAttribute(uuid, key);
                if (attr != null) {
                    ret.put(ReturnValues.SNIPLETT, attr.getHtml(_parameter, fieldValue.getObject()).toString());
                }
            }
        }
        return ret;
    }

    /**
     * Sets the master password.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return new empty Return
     * @throws EFapsException on error
     */
    public Return setMasterPassword(final Parameter _parameter)
        throws EFapsException
    {
        final String pwd = _parameter.getParameterValue("masterPassword");
        SystemConfiguration.getPBEConfig().setPassword(pwd);
        return new Return();
    }
}
