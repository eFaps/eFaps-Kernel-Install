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


package org.efaps.esjp.common.uiform;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.datamodel.attributetype.LinkType;
import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.bpm.BPM;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.kie.api.runtime.process.ProcessInstance;


/**
 * This esjp is used from the UI_COMMAND_EXECUTE from the Form on Create.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("a76c48e2-79e4-4083-93b8-f4bc192eaa02")
@EFapsApplication("eFaps-Kernel")
public abstract class Create_Base
    extends AbstractCommon
    implements EventExecution
{
    /**
     * Execute the esjp.
     *
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter Parameter as defined for an esjp
     * @return new empty Return
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        // create the basic object
        final Instance instance = basicInsert(_parameter);
        // connect the basic object to a middle object
        connect(_parameter, instance);
        // check if we have a file-upload field
        fileUpload(_parameter, instance);
        // create classifications
        insertClassification(_parameter, instance);
        // execute processes
        executeProcess(_parameter, instance);

        final Return ret = new Return();
        ret.put(ReturnValues.INSTANCE, instance);
        return ret;
    }

    /**
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance on the insert
     * @throws EFapsException on error
     */
    public void executeProcess(final Parameter _parameter,
                               final Instance _instance)
        throws EFapsException
    {
        if (EFapsSystemConfiguration.get().getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM)) {
            if (getProperty(_parameter, "ProcessID") != null) {
                if ("true".equalsIgnoreCase(getProperty(_parameter, "SaveContextBeforeProcessStart"))) {
                    Context.save();
                }
                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("OID", _instance.getOid());
                add2ProcessMap(_parameter, _instance, params);
                final ProcessInstance processInstance = BPM
                                .startProcess(getProperty(_parameter, "ProcessID"), params);
                if ("true".equalsIgnoreCase(getProperty(_parameter, "RegisterProcess"))) {
                    // BPM_GeneralInstance2ProcessId -- use of UUID because installed from different module
                    final Insert insert = new Insert(UUID.fromString("f6731331-e3a7-4a98-be35-ad1bb8e88497"));
                    insert.add("ProcessId", processInstance.getId());
                    insert.add("GeneralInstanceLink", _instance.getGeneralId());
                    insert.executeWithoutTrigger();
                }
            }
        }
    }

    /**
     * Add additional values to the map passed to the process prior to
     * execution.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @param _instance Insert the values can be added to
     * @param _params Map passed to the Process
     * @throws EFapsException on error
     */
    protected void add2ProcessMap(final Parameter _parameter,
                                  final Instance _instance,
                                  final Map<String, Object> _params)
        throws EFapsException
    {

    }

    /**
     * Method that insert the basic object.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @return Instance on the insert
     * @throws EFapsException on error
     */
    public Instance basicInsert(final Parameter _parameter)
        throws EFapsException
    {
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Instance parent = _parameter.getInstance();
        final List<FieldSet> fieldsets = new ArrayList<FieldSet>();

        Status status = null;
        if (getProperty(_parameter, "StatusGroup") != null && getProperty(_parameter, "Status") != null) {
            status = Status.find(getProperty(_parameter, "StatusGroup"), getProperty(_parameter, "Status"));
        }

        Type createType = command.getTargetCreateType();
        if (createType.isAbstract()) {
            final String typeStr = _parameter.getParameterValue(getProperty(_parameter, "TypeFieldName"));
            if (typeStr != null && !typeStr.isEmpty()) {
                if (isUUID(typeStr)) {
                    createType = Type.get(UUID.fromString(typeStr));
                } else if (Instance.get(typeStr).isValid()) {
                    createType = Type.get(Instance.get(typeStr).getId());
                } else if (NumberUtils.isDigits(typeStr)) {
                    createType = Type.get(Long.parseLong(typeStr));
                } else {
                    createType = Type.get(typeStr);
                }
            } else {
                createType = null;
            }
        }
        final Insert insert = new Insert(createType);
        if (status != null) {
            insert.add(command.getTargetCreateType().getStatusAttribute(), status.getId());
        }

        for (final Field field : command.getTargetForm().getFields()) {
            final String attrName = field.getAttribute();
            if (attrName != null
                          && (field.isEditableDisplay(TargetMode.CREATE) || field.isHiddenDisplay(TargetMode.CREATE))) {
                if (field instanceof FieldSet) {
                    fieldsets.add((FieldSet) field);
                } else {

                    final Attribute attr = command.getTargetCreateType().getAttribute(attrName);
                    // check if not a file-upload field
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        if (_parameter.getParameters().containsKey(field.getName())) {
                            add2Insert(_parameter, insert, attr, field.getName(), 0);
                        }
                    }
                }
            }
        }
        if (command.getTargetConnectAttribute() != null) {
            insert.add(command.getTargetConnectAttribute(), parent);
        }
        add2basicInsert(_parameter, insert);
        insert.execute();

        final Instance instance = insert.getInstance();
        insertFieldSets(_parameter, instance, fieldsets);

        return instance;
    }


    /**
     * Add additional values to the basic insert, prior to execution.
     *
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _insert       Insert the values can be added to
     * @throws EFapsException on error
     */
    protected void add2basicInsert(final Parameter _parameter,
                                   final Insert _insert)
        throws EFapsException
    {

    }


    /**
     * Add to the given update.
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _insert   Insert
     * @param _attr     Attribute
     * @param _fieldName name of the Field
     *  @param _idx          index
     * @throws EFapsException on error
     */
    protected void add2Insert(final Parameter _parameter,
                              final Insert _insert,
                              final Attribute _attr,
                              final String _fieldName,
                              final int _idx)
        throws EFapsException
    {
        if (_attr.hasUoM()) {
            _insert.add(_attr, new Object[] { _parameter.getParameterValues(_fieldName)[_idx],
                            _parameter.getParameterValues(_fieldName + "UoM")[_idx] });
        } else if (_attr.getAttributeType().getDbAttrType() instanceof RateType) {
            final String value = _parameter.getParameterValues(_fieldName)[_idx];
            final boolean inverted = "true".equalsIgnoreCase(_parameter.getParameterValues(_fieldName
                            + RateUI.INVERTEDSUFFIX)[_idx]);
            _insert.add(_attr, new Object[] { inverted ? 1 : value, inverted ? value : 1 });
        } else if (_attr.getAttributeType().getDbAttrType() instanceof LinkType) {
            final String value = _parameter.getParameterValues(_fieldName)[_idx];
            final Instance tmpInst = Instance.get(value);
            _insert.add(_attr, tmpInst.isValid() ? tmpInst : value);
        } else {
            _insert.add(_attr, _parameter.getParameterValues(_fieldName)[_idx]);
        }
    }


    /**
     * Method to create the related fieldsets if parameters are given for them.
     *
     * @param _parameter    Parameter as passed from the efaps API.
     * @param _instance     Instance of the new object
     * @param _fieldsets    fieldsets to insert
     * @throws EFapsException on error
     */
    public void insertFieldSets(final Parameter _parameter,
                                final Instance _instance,
                                final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        for (final FieldSet fieldset : _fieldsets) {
            if (_parameter.getParameters().containsKey(fieldset.getName() + "eFapsRemove")) {
                // to mountain backward compatibility
                insertFieldSetsOld(_parameter, _instance, _fieldsets);
                break;
            } else {
                final String setName = fieldset.getAttribute();
                final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);
                Object[] ids = null;
                if (_parameter.getParameters().containsKey(fieldset.getName() + "_ID")) {
                    final String[] idArray = _parameter.getParameterValues(fieldset.getName() + "_ID");
                    ids = new Object[idArray.length];
                    for (int i = 0; i < idArray.length; i++) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(setName), _instance.getId());
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "_" + attrName;
                            if (_parameter.getParameters().containsKey(fieldName)) {
                                add2Insert(_parameter, insert, child, fieldName, i);
                            }
                        }
                        insert.execute();
                        ids[i] = insert.getId();
                    }
                }
                final QueryBuilder queryBldr = new QueryBuilder(set);
                queryBldr.addWhereAttrEqValue(set.getAttribute(setName), _instance.getId());
                if (ids != null) {
                    queryBldr.addWhereAttrNotEqValue("ID", ids);
                }
                final InstanceQuery query = queryBldr.getQuery();
                for (final Instance toDelInst: query.execute()) {
                    final Delete del = new Delete(toDelInst);
                    del.execute();
                }
            }
        }
    }


    // OLD VERSION! WILL BE REMOVED (only back portability)
    private void insertFieldSetsOld(final Parameter _parameter,
                                    final Instance _instance,
                                    final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);
        // to find out if new values where added for a field set, first it is checked
        // if it exists in this map
        if (others != null) {

            final NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumIntegerDigits(2);

            for (final FieldSet fieldset : _fieldsets) {
                final String[] yCoords = (String[]) others.get(fieldset.getName() + "_eFapsNew");
                if (yCoords != null) {
                    final String setName = fieldset.getAttribute();
                    final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);

                    for (final String yCoord : yCoords) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(setName), ((Long) _instance.getId()).toString());
                        int xCoord = 0;
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "_eFapsNew_"
                                            + nf.format(Integer.parseInt(yCoord)) + nf.format(xCoord);
                            if (_parameter.getParameters().containsKey(fieldName)) {
                                add2Insert(_parameter, insert, child, fieldName, 0);
                            }
                            xCoord++;
                        }
                        insert.execute();
                    }
                }
            }
        }
    }


    /**
     * Method to connect the new instance to parent via middle object.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    public void connect(final Parameter _parameter,
                        final Instance _instance)
        throws EFapsException
    {
        final Instance parent = _parameter.getInstance();

        if (getProperty(_parameter, "ConnectType") != null) {
            final String type = getProperty(_parameter, "ConnectType");
            final String childAttr = getProperty(_parameter, "ConnectChildAttribute");
            final String parentAttr = getProperty(_parameter, "ConnectParentAttribute");

            final Insert insert = new Insert(type);
            insert.add(parentAttr, parent.getId());
            insert.add(childAttr, _instance.getId());
            insert.execute();
        }
    }

    /**
     * Method to upload the file.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    public void fileUpload(final Parameter _parameter,
                           final Instance _instance)
        throws EFapsException
    {
        final Context context = Context.getThreadContext();

        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

        for (final Field field : command.getTargetForm().getFields()) {
            final String attrName = field.getAttribute();
            if (attrName == null && field.isEditableDisplay(TargetMode.CREATE)) {
                final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());
                if (fileItem != null) {
                    final Checkin checkin = new Checkin(_instance);
                    try {
                        checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int) fileItem.getSize());
                    } catch (final IOException e) {
                        throw new EFapsException(this.getClass(), "execute", e, _parameter);
                    }
                }
            }
        }
    }

    /**
     * Method to insert the classifications.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    public void insertClassification(final Parameter _parameter,
                                     final Instance _instance)
        throws EFapsException
    {
        if (_parameter.get(ParameterValues.CLASSIFICATIONS) != null) {

            final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);

            for (final Object object : classifications) {
                final Classification classification = (Classification) object;
                final List<FieldSet> fieldsets = new ArrayList<FieldSet>();

                final Insert relInsert = new Insert(classification.getClassifyRelationType());
                relInsert.add(classification.getRelLinkAttributeName(), ((Long) _instance.getId()).toString());
                relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                relInsert.execute();

                final Form form = Form.getTypeForm(classification);
                final Insert classInsert = new Insert(classification);
                classInsert.add(classification.getLinkAttributeName(), ((Long) _instance.getId()).toString());
                for (final Field field : form.getFields()) {
                    final String attrName = field.getAttribute();
                    if (attrName != null
                          && (field.isEditableDisplay(TargetMode.CREATE) || field.isHiddenDisplay(TargetMode.CREATE))) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final Attribute attr = classification.getAttribute(attrName);
                            if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                                if (_parameter.getParameters().containsKey(field.getName())) {
                                    add2Insert(_parameter, classInsert, attr, field.getName(), 0);
                                }
                            }
                        }
                    }
                }
                classInsert.execute();
                insertFieldSets(_parameter, classInsert.getInstance(), fieldsets);
            }
        }
    }
}
