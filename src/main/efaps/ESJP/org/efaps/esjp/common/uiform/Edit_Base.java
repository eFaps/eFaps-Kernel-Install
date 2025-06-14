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
package org.efaps.esjp.common.uiform;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.admin.datamodel.ui.BitEnumUI;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.eql.EQL;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3a99a177-8b5c-4dd4-b876-dcba28ea3138")
@EFapsApplication("eFaps-Kernel")
public abstract class Edit_Base
    extends AbstractCommon
    implements EventExecution
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Edit.class);

    /**
     * String used as a temp key.
     */
    protected static final String FAKEROW = "NoMoreRowsExisting";

    /**
     * @param _parameter Parameter as provided by eFaps for a esjp
     * @throws EFapsException on error
     * @return empty Return
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

        final Context context = Context.getThreadContext();
        // update the Values for the general form
        final String classifcationName = updateMainElements(_parameter, command.getTargetForm(), instance);

        // ************************************
        // check if we have a fileupload field
        if (context.getFileParameters().size() > 0) {
            for (final Field field : command.getTargetForm().getFields()) {
                final String attrName = field.getAttribute();
                if (attrName == null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());
                    if (fileItem != null) {
                        final Checkin checkin = new Checkin(instance);
                        try {
                            checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int) fileItem.getSize());
                        } catch (final IOException e) {
                            throw new EFapsException(this.getClass(), "execute", e, _parameter);
                        }
                    }
                }
            }
        }
        if (classifcationName != null) {
            updateClassifcation(_parameter, instance, classifcationName);
        }
        updateConnection2Object(_parameter, instance);
        return new Return();
    }

    /**
     * Method updates the main elements from the form.
     *
     * @param _parameter _parameter Parameter as provided by eFaps for a esjp
     * @param _form from used for the update
     * @param _instance instance that must be updated
     * @return the name of a classification if found in the form, else null
     * @throws EFapsException on error
     */
    public String updateMainElements(final Parameter _parameter,
                                     final Form _form,
                                     final Instance _instance)
        throws EFapsException
    {
        return updateMainElements(_parameter, _form, _instance, new HashMap<>(), null);
    }

    /**
     * Method updates the main elements from the form.
     *
     * @param _parameter _parameter Parameter as provided by eFaps for a esjp
     * @param _form from used for the update
     * @param _instance instance that must be updated
     * @param _valueMap map of values
     * @param _fieldTables list of tables
     * @return the name of a classification if found in the form, else null
     * @throws EFapsException on error
     */
    public String updateMainElements(final Parameter _parameter,
                                     final Form _form,
                                     final Instance _instance,
                                     final Map<String, Object> _valueMap,
                                     final List<FieldTable> _fieldTables)
        throws EFapsException
    {
        String ret = null;
        final List<FieldSet> fieldsets = new ArrayList<>();
        final List<FieldTable> fieldTables;
        if (_fieldTables == null) {
            fieldTables = new ArrayList<>();
        } else {
            fieldTables = _fieldTables;
        }

        final List<Field> fields = new ArrayList<>();

        final PrintQuery print = new PrintQuery(_instance);
        for (final Field field : _form.getFields()) {
            if (field instanceof FieldSet) {
                if (field.hasAccess((TargetMode) _parameter.get(ParameterValues.ACCESSMODE), _instance)) {
                    fieldsets.add((FieldSet) field);
                }
            } else if (field instanceof FieldTable && field.isEditableDisplay(TargetMode.EDIT)) {
                fieldTables.add((FieldTable) field);
            } else if (field instanceof final FieldClassification fieldClass) {
                final var rootClasses = fieldClass.evalRootClassifications();
                if (rootClasses.isEmpty()) {
                    ret = fieldClass.getClassificationName();
                } else {
                    ret = fieldClass.evalRootClassifications().stream().map(Classification::getName)
                                    .collect(Collectors.joining(";"));
                }
            } else {
                final String attrName = field.getAttribute();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = _instance.getType().getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null && !AbstractFileType.class.isAssignableFrom(
                                    attr.getAttributeType().getClassRepr())) {
                        print.addAttribute(attrName);
                        fields.add(field);
                    }
                }
            }
        }
        Context.getThreadContext();
        if (print.execute()) {
            final Update update = new Update(_instance);
            for (final Field field : fields) {
                final String attrName = field.getAttribute();
                final Attribute attr = _instance.getType().getAttribute(attrName);
                if (attr != null && (_parameter.getParameters().containsKey(field.getName())
                                || attr.getAttributeType().getUIProvider() instanceof BitEnumUI)) {
                    final Object oldValue = print.getAttribute(attrName);
                    final String[] newValue = _parameter.getParameterValues(field.getName());
                    if (hasChanged(oldValue, newValue)) {
                        _valueMap.put(attrName, add2Update(_parameter, update, attr, field.getName()));
                    }
                }
            }
            add2MainUpdate(_parameter, update);
            update.execute();
        }
        updateFieldSets(_parameter, _instance, fieldsets);
        if (_fieldTables == null) {
            updateFieldTable(_parameter, _instance, fieldTables);
        }
        return ret;
    }

    protected boolean hasChanged(final Object oldValue,
                                 final String[] newValues)
    {
        if (newValues == null && oldValue != null) {
            return true;
        }
        if (newValues != null && newValues.length == 1) {
            final var newValue = newValues[0];
            return !String.valueOf(oldValue).equals(newValue);
        }
        return true;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _update Update
     * @throws EFapsException on error
     */
    protected void add2MainUpdate(final Parameter _parameter,
                                  final Update _update)
        throws EFapsException
    {
        // to be used form implementations
    }

    /**
     * Add to the given update.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _update Update
     * @param _attr Attribute
     * @param _fieldName name of the Field
     * @throws EFapsException on error
     * @return the object addeded
     */
    protected Object add2Update(final Parameter _parameter,
                                final Update _update,
                                final Attribute _attr,
                                final String _fieldName)
        throws EFapsException
    {
        final Context context = Context.getThreadContext();
        Object ret = null;
        if (_attr.hasUoM()) {
            final Object[] tmp = new Object[] { _parameter.getParameterValue(_fieldName),
                            _parameter.getParameterValue(_fieldName + "UoM") };
            _update.add(_attr, tmp);
            ret = tmp;
        } else if (_attr.getAttributeType().getDbAttrType() instanceof RateType) {
            final String value = _parameter.getParameterValue(_fieldName);
            final boolean inverted = "true".equalsIgnoreCase(context.getParameter(_fieldName + RateUI.INVERTEDSUFFIX));
            final Object[] tmp = new Object[] { inverted ? 1 : value, inverted ? value : 1 };
            _update.add(_attr, tmp);
            ret = tmp;
        } else if (_attr.getAttributeType().getUIProvider() instanceof BitEnumUI) {
            final String[] tmp = _parameter.getParameterValues(_fieldName);
            if (tmp == null) {
                _update.add(_attr, (Object[]) null);
            } else {
                _update.add(_attr, Arrays.copyOf(tmp, tmp.length, Object[].class));
            }
            ret = tmp;
        } else {
            ret = _parameter.getParameterValue(_fieldName);
            _update.add(_attr, ret);
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _update Update to add to
     * @param _attr current Attribute
     * @param _fieldName name of the field
     * @param _idx index
     * @throws EFapsException obn error
     */
    protected void add2Update(final Parameter _parameter,
                              final Update _update,
                              final Attribute _attr,
                              final String _fieldName,
                              final int _idx)
        throws EFapsException
    {
        if (_attr.hasUoM()) {
            _update.add(_attr, new Object[] { _parameter.getParameterValues(_fieldName)[_idx],
                            _parameter.getParameterValues(_fieldName + "UoM")[_idx] });
        } else if (_attr.getAttributeType().getDbAttrType() instanceof RateType) {
            final String value = _parameter.getParameterValues(_fieldName)[_idx];
            final boolean inverted = "true".equalsIgnoreCase(_parameter.getParameterValues(_fieldName
                            + RateUI.INVERTEDSUFFIX)[_idx]);
            _update.add(_attr, new Object[] { inverted ? 1 : value, inverted ? value : 1 });
        } else {
            _update.add(_attr, _parameter.getParameterValues(_fieldName)[_idx]);
        }
    }

    // OLD VERSION! WILL BE REMOVED
    private void updateFieldSetsOld(final Parameter _parameter,
                                    final Instance _instance,
                                    final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        for (final FieldSet fieldset : _fieldsets) {
            final String setName = fieldset.getAttribute();
            final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);

            // first already existing values must be updated, if they were
            // altered
            boolean updateExisting = true;
            int yCoord = 0;
            while (updateExisting) {
                // check in the context if already existing values might have
                // been altered, by
                // using the hidden field that is added when existing values for
                // a fieldset are shown
                final String idfield = "hiddenId_" + fieldset.getName() + "_" + nf.format(yCoord);
                if (_parameter.getParameters().containsKey(idfield)) {
                    final String id = _parameter.getParameterValue(idfield);
                    // check the values in the database
                    final PrintQuery printQuery = new PrintQuery(set, id);
                    for (final Attribute attr : set.getAttributes().values()) {
                        printQuery.addAttribute(attr);
                    }
                    printQuery.execute();

                    final Update setupdate = new Update(set, id);
                    int xCoord = 0;
                    boolean update = false;
                    for (final String attrName : fieldset.getOrder()) {
                        final Attribute child = set.getAttribute(attrName);
                        final String fieldName = fieldset.getName() + "_" + nf.format(yCoord) + nf.format(xCoord);
                        if (_parameter.getParameters().containsKey(fieldName)) {
                            final Object object = printQuery.getAttribute(attrName);
                            final String oldValue = object != null ? object.toString() : null;
                            final String newValue = _parameter.getParameterValue(fieldName);
                            if (!newValue.equals(oldValue)) {
                                add2Update(_parameter, setupdate, child, fieldName);
                                update = true;
                            }
                        }
                        xCoord++;
                    }
                    if (update) {
                        setupdate.execute();
                    }
                } else {
                    updateExisting = false;
                }
                yCoord++;
            }

            // add new values
            final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);
            if (others != null) {
                // add new Values
                final String[] yCoords = (String[]) others.get(fieldset.getName() + "_eFapsNew");
                if (yCoords != null) {

                    for (final String ayCoord : yCoords) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(setName), ((Long) _instance.getId()).toString());
                        int xCoord = 0;
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "_eFapsNew_"
                                            + nf.format(Integer.parseInt(ayCoord)) + nf.format(xCoord);
                            if (_parameter.getParameters().containsKey(fieldName)) {
                                add2Update(_parameter, insert, child, fieldName);
                            }
                            xCoord++;
                        }
                        insert.execute();
                    }
                }

                // remove deleted Values
                final String[] removeOnes = (String[]) others.get(fieldset.getName() + "eFapsRemove");
                if (removeOnes != null) {
                    for (final String removeOne : removeOnes) {
                        final Delete delete = new Delete(set, removeOne);
                        delete.execute();
                    }
                }
            }
        }
    }

    /**
     * Method to update the related fieldsets if parameters are given for them.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @param _fieldsets fieldsets to insert
     * @throws EFapsException on error
     */
    public void updateFieldSets(final Parameter _parameter,
                                final Instance _instance,
                                final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        // check if a call from the rest API
        if (_parameter.getParameters().containsKey("eFaps-REST")) {
            updateFieldSetsByPayload(_parameter, _instance, _fieldsets);
        } else {

            @SuppressWarnings("unchecked") final Map<String, String> idmap = (Map<String, String>) _parameter
                            .get(ParameterValues.OIDMAP4UI);
            for (final FieldSet fieldset : _fieldsets) {
                if (_parameter.getParameters().containsKey(fieldset.getName() + "eFapsRemove")) {
                    // to mantain backward compatibility
                    updateFieldSetsOld(_parameter, _instance, _fieldsets);
                    break;
                } else {
                    final String setName = fieldset.getAttribute();
                    final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);
                    Object[] ids = null;
                    if (_parameter.getParameters().containsKey(fieldset.getName() + "_ID")) {
                        final String[] idArray = _parameter.getParameterValues(fieldset.getName() + "_ID");
                        ids = new Object[idArray.length];
                        for (int i = 0; i < idArray.length; i++) {
                            final String oid = idmap.get(idArray[i]);
                            // in case of new
                            final Update update;
                            if (oid == null) {
                                update = new Insert(set);
                                update.add(set.getAttribute(setName), _instance.getId());
                            } else {
                                update = new Update(oid);
                            }
                            for (final String attrName : fieldset.getOrder()) {
                                final Attribute child = set.getAttribute(attrName);
                                final String fieldName = fieldset.getName() + "_" + attrName;
                                if (_parameter.getParameters().containsKey(fieldName)) {
                                    add2Update(_parameter, update, child, fieldName, i);
                                }
                            }
                            update.execute();
                            ids[i] = update.getId();
                        }
                    }
                    final QueryBuilder queryBldr = new QueryBuilder(set);
                    queryBldr.addWhereAttrEqValue(set.getAttribute(setName), _instance.getId());
                    if (ids != null) {
                        queryBldr.addWhereAttrNotEqValue("ID", ids);
                    }
                    final InstanceQuery query = queryBldr.getQuery();
                    for (final Instance toDelInst : query.execute()) {
                        final Delete del = new Delete(toDelInst);
                        del.execute();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void updateFieldSetsByPayload(final Parameter parameter,
                                         final Instance instance,
                                         final List<FieldSet> fieldsets)
        throws EFapsException
    {

        final var valueMap = (Map<String, Object>) parameter.get(ParameterValues.PAYLOAD);
        for (final var fieldSet : fieldsets) {
            final String setName = fieldSet.getAttribute();
            final AttributeSet attributeSet = AttributeSet.find(instance.getType().getName(), setName);
            final var setEval = EQL.builder().print()
                            .query(attributeSet.getName())
                            .where().attribute(attributeSet.getAttributeName()).eq(instance)
                            .select().instance()
                            .evaluate();
            if (valueMap.containsKey(fieldSet.getName())) {
                final var entryMap = (Map<String, Object>) valueMap.get(fieldSet.getName());
                while (setEval.next()) {
                    final var setInst = setEval.inst();
                    LOG.debug("{}", setInst);
                    if (entryMap.containsKey(String.valueOf(setInst.getId()))) {
                        final var setAttrMap = (Map<String, Object>) entryMap.get(String.valueOf(setInst.getId()));
                        final var updateBldr = EQL.builder().update(setInst);
                        for (final var setAttr : attributeSet.getSetAttributes()) {
                            if (setAttrMap.containsKey(setAttr)) {
                                updateBldr.set(setAttr, setAttrMap.get(setAttr) == null ? null
                                                : String.valueOf(setAttrMap.get(setAttr)));
                            }
                        }
                        updateBldr.execute();
                    } else {
                        EQL.builder().delete(setInst).stmt().execute();
                    }
                }
                for (final var entry : entryMap.entrySet()) {
                    if (!StringUtils.isNumeric(entry.getKey())) {
                        final var setAttrMap = (Map<String, Object>) entry.getValue();
                        final var insertBldr = EQL.builder().insert(attributeSet)
                                        .set(attributeSet.getAttributeName(), instance.getOid());
                        for (final var setAttr : attributeSet.getSetAttributes()) {
                            if (setAttrMap.containsKey(setAttr)) {
                                insertBldr.set(setAttr, setAttrMap.get(setAttr) == null ? null
                                                : String.valueOf(setAttrMap.get(setAttr)));
                            }
                        }
                        insertBldr.execute();
                    }
                }

            } else {
                while (setEval.next()) {
                    final var setInst = setEval.inst();
                    LOG.debug("{}", setInst);
                }
            }
        }
    }

    /**
     * Method to update the classifications.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @param _classifcationName name of the classificationto be updated
     * @throws EFapsException on error
     */
    public void updateClassifcation(final Parameter _parameter,
                                    final Instance _instance,
                                    final String _classifcationName)
        throws EFapsException
    {

        final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);
        final Classification classType = (Classification) Type.get(_classifcationName.split(";")[0]);

        final Map<Classification, Map<String, Object>> clas2values = new HashMap<>();
        // get the already existing classifications
        final QueryBuilder relQueryBldr = new QueryBuilder(classType.getClassifyRelationType());
        relQueryBldr.addWhereAttrEqValue(classType.getRelLinkAttributeName(), _instance.getId());
        final MultiPrintQuery relMulti = relQueryBldr.getPrint();
        relMulti.addAttribute(classType.getRelTypeAttributeName());
        relMulti.execute();

        while (relMulti.next()) {
            final Long typeid = relMulti.<Long>getAttribute(classType.getRelTypeAttributeName());
            final Classification subClassType = (Classification) Type.get(typeid);
            final Map<String, Object> values = new HashMap<>();
            clas2values.put(subClassType, values);

            final QueryBuilder subQueryBldr = new QueryBuilder(subClassType);
            subQueryBldr.addWhereAttrEqValue(subClassType.getLinkAttributeName(), _instance.getId());
            final MultiPrintQuery subMulti = subQueryBldr.getPrint();

            final List<Field> fields = new ArrayList<>();
            final Form form = Form.getTypeForm(subClassType);
            for (final Field field : form.getFields()) {
                final String attrName = field.getAttribute();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = subClassType.getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null && !AbstractFileType.class.isAssignableFrom(
                                    attr.getAttributeType().getClassRepr())) {
                        subMulti.addAttribute(attrName);
                        fields.add(field);
                    }
                }
            }
            if (subMulti.execute()) {
                if (subMulti.next()) {
                    for (final Field field : fields) {
                        final String attrName = field.getAttribute();
                        values.put(field.getName(), subMulti.getAttribute(attrName));
                    }
                    values.put("OID", subMulti.getCurrentInstance().getOid());
                }
            }
            values.put("relOID", relMulti.getCurrentInstance().getOid());
        }

        if (classifications != null) {
            for (final Object object : classifications) {
                final Classification classification = (Classification) object;
                // if the classification does not exist yet the relation must be
                // created, and the new instance of the classification inserted
                final Form form = Form.getTypeForm(classification);
                if (!clas2values.containsKey(classification)) {
                    final Insert relInsert = new Insert(classification.getClassifyRelationType());
                    relInsert.add(classification.getRelLinkAttributeName(), ((Long) _instance.getId()).toString());
                    relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                    relInsert.execute();

                    final Insert classInsert = new Insert(classification);
                    classInsert.add(classification.getLinkAttributeName(), ((Long) _instance.getId()).toString());
                    final List<FieldSet> fieldsets = new ArrayList<>();
                    new HashSet<>();
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final String attrName = field.getAttribute();
                            if (attrName != null && (field.isEditableDisplay(TargetMode.EDIT)
                                            || field.isEditableDisplay(TargetMode.CREATE))) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null && _parameter.getParameterValue(field.getName()) != null
                                                && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                                .getClassRepr())) {
                                    add2Update(_parameter, classInsert, attr, field.getName());
                                }
                            }
                        }
                    }
                    classInsert.execute();
                    updateFieldSets(_parameter, classInsert.getInstance(), fieldsets);
                } else {
                    final Map<String, Object> values = clas2values.get(classification);
                    final List<FieldSet> fieldsets = new ArrayList<>();
                    final Update update = new Update((String) values.get("OID"));
                    boolean execUpdate = false;
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            if (field.hasAccess((TargetMode) _parameter.get(ParameterValues.ACCESSMODE), _instance)) {
                                fieldsets.add((FieldSet) field);
                            }
                        } else {
                            final String attrName = field.getAttribute();
                            if (attrName != null && (field.isEditableDisplay(TargetMode.EDIT)
                                            || field.isEditableDisplay(TargetMode.CREATE))) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null && _parameter.getParameterValue(field.getName()) != null
                                                && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                                .getClassRepr())) {
                                    if (_parameter.getParameters().containsKey(field.getName())
                                                    || attr.getAttributeType().getUIProvider() instanceof BitEnumUI) {
                                        final String newValue = _parameter.getParameterValue(field.getName());
                                        final Object value = values.get(field.getName());
                                        final String oldValue = value != null ? value.toString() : null;
                                        if (newValue == null && oldValue != null
                                                        || newValue != null && !newValue.equals(oldValue)) {
                                            execUpdate = true;
                                            add2Update(_parameter, update, attr, field.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (execUpdate) {
                        update.execute();
                    }
                    updateFieldSets(_parameter, update.getInstance(), fieldsets);
                }
            }
        }
        // remove the classifications that are not any more wanted
        for (final Classification clas : clas2values.keySet()) {
            if (classifications == null || !classifications.contains(clas)) {
                final Instance delInst = Instance.get((String) clas2values.get(clas).get("OID"));
                if (delInst.isValid()) {
                    final Delete del = new Delete(delInst);
                    del.execute();
                }
                final Instance relDelInst = Instance.get((String) clas2values.get(clas).get("relOID"));
                if (relDelInst.isValid()) {
                    final Delete del = new Delete(relDelInst);
                    del.execute();
                }

            }
        }
    }

    /**
     * Method to update the fieldtables.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @param _instance instance
     * @param _fieldTables list of fieldtables
     * @throws EFapsException on error
     */
    public void updateFieldTable(final Parameter _parameter,
                                 final Instance _instance,
                                 final List<FieldTable> _fieldTables)
        throws EFapsException
    {
        if (_fieldTables.size() > 0) {
            final List<RowUpdate> rows = analyseFieldTables(_parameter, _fieldTables);
            deleteRows(_parameter, rows);
            updateAddRows(_parameter, rows);
        }
    }

    /**
     * Delete removed rows.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @param _rows rows to be updated or added
     * @throws EFapsException on error
     */
    public void deleteRows(final Parameter _parameter,
                           final List<RowUpdate> _rows)
        throws EFapsException
    {
        final Map<?, ?> oids = (Map<?, ?>) _parameter.get(ParameterValues.OIDMAP4UI);
        final Map<Type, Set<String>> exist = new HashMap<>();
        final Map<Type, String> type2LinkAttr = new HashMap<>();
        for (final RowUpdate row : _rows) {
            if (oids.containsKey(row.getRowId())) {
                Set<String> set;
                if (exist.containsKey(row.getType())) {
                    set = exist.get(row.getType());
                } else {
                    set = new HashSet<>();
                    exist.put(row.getType(), set);
                    type2LinkAttr.put(row.getType(), row.getLinkAttrName());
                }
                set.add((String) oids.get(row.getRowId()));
            }
        }

        for (final Entry<Type, Set<String>> entry : exist.entrySet()) {
            final QueryBuilder queryBldr = new QueryBuilder(entry.getKey());
            queryBldr.addWhereAttrEqValue(type2LinkAttr.get(entry.getKey()),
                            _parameter.getInstance().getId());
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            while (query.next()) {
                final String oid = query.getCurrentValue().getOid();
                if (!entry.getValue().contains(oid)) {
                    final Delete del = new Delete(oid);
                    del.execute();
                }
            }
        }
    }

    /**
     * Update and add new Rows.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @param _rows rows to be updated or added
     * @throws EFapsException on error
     */
    public void updateAddRows(final Parameter _parameter,
                              final List<RowUpdate> _rows)
        throws EFapsException
    {
        // TODO compare with database before update??
        final Map<?, ?> oids = (Map<?, ?>) _parameter.get(ParameterValues.OIDMAP4UI);
        for (final RowUpdate row : _rows) {
            if (!Edit_Base.FAKEROW.equals(row.getRowId())) {
                final String oid = (String) oids.get(row.getRowId());
                final Update update;
                if (oid != null) {
                    update = new Update(oid);
                } else {
                    update = new Insert(row.getType());
                    update.add(row.getLinkAttrName(), _parameter.getInstance().getId());
                }
                for (final Entry<String, Object> entry : row.getValues().entrySet()) {
                    update.add(entry.getKey(), entry.getValue());
                }
                add2Update4FieldTable(_parameter, update, row);
                update.execute();
            }
        }
    }

    /**
     * Add additionals to the Update.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _update Updaet that will be executed
     * @param _row row info
     * @throws EFapsException on error
     */
    protected void add2Update4FieldTable(final Parameter _parameter,
                                         final Update _update,
                                         final RowUpdate _row)
        throws EFapsException
    {
        // to be implemented by subclasses.
    }

    /**
     * Analyze the table values row by row.
     *
     * @param _parameter Parameter as passed fromthe eFaps API
     * @param _fieldTables list of fieldtables
     * @return list of rows
     * @throws CacheReloadException on error
     */
    public List<RowUpdate> analyseFieldTables(final Parameter _parameter,
                                              final List<FieldTable> _fieldTables)
        throws CacheReloadException
    {
        final List<RowUpdate> rows = new ArrayList<>();
        boolean deleteAll = false;
        if (_parameter.getParameterValues("eFapsTRID") == null) {
            deleteAll = true;
        } else {
            // get all rows
            for (final String rowId : _parameter.getParameterValues("eFapsTRID")) {
                rows.add(new RowUpdate(rowId));
            }
        }
        final Iterator<RowUpdate> rowiter = rows.iterator();
        for (final FieldTable fieldTable : _fieldTables) {
            if (fieldTable.isEditableDisplay(TargetMode.EDIT)) {
                // get the type of the table object
                final List<EventDefinition> events = fieldTable.getEvents(EventType.UI_TABLE_EVALUATE);
                if (events.size() > 0) {
                    final EventDefinition event = events.get(0);
                    final Type type;
                    final String conattr;
                    if (event.getProperty("Expand") != null) {
                        // TODO remove!
                        Edit_Base.LOG.error("Use of deprecated api calling for Field: '%s' in Collection '%s' ",
                                        new Object[] { fieldTable.getName(), fieldTable.getCollection().getName() });
                        final String expand = event.getProperty("Expand");
                        final String[] typeatt = expand.split("\\\\");
                        final String typeStr = typeatt[0];
                        type = Type.get(typeStr);
                        conattr = typeatt[1];
                    } else if (event.getProperty("Types") != null) {
                        Edit_Base.LOG.error("Use of deprecated api calling for Field: '%s' in Collection '%s' ",
                                        new Object[] { fieldTable.getName(), fieldTable.getCollection().getName() });
                        type = Type.get(event.getProperty("Types"));
                        conattr = event.getProperty("LinkFroms");
                    } else {
                        type = Type.get(event.getProperty("Type"));
                        conattr = event.getProperty("LinkFrom");
                    }
                    if (deleteAll) {
                        final RowUpdate fake = new RowUpdate(Edit_Base.FAKEROW);
                        fake.setType(type);
                        fake.setLinkAttrName(conattr);
                        rows.add(fake);
                    } else {
                        int i = 0;
                        boolean more = true;
                        boolean first = true;
                        while (more && rowiter.hasNext()) {
                            RowUpdate row = null;
                            if (!first) {
                                row = rowiter.next();
                                row.setIndex(i);
                                row.setType(type);
                                row.setLinkAttrName(conattr);
                            }
                            for (final Field field : fieldTable.getTargetTable().getFields()) {
                                final String attrName = field.getAttribute();
                                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                    final String[] values = _parameter.getParameterValues(field.getName());
                                    if (values != null && values.length > i) {
                                        if (first) {
                                            row = rowiter.next();
                                            row.setIndex(i);
                                            row.setType(type);
                                            row.setLinkAttrName(conattr);
                                            first = false;
                                        }
                                        row.addValue(_parameter, attrName, field.getName(), i);
                                    } else {
                                        more = false;
                                        break;
                                    }
                                }
                            }
                            i++;
                        }
                    }
                }
            }
        }
        return rows;
    }

    /**
     * Update connection 2 object.
     *
     * @param _parameter the _parameter
     * @param _instance the instance
     * @throws EFapsException on error
     */
    public void updateConnection2Object(final Parameter _parameter,
                                        final Instance _instance)
        throws EFapsException
    {
        final Map<Integer, String> connectTypes = analyseProperty(_parameter, "ConnectType");
        if (!connectTypes.isEmpty()) {
            final Map<Integer, String> currentLinks = analyseProperty(_parameter, "ConnectCurrentLink");
            final Map<Integer, String> foreignLinks = analyseProperty(_parameter, "ConnectForeignLink");
            final Map<Integer, String> foreignFields = analyseProperty(_parameter, "ConnectForeignField");
            // all must be of the same size
            if (connectTypes.size() == currentLinks.size() && foreignLinks.size() == foreignFields.size()
                            && connectTypes.size() == foreignLinks.size()) {
                for (final Entry<Integer, String> entry : connectTypes.entrySet()) {
                    final String[] foreigns = _parameter.getParameterValues(foreignFields.get(entry.getKey()));
                    if (foreigns != null) {
                        for (final String foreign : foreigns) {
                            final String typeStr = entry.getValue();
                            final Type type = isUUID(typeStr) ? Type.get(UUID.fromString(typeStr)) : Type.get(typeStr);

                            final QueryBuilder queryBldr = new QueryBuilder(type);
                            queryBldr.addWhereAttrEqValue(currentLinks.get(entry.getKey()), _instance);
                            final List<Instance> insts = queryBldr.getQuery().execute();
                            if (insts.size() == 1 && StringUtils.isEmpty(foreign)) {
                                new Delete(insts.get(0)).execute();
                            } else if (insts.size() < 2 && !StringUtils.isEmpty(foreign)) {
                                final Update update = insts.isEmpty() ? new Insert(type) : new Update(insts.get(0));
                                if (insts.isEmpty()) {
                                    update.add(currentLinks.get(entry.getKey()), _instance);
                                }
                                final Instance inst = Instance.get(foreign);
                                if (inst.isValid()) {
                                    update.add(foreignLinks.get(entry.getKey()), inst);
                                } else {
                                    update.add(foreignLinks.get(entry.getKey()), foreign);
                                }
                                add2updateConnection2Object(_parameter, update);
                                update.execute();
                            }
                        }
                    }
                }
            } else {
                Edit_Base.LOG.error("The properties must be of the same size!");
            }
        }
    }

    /**
     * Add2update connection2 object.
     *
     * @param _parameter the _parameter
     * @param _update the _update
     * @throws EFapsException on error
     */
    protected void add2updateConnection2Object(final Parameter _parameter,
                                               final Update _update)
        throws EFapsException
    {

    }

    /**
     * Class for update of a row.
     */
    public static class RowUpdate
    {

        /**
         * Id of this row.
         */
        private final String rowId;

        /**
         * index of this row.
         */
        private int index;

        /**
         * Type for this row.
         */
        private Type type;

        /**
         * Values in this row.
         */
        private final Map<String, Object> values = new HashMap<>();

        /**
         * Name of the link attribute.
         */
        private String linkAttrName;

        /**
         * @param _rowId id of the row
         */
        public RowUpdate(final String _rowId)
        {
            rowId = _rowId;
        }

        /**
         * Getter method for the instance variable {@link #rowId}.
         *
         * @return value of instance variable {@link #rowId}
         */
        public String getRowId()
        {
            return rowId;
        }

        /**
         * Getter method for the instance variable {@link #index}.
         *
         * @return value of instance variable {@link #index}
         */
        public int getIndex()
        {
            return index;
        }

        /**
         * Getter method for the instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public Type getType()
        {
            return type;
        }

        /**
         * Getter method for the instance variable {@link #values}.
         *
         * @return value of instance variable {@link #values}
         */
        public Map<String, Object> getValues()
        {
            return values;
        }

        /**
         * Adds the value.
         *
         * @param _parameter Parameter as passed by the eFaps API
         * @param _attrName name of the attribute
         * @param _fieldName the field name
         * @param _idx the idx
         */
        public void addValue(final Parameter _parameter,
                             final String _attrName,
                             final String _fieldName,
                             final int _idx)
        {
            final Attribute attribute = getType().getAttribute(_attrName);
            if (attribute != null) {
                final Object value;
                if (attribute.hasUoM()) {
                    value = new Object[] { _parameter.getParameterValues(_fieldName)[_idx],
                                    _parameter.getParameterValues(_fieldName + "UoM")[_idx] };
                } else if (attribute.getAttributeType().getDbAttrType() instanceof RateType) {
                    final String valueTmp = _parameter.getParameterValues(_fieldName)[_idx];
                    final boolean inverted = "true".equalsIgnoreCase(_parameter.getParameterValues(_fieldName
                                    + RateUI.INVERTEDSUFFIX)[_idx]);
                    value = new Object[] { inverted ? 1 : valueTmp, inverted ? valueTmp : 1 };
                } else if (attribute.hasLink()) {
                    final String valueTmp = _parameter.getParameterValues(_fieldName)[_idx];
                    final Instance inst = Instance.get(valueTmp);
                    if (inst.isValid()) {
                        value = inst;
                    } else {
                        value = valueTmp;
                    }
                } else {
                    value = _parameter.getParameterValues(_fieldName)[_idx];
                }
                values.put(_attrName, value);
            }
        }

        /**
         * Setter method for instance variable {@link #type}.
         *
         * @param _type value for instance variable {@link #type}
         */
        public void setType(final Type _type)
        {
            type = _type;
        }

        /**
         * Setter method for instance variable {@link #index}.
         *
         * @param _index value for instance variable {@link #index}
         */
        public void setIndex(final int _index)
        {
            index = _index;
        }

        /**
         * Getter method for the instance variable {@link #linkAttrName}.
         *
         * @return value of instance variable {@link #linkAttrName}
         */
        public String getLinkAttrName()
        {
            return linkAttrName;
        }

        /**
         * Setter method for instance variable {@link #linkAttrName}.
         *
         * @param _linkAttrName value for instance variable
         *            {@link #linkAttrName}
         */

        public void setLinkAttrName(final String _linkAttrName)
        {
            linkAttrName = _linkAttrName;
        }
    }
}
