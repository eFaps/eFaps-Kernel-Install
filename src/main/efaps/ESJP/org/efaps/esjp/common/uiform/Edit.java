/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.common.uiform;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
@EFapsRevision("$Rev$")
public class Edit
    implements EventExecution
{

    /**
     * @param _parameter Parameter as provided by eFaps for a esjp
     * @throws EFapsException on error
     * @return empty Return
     */
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
                final String attrName = field.getExpression() == null
                                ? field.getAttribute()
                                : field.getExpression();
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
        String ret = null;
        final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
        final List<FieldTable> fieldTables = new ArrayList<FieldTable>();
        final List<Field> fields = new ArrayList<Field>();

        final SearchQuery query = new SearchQuery();
        query.setObject(_instance);
        for (final Field field : _form.getFields()) {
            if (field instanceof FieldSet) {
                fieldsets.add((FieldSet) field);
            } else if (field instanceof FieldTable) {
                fieldTables.add((FieldTable) field);
            } else if (field instanceof FieldClassification) {
                ret = ((FieldClassification) field).getClassificationName();
            } else {
                final String attrName = field.getExpression() == null
                                ? field.getAttribute()
                                : field.getExpression();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = _instance.getType().getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        query.addSelect(attrName);
                        fields.add(field);
                    }
                }
            }
        }
        query.execute();
        final Context context = Context.getThreadContext();
        if (query.next()) {
            final Update update = new Update(_instance);
            for (final Field field : fields) {
                if (context.getParameters().containsKey(field.getName())) {
                    final String newValue = context.getParameter(field.getName());
                    final String attrName = field.getExpression() == null
                                    ? field.getAttribute()
                                    : field.getExpression();
                    final Object object = query.get(attrName);
                    final String oldValue = object != null ? object.toString() : null;
                    if (!newValue.equals(oldValue)) {
                        final Attribute attr = _instance.getType().getAttribute(attrName);
                        if (attr.hasUoM()) {
                            update.add(attr, new Object[] { context.getParameter(field.getName()),
                                            context.getParameter(field.getName() + "UoM") });
                        } else {
                            update.add(attr, newValue);
                        }
                    }
                }
            }
            update.execute();
        }
        updateFieldSets(_parameter, _instance, fieldsets);
        updateFieldTable(_parameter, _instance, fieldTables);
        return ret;
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
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        for (final FieldSet fieldset : _fieldsets) {
            final String setName = fieldset.getExpression() == null
                            ? fieldset.getAttribute()
                            : fieldset.getExpression();
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
                                if (child.hasUoM()) {
                                    setupdate.add(child, new Object[] { newValue,
                                                    _parameter.getParameterValue(fieldName + "UoM") });
                                } else {
                                    setupdate.add(child, newValue);
                                }
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
                                if (child.hasUoM()) {
                                    insert.add(child, new Object[] { _parameter.getParameterValue(fieldName),
                                                    _parameter.getParameterValue(fieldName + "UoM") });
                                } else {
                                    insert.add(child, _parameter.getParameterValue(fieldName));
                                }
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
        final Classification classType = (Classification) Type.get(_classifcationName);

        final Map<Classification, Map<String, Object>> clas2values = new HashMap<Classification, Map<String, Object>>();
        // get the already existing classifications
        final SearchQuery relQuery = new SearchQuery();
        relQuery.setExpand(_instance, classType.getClassifyRelationType().getName() + "\\"
                        + classType.getRelLinkAttributeName());
        relQuery.addSelect(classType.getRelTypeAttributeName());
        relQuery.addSelect("OID");
        relQuery.execute();

        while (relQuery.next()) {
            final Long typeid = (Long) relQuery.get(classType.getRelTypeAttributeName());
            final Classification subClassType = (Classification) Type.get(typeid);
            final Map<String, Object> values = new HashMap<String, Object>();
            clas2values.put(subClassType, values);

            final SearchQuery subquery = new SearchQuery();
            subquery.setExpand(_instance, subClassType.getName() + "\\" + subClassType.getLinkAttributeName());
            subquery.addSelect("OID");

            final List<Field> fields = new ArrayList<Field>();
            final Form form = Form.getTypeForm(subClassType);
            for (final Field field : form.getFields()) {
                final String attrName = field.getExpression() == null
                                ? field.getAttribute()
                                : field.getExpression();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = subClassType.getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        subquery.addSelect(attrName);
                        fields.add(field);
                    }
                }
            }
            subquery.execute();
            if (subquery.next()) {
                for (final Field field : fields) {
                    final String attrName = field.getExpression() == null
                                    ? field.getAttribute()
                                    : field.getExpression();
                    values.put(field.getName(), subquery.get(attrName));
                }
                values.put("OID", subquery.get("OID"));
            }
            subquery.close();
            values.put("relOID", relQuery.get("OID"));
        }
        relQuery.close();

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
                    final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final String attrName = field.getExpression() == null
                                            ? field.getAttribute()
                                            : field.getExpression();
                            if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null
                                                && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                .getClassRepr())) {
                                    if (_parameter.getParameters().containsKey(field.getName())) {
                                        final String value = _parameter.getParameterValue(field.getName());
                                        if (attr.hasUoM()) {
                                            classInsert.add(attr, new Object[] { value,
                                                            _parameter.getParameterValue(field.getName() + "UoM") });
                                        } else {
                                            classInsert.add(attr, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    classInsert.execute();
                    updateFieldSets(_parameter, classInsert.getInstance(), fieldsets);
                } else {
                    final Map<String, Object> values = clas2values.get(classification);
                    final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
                    final Update update = new Update((String) values.get("OID"));
                    boolean execUpdate = false;
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final String attrName = field.getExpression() == null
                                            ? field.getAttribute()
                                            : field.getExpression();
                            if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null
                                                && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                .getClassRepr())) {
                                    if (_parameter.getParameters().containsKey(field.getName())) {
                                        final String newValue = _parameter.getParameterValue(field.getName());
                                        final Object value = values.get(field.getName());
                                        final String oldValue = value != null ? value.toString() : null;
                                        if (!newValue.equals(oldValue)) {
                                            execUpdate = true;
                                            if (attr.hasUoM()) {
                                                update.add(attr, new Object[] { newValue,
                                                               _parameter.getParameterValue(field.getName() + "UoM") });
                                            } else {
                                                update.add(attr, newValue);
                                            }
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
                Delete del = new Delete((String) clas2values.get(clas).get("OID"));
                del.execute();
                del = new Delete((String) clas2values.get(clas).get("relOID"));
                del.execute();
            }
        }
    }

    /**
     * Method to update the fieldtables.
     * @param _parameter     Parameter as passed from the eFaps API
     * @param _instance     instance
     * @param _fieldTables  list of fieldtables
     * @throws EFapsException on error
     */
    public void updateFieldTable(final Parameter _parameter,
                                 final Instance _instance,
                                 final List<FieldTable> _fieldTables)
        throws EFapsException
    {
        if (_fieldTables.size() > 0) {
            final List<RowUpdate> rows = analyseFieldTables(_parameter, _fieldTables);
            updateAddRows(_parameter, rows);
            deleteRows(_parameter, rows);
        }
    }

    /**
     * Delete removed rows.
    *  @param _parameter    Parameter as passed from the eFaps API
     * @param _rows         rows to be updated or added
     * @throws EFapsException on error
     */
    public void deleteRows(final Parameter _parameter,
                           final List<RowUpdate> _rows)
        throws EFapsException
    {
        final Map<?, ?> oids = (Map<?, ?>) _parameter.get(ParameterValues.OIDMAP4UI);
        final Map<String, Set<String>> exist = new HashMap<String, Set<String>>();
        for (final RowUpdate row : _rows) {
            if (oids.containsKey(row.getRowId())) {
                Set<String> set;
                if (exist.containsKey(row.getExpand())) {
                    set = exist.get(row.getExpand());
                } else {
                    set = new HashSet<String>();
                    exist.put(row.getExpand(), set);
                }
                set.add((String) oids.get(row.getRowId()));
            }
        }

        for (final Entry<String, Set<String>> entry : exist.entrySet()) {
            final SearchQuery query = new SearchQuery();
            query.setExpand(_parameter.getInstance(),  entry.getKey());
            query.addSelect("OID");
            query.execute();
            while (query.next()) {
                final String oid = (String) query.get("OID");
                if (!entry.getValue().contains(oid)) {
                    final Delete del = new Delete(oid);
                    del.execute();
                }
            }
        }
    }

    /**
     * Update and add new Rows.
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _rows         rows to be updated or added
     * @throws EFapsException on error
     */
    public void updateAddRows(final Parameter _parameter,
                              final List<RowUpdate> _rows)
        throws EFapsException
    {
        //TODO compare with database before update??
        final Map<?, ?> oids = (Map<?, ?>) _parameter.get(ParameterValues.OIDMAP4UI);
        for (final RowUpdate row : _rows) {
            final String oid = (String) oids.get(row.getRowId());
            final Update update;
            if (oid != null) {
                update = new Update(oid);
            } else {
                update = new Insert(row.getType());
                update.add(row.getLinkAttrName(), _parameter.getInstance().getId());
            }
            for (final String[] value : row.getValues()) {
                update.add(value[0], value[1]);
            }
            update.execute();
        }
    }

    /**
     * Analyze the table values row by row.
     * @param _parameter    Parameter as passed fromthe eFaps API
     * @param _fieldTables  list of fieldtables
     * @return list of rows
     */
    public List<RowUpdate> analyseFieldTables(final Parameter _parameter,
                                              final List<FieldTable> _fieldTables)
    {
        final List<RowUpdate> rows = new ArrayList<RowUpdate>();
        // get all rows
        for (final String rowId : _parameter.getParameterValues("eFapsTableRowID")) {
            rows.add(new RowUpdate(rowId));
        }
        final Iterator<RowUpdate> rowiter = rows.iterator();
        for (final FieldTable fieldTable : _fieldTables) {
            if (fieldTable.isEditableDisplay(TargetMode.EDIT)) {
                // get the type of the table object
                final List<EventDefinition> events = fieldTable.getEvents(EventType.UI_TABLE_EVALUATE);
                if (events.size() > 0) {
                    final EventDefinition event = events.get(0);
                    final String expand = event.getProperty("Expand");
                    final String[] typeatt = expand.split("\\\\");
                    final String typeStr = typeatt[0];
                    final Type type = Type.get(typeStr);
                    final String conattr = typeatt[1];

                    int i = 0;
                    boolean more = true;
                    boolean first = true;
                    while (more && rowiter.hasNext()) {
                        RowUpdate row = null;
                        if (!first) {
                            row = rowiter.next();
                            row.setIndex(i);
                            row.setType(type);
                            row.setExpand(expand);
                            row.setLinkAttrName(conattr);
                        }
                        for (final Field field : fieldTable.getTargetTable().getFields()) {
                            final String attrName = field.getAttribute();
                            if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                final String[] values = _parameter.getParameterValues(field.getName());
                                if (values.length > i) {
                                    if (first) {
                                        row = rowiter.next();
                                        row.setIndex(i);
                                        row.setType(type);
                                        row.setExpand(expand);
                                        row.setLinkAttrName(conattr);
                                        first = false;
                                    }
                                    row.addValue(attrName, values[i]);
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
        return rows;
    }

    public class RowUpdate
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
        private final Set<String[]> values = new HashSet<String[]>();

        /**
         * Expand.
         */
        private String expand;

        /**
         * Name of the link attribute.
         */
        private String linkAttrName;

        /**
         * @param _rowId id of the row
         */
        public RowUpdate(final String _rowId)
        {
            this.rowId = _rowId;
        }

        /**
         * Getter method for the instance variable {@link #rowId}.
         *
         * @return value of instance variable {@link #rowId}
         */
        public String getRowId()
        {
            return this.rowId;
        }

        /**
         * Getter method for the instance variable {@link #index}.
         *
         * @return value of instance variable {@link #index}
         */
        public int getIndex()
        {
            return this.index;
        }

        /**
         * Getter method for the instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public Type getType()
        {
            return this.type;
        }

        /**
         * Getter method for the instance variable {@link #values}.
         *
         * @return value of instance variable {@link #values}
         */
        public Set<String[]> getValues()
        {
            return this.values;
        }

        /**
         * @param _attrName name of the attribute
         * @param _value    value
         */
        public void addValue(final String _attrName,
                             final String _value)
        {
            this.values.add(new String[] { _attrName, _value });
        }

        /**
         * Setter method for instance variable {@link #type}.
         *
         * @param _type value for instance variable {@link #type}
         */
        public void setType(final Type _type)
        {
            this.type = _type;
        }

        /**
         * Setter method for instance variable {@link #index}.
         *
         * @param _index value for instance variable {@link #index}
         */
        public void setIndex(final int _index)
        {
            this.index = _index;
        }

        /**
         * Getter method for the instance variable {@link #expand}.
         *
         * @return value of instance variable {@link #expand}
         */
        public String getExpand()
        {
            return this.expand;
        }

        /**
         * Setter method for instance variable {@link #expand}.
         *
         * @param _expand value for instance variable {@link #expand}
         */
        public void setExpand(final String _expand)
        {
            this.expand = _expand;
        }

        /**
         * Getter method for the instance variable {@link #linkAttrName}.
         *
         * @return value of instance variable {@link #linkAttrName}
         */
        public String getLinkAttrName()
        {
            return this.linkAttrName;
        }

        /**
         * Setter method for instance variable {@link #linkAttrName}.
         *
         * @param _linkAttrName value for instance variable {@link #linkAttrName}
         */

        public void setLinkAttrName(final String _linkAttrName)
        {
            this.linkAttrName = _linkAttrName;
        }
    }

}
