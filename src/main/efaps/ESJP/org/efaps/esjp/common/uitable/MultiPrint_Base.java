/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.esjp.common.uitable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.FilterDefault;
import org.efaps.api.ui.FilterType;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IFilterList;
import org.efaps.api.ui.IListFilter;
import org.efaps.api.ui.IMapFilter;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic class to get the Instances for a UI-table.
 *
* @author The eFaps Team
 */
@EFapsUUID("49c223e8-e500-4c91-a949-576b63c4fb31")
@EFapsApplication("eFaps-Kernel")
public abstract class MultiPrint_Base
    extends AbstractCommon
{
    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(MultiPrint.class);

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return with List of instances
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, getInstances(_parameter));
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return with List of instances
     * @throws EFapsException on error
     */
    public List<Instance> getInstances(final Parameter _parameter)
        throws EFapsException
    {
        final List<Instance> instances = new ArrayList<>();
        final boolean hasTable = _parameter.get(ParameterValues.OTHERS) instanceof IFilterList;

        final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
        add2QueryBldr(_parameter, queryBldr);
        if (!hasTable || analyzeTable(_parameter,
                        (IFilterList) _parameter.get(ParameterValues.OTHERS), queryBldr)) {
            final Collection<String> selects = analyseProperty(_parameter, "InstanceSelect").values();
            if (selects.isEmpty()) {
                final InstanceQuery query = queryBldr.getQuery();
                instances.addAll(query.execute());
            } else {
                final MultiPrintQuery multi = queryBldr.getPrint();
                for (final String select : selects) {
                    multi.addSelect(select);
                }
                multi.execute();
                while (multi.next()) {
                    for (final String select : selects) {
                        final Instance inst = multi.getSelect(select);
                        if (inst != null && inst.isValid()) {
                            instances.add(inst);
                        }
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Get the Status attribute name of a type by searching in the parent types.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _type Type to use
     * @return name of a StatusAttribute
     * @throws EFapsException on error
     */
    protected String getStatusAttribute4Type(final Parameter _parameter,
                                             final Type _type)
        throws EFapsException
    {
        Type type = _type;
        Attribute attr = type.getStatusAttribute();
        while (attr == null) {
            type = type.getParentType();
            attr = type.getStatusAttribute();
        }
        return attr.getName();
    }

    /**
     * @param _parameter    Parameter as passes by the eFaps API
     * @param _queryBldr    QueryBuilder to add to
     * @throws EFapsException on error
     */
    protected void add2QueryBldr(final Parameter _parameter,
                                 final QueryBuilder _queryBldr)
        throws EFapsException
    {

    }

    /**
     * Method to get the List of instances.
     *
     * @param _parameter parameter from the eFaps API
     * @param _filterlist the filterlist
     * @param _queryBldr QueryBuilder used to get the instances
     * @return List of instance
     * @throws EFapsException on error
     */
    protected boolean analyzeTable(final Parameter _parameter,
                                   final IFilterList _filterlist,
                                   final QueryBuilder _queryBldr)
        throws EFapsException
    {
        return analyzeTable(_parameter, _filterlist, _queryBldr, Type.get(_queryBldr.getTypeUUID()));
    }

    /**
     * Method to get the List of instances.
     *
     * @param _parameter parameter from the eFaps API
     * @param _filterlist the filterlist
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type the query is based on
     * @return List of instance
     * @throws EFapsException on error
     */
    protected boolean analyzeTable(final Parameter _parameter,
                                   final IFilterList _filterlist,
                                   final QueryBuilder _queryBldr,
                                   final Type _type)
        throws EFapsException
    {
        boolean exec = true;
        if (CollectionUtils.isNotEmpty(_filterlist)) {
            // filtering
            for (final IFilter filter : _filterlist) {
                final Field field = Field.get(filter.getFieldId());
                if (field.getFilter().getType().equals(FilterType.FREETEXT)
                                || field.getFilter().getType().equals(FilterType.FORM)
                                                && field.getFilter().getAttributes() != null) {
                    String attrName = field.getAttribute();
                    String[] attrNames = null;
                    if (field.getFilter().getAttributes() != null) {
                        if (field.getFilter().getAttributes().contains(",")) {
                            attrNames = field.getFilter().getAttributes().split(",");
                        } else {
                            attrName = field.getFilter().getAttributes();
                        }
                    }
                    if (attrNames != null) {
                        exec = addInsideRangeFilter(_parameter, filter, _queryBldr, _type, attrNames);
                    } else {
                        if (attrName != null && _type.getAttribute(attrName) != null) {
                            exec = addFilter(_parameter, filter, _queryBldr, _type, attrName);
                        } else {
                            exec = addFilter4Select(_parameter, filter, _queryBldr, _type, field.getSelect());
                        }
                    }
                    if (!exec) {
                        break;
                    }
                } else if (field.getFilter().getType().equals(FilterType.CLASSIFICATION)) {
                    exec = addClassFilter(_parameter, filter, _queryBldr, _type);
                } else if (field.getFilter().getType().equals(FilterType.STATUS)) {
                    exec = addStatusFilter(_parameter, filter, _queryBldr, _type);
                }
            }
        }
        return exec;
    }

    /**
     * Method to add a Filter for Status.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _filter the filter
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addStatusFilter(final Parameter _parameter,
                                      final IFilter _filter,
                                      final QueryBuilder _queryBldr,
                                      final Type _type)
        throws EFapsException
    {
        boolean ret = false;
        if (_filter instanceof IListFilter) {
            if (ArrayUtils.isNotEmpty(((IListFilter) _filter).getValues())) {
                _queryBldr.addWhereAttrEqValue(Field.get(_filter.getFieldId()).getAttribute(), ((IListFilter) _filter)
                                .getValues());
                ret = true;
            }
        } else {
            final List<Status> filters = new ArrayList<>();
            final String defaultvalues = Field.get(_filter.getFieldId()).getFilter().getDefaultValue();
            if (defaultvalues != null && !defaultvalues.isEmpty()) {
                final String[] defaultAr = defaultvalues.split(";");
                final Attribute attr = _type.getStatusAttribute();
                final Type statusgrp = attr.getLink();
                final List<Status> status = getStatus4Type(statusgrp);
                for (final String defaultv : defaultAr) {
                    for (final Status statusTmp : status) {
                        if (defaultv.equals(statusTmp.getKey())) {
                            filters.add(statusTmp);
                        }
                    }
                }
            }
            if (!filters.isEmpty()) {
                _queryBldr.addWhereAttrEqValue(Field.get(_filter.getFieldId()).getAttribute(), filters.toArray());
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Recursive method to get all status for a Type representing a StatusGrp.
     * @param _type Type the status list is wanted for
     * @return list of status
     * @throws CacheReloadException on error
     */
    protected List<Status> getStatus4Type(final Type _type)
        throws CacheReloadException
    {
        final List<Status> ret = new ArrayList<>();
        final StatusGroup grp = Status.get(_type.getUUID());
        if (grp != null) {
            ret.addAll(grp.values());
        } else {
            for (final Type type : _type.getChildTypes()) {
                ret.addAll(getStatus4Type(type));
            }
        }
        return ret;
    }

    /**
     * Method to add a Filter for Classification.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _filter the filter
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addClassFilter(final Parameter _parameter,
                                     final IFilter _filter,
                                     final QueryBuilder _queryBldr,
                                     final Type _type)
        throws EFapsException
    {
        boolean ret = false;
        if (_filter instanceof IListFilter) {
            final List<?> list = Arrays.asList(((IListFilter) _filter).getValues());
            final Set<Classification> filters = new HashSet<>();
            final Set<Classification> remove = new HashSet<>();
            for (final Object obj : list) {
                if (obj != null) {
                    filters.add((Classification) Type.get((UUID) obj));
                }
            }

            for (final Classification clazz : filters) {
                for (final Classification child : clazz.getChildClassifications()) {
                    if (filters.contains(child)) {
                        remove.add(clazz);
                        break;
                    }
                }
            }
            filters.removeAll(remove);
            if (!filters.isEmpty()) {
                _queryBldr.addWhereClassification(filters.toArray(new Classification[filters.size()]));
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Method to add a Filter for a inside Range of two attributes.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _filter the filter
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @param _attrNames names of the attributes
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addInsideRangeFilter(final Parameter _parameter,
                                           final IFilter _filter,
                                           final QueryBuilder _queryBldr,
                                           final Type _type,
                                           final String[] _attrNames)
        throws EFapsException
    {
        boolean ret = true;
        final Attribute attr1;
        if (_attrNames[0].contains("/")) {
            attr1 = Attribute.get(_attrNames[0]);
        } else {
            attr1 = _type.getAttribute(_attrNames[0]);
        }
        final Attribute attr2;
        if (_attrNames[0].contains("/")) {
            attr2 = Attribute.get(_attrNames[1]);
        } else {
            attr2 = _type.getAttribute(_attrNames[1]);
        }
        final UUID attrTypeUUId = attr1.getAttributeType().getUUID();
        final String from;
        final String to;
        if (_filter instanceof IMapFilter) {
            from = (String) ((IMapFilter) _filter).get("from");
            to = (String) ((IMapFilter) _filter).get("to");
        } else {
            from = null;
            to = null;
        }
        if ((from == null || to == null) && Field.get(_filter.getFieldId()).getFilter().getDefaultValue() == null) {
            if (from != null && to == null && !from.isEmpty()) {
                _queryBldr.addWhereAttrMatchValue(attr1, from).setIgnoreCase(true);
            } else {
                ret = false;
            }
        } else {
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (from == null || to == null) {
                    final DateTime[] dates = (DateTime[]) getFilter(Field.get(_filter.getFieldId()));
                    dateTo = dates[0];
                    dateFrom = dateTo.plusMinutes(2);
                } else {
                    dateFrom = DateTimeUtil.translateFromUI(from).minusMinutes(1);
                    dateTo = DateTimeUtil.translateFromUI(from).plusMinutes(1);
                }
                _queryBldr.addWhereAttrLessValue(attr1, dateFrom);
                _queryBldr.addWhereAttrGreaterValue(attr2, dateTo);
            }
        }
        return ret;
    }

    /**
     * Method to add a Filter for one attribute.
     *
     * @param _parameter the _parameter
     * @param _filter the filter
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @param _attrName name of the attribute
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addFilter(final Parameter _parameter,
                                final IFilter _filter,
                                final QueryBuilder _queryBldr,
                                final Type _type,
                                final String _attrName)
        throws EFapsException
    {
        boolean ret = true;
        final Attribute attr = _type.getAttribute(_attrName);
        final IUIProvider uiProvider = attr.getAttributeType().getUIProvider();

        if (_filter instanceof IMapFilter) {
            final Map<?, ?> inner = (IMapFilter) _filter;
            final String from = (String) inner.get("from");
            final String to = (String) inner.get("to");
            // Date or DateTime
            if (uiProvider instanceof DateTimeUI || uiProvider instanceof DateUI) {
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (from == null || to == null) {
                    final DateTime[] dates = (DateTime[]) getFilter(Field.get(_filter.getFieldId()));
                    dateFrom = dates[0];
                    dateTo = dates[1];
                } else {
                    dateFrom = DateTimeUtil.translateFromUI(from).minusSeconds(1);
                    dateTo = DateTimeUtil.translateFromUI(to).plusDays(1);
                }
                _queryBldr.addWhereAttrGreaterValue(_attrName, dateFrom);
                _queryBldr.addWhereAttrLessValue(_attrName, dateTo);
            } else {
                if (from != null && !from.isEmpty()) {
                    addMatch(_parameter, _queryBldr, _attrName, inner, new String[] { from });
                } else if (inner.containsKey(Field.get(_filter.getFieldId()).getName())) {
                    final String[] filter = (String[]) inner.get(Field.get(_filter.getFieldId()).getName());
                    addMatch(_parameter, _queryBldr, _attrName, inner, filter);
                } else {
                    final String[] filter = (String[]) getFilter(Field.get(_filter.getFieldId()));
                    if (filter != null) {
                        addMatch(_parameter, _queryBldr, _attrName, inner, filter);
                    } else {
                        ret = false;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Adds the match.
     *
     * @param _parameter the _parameter
     * @param _queryBldr the _query bldr
     * @param _attrName the _attr name
     * @param _filterMap the _filter map
     * @param _criterias the _criterias
     * @throws EFapsException the e faps exception
     */
    protected void addMatch(final Parameter _parameter,
                            final QueryBuilder _queryBldr,
                            final String _attrName,
                            final Map<?, ?> _filterMap,
                            final String[] _criterias)
        throws EFapsException
    {
        boolean ignoreCase = true;
        final String[] value = _criterias;
        if (_filterMap.containsKey("expertMode")) {
            if (!(Boolean) _filterMap.get("expertMode")) {
                for (int i = 0; i < value.length; i++) {
                    value[i] = "*" + value[i] + "*";
                }
            }
        }
        if (_filterMap.containsKey("ignoreCase")) {
            ignoreCase = (Boolean) _filterMap.get("ignoreCase");
        }
        _queryBldr.addWhereAttrMatchValue(_attrName, (Object[]) value).setIgnoreCase(ignoreCase);
    }


    /**
     * Method to add a Filter for one select.
     *
     * @param _parameter the _parameter
     * @param _filter the filter
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @param _select of the field
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addFilter4Select(final Parameter _parameter,
                                       final IFilter _filter,
                                       final QueryBuilder _queryBldr,
                                       final Type _type,
                                       final String _select)
        throws EFapsException
    {
        boolean ret = true;
        if (_filter instanceof IMapFilter) {
            final Map<?, ?> inner = (IMapFilter) _filter;
            final String from = (String) inner.get("from");
            final boolean validFrom = from != null && !from.isEmpty() ? true : false;
            final String to = (String) inner.get("to");
            final boolean validTo = to != null && !to.isEmpty() ? true : false;
            final Object[] filter = getFilter(Field.get(_filter.getFieldId()));

            if (validFrom || !validFrom && filter != null || validFrom && validTo || !validFrom && !validTo
                            && filter != null) {
                final String[] parts = _select.split("\\.");
                final List<String> lstParts = new ArrayList<>();
                for (final String part : parts) {
                    lstParts.add(part);
                }
                if (_select.startsWith("class")) {
                    final String typeStr = lstParts.get(0).substring(6, lstParts.get(0).length() - 1);
                    final Classification classification = (Classification) Type.get(typeStr);
                    final AttributeQuery attrQuery = evaluateFilterSelect(_parameter, inner, lstParts, 1,
                                    classification, from, to, filter);

                    _queryBldr.addWhereAttrInQuery("ID", attrQuery);
                } else if (_select.startsWith("linkto")) {
                    final String attrName = lstParts.get(0).substring(7, lstParts.get(0).length() - 1);
                    final Attribute attr = _type.getAttribute(attrName);
                    final AttributeQuery attrQuery = evaluateFilterSelect(_parameter, inner, lstParts, 1, attr
                                    .getLink(), from, to, filter);

                    _queryBldr.addWhereAttrInQuery(attrName, attrQuery);
                } else if (_select.startsWith("linkfrom")) {
                    final String[] parts2 = lstParts.get(0).split("#");
                    final String typeStr = parts2[0].substring(9);
                    final Type type = Type.get(typeStr);
                    final String attrName = parts2[1].substring(0, parts2[1].length() - 1);
                    final AttributeQuery attrQuery2 = evaluateFilterSelect(_parameter, inner, lstParts, 1, type, from,
                                    to, filter);

                    final QueryBuilder attrQueryBldr = new QueryBuilder(type);
                    attrQueryBldr.addWhereAttrInQuery("ID", attrQuery2);
                    final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery(attrName);
                    _queryBldr.addWhereAttrInQuery("ID", attrQuery);
                }
            } else {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Evaluate filter select.
     *
     * @param _parameter the _parameter
     * @param _filterMap the _filter map
     * @param _lstParts the _lst parts
     * @param _dir the _dir
     * @param _type the _type
     * @param _from the _from
     * @param _to the _to
     * @param _filter the _filter
     * @return the attribute query
     * @throws EFapsException the e faps exception
     */
    protected AttributeQuery evaluateFilterSelect(final Parameter _parameter,
                                                  final Map<?, ?> _filterMap,
                                                  final List<String> _lstParts,
                                                  final Integer _dir,
                                                  final Type _type,
                                                  final String _from,
                                                  final String _to,
                                                  final Object[] _filter)
        throws EFapsException
    {
        AttributeQuery attrQuery = null;
        if (_lstParts.get(_dir).startsWith("linkto")) {
            final String attrName = _lstParts.get(_dir).substring(7, _lstParts.get(_dir).length() - 1);
            final Attribute attr = _type.getAttribute(attrName);

            final AttributeQuery attrQueryAux = evaluateFilterSelect(_parameter, _filterMap, _lstParts, _dir + 1,
                            attr.getLink(), _from, _to, _filter);
            final QueryBuilder attrQueryBldr = new QueryBuilder(_type);
            attrQueryBldr.addWhereAttrInQuery(attrName, attrQueryAux);

            attrQuery = attrQueryBldr.getAttributeQuery("ID");
        } else if (_lstParts.get(_dir).startsWith("linkfrom")) {

        } else if (_lstParts.get(_dir).startsWith("attribute")) {
            final String attrName = _lstParts.get(_dir).substring(10, _lstParts.get(_dir).length() - 1);
            final QueryBuilder attrQueryBldr = new QueryBuilder(_type);
            final Attribute attr = _type.getAttribute(attrName);
            final UUID attrTypeUUId = attr.getAttributeType().getUUID();
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (_from == null || _to == null) {
                    final DateTime[] dates = (DateTime[]) _filter;
                    dateFrom = dates[0];
                    dateTo = dates[1];
                } else {
                    dateFrom = DateTimeUtil.translateFromUI(_from).minusSeconds(1);
                    dateTo = DateTimeUtil.translateFromUI(_to).plusDays(1);
                }
                attrQueryBldr.addWhereAttrGreaterValue(attrName, dateFrom);
                attrQueryBldr.addWhereAttrLessValue(attrName, dateTo);
            } else {
                if (_from != null && !_from.isEmpty()) {
                    addMatch(_parameter, attrQueryBldr, attrName, _filterMap, new String[] { _from });
                } else {
                    final String[] filter = (String[]) _filter;
                    addMatch(_parameter, attrQueryBldr, attrName, _filterMap, filter);
                }
            }

            if (_type instanceof Classification) {
                attrQuery = attrQueryBldr.getAttributeQuery(((Classification) _type).getLinkAttributeName());
            } else {
                attrQuery = attrQueryBldr.getAttributeQuery("ID");
            }
        } else if (_lstParts.get(_dir).startsWith("class")) {
            final String typeStr = _lstParts.get(_dir).substring(6, _lstParts.get(_dir).length() - 1);
            final Classification classification = (Classification) Type.get(typeStr);

            final AttributeQuery attrQueryAux = evaluateFilterSelect(_parameter, _filterMap, _lstParts, _dir + 1,
                            classification, _from, _to, _filter);

            final QueryBuilder attrQueryBldr = new QueryBuilder(_type);
            attrQueryBldr.addWhereAttrInQuery("ID", attrQueryAux);
            attrQuery = attrQueryBldr.getAttributeQuery("ID");
        }
        return attrQuery;
    }

    /**
     * @param _field Field the date is wanted for
     * @return datetime array
     * @throws EFapsException on error
     */
    protected Object[] getFilter(final Field _field)
        throws EFapsException
    {
        Object[] ret = null;
        final String filter = _field.getFilter().getDefaultValue();
        if (filter != null) {
            final String[] parts = filter.split(":");
            final String range = parts[0];
            final int fromSub = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            final int rangeCount = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
            DateTime dateFrom = new DateTime();
            DateTime dateTo = new DateTime();
            if (range != null) {
                final FilterDefault def = FilterDefault.valueOf(range.toUpperCase());
                // to get a timezone dependent DateTim
                DateTime tmp = DateTimeUtil.translateFromUI(new DateTime()).withTimeAtStartOfDay();
                switch (def) {
                    case TODAY:
                        dateFrom = tmp.toDateTime().minusDays(fromSub).minusMinutes(1);
                        dateTo = dateFrom.plusDays(rangeCount).plusSeconds(1);
                        ret = new DateTime[] { dateFrom, dateTo };
                        break;
                    case WEEK:
                        // the first of the current week
                        tmp = tmp.minusDays(tmp.getDayOfWeek() - 1);
                        dateFrom = tmp.minusWeeks(fromSub).minusMinutes(1);
                        dateTo = tmp.plusWeeks(rangeCount);
                        ret = new DateTime[] { dateFrom, dateTo };
                        break;
                    case MONTH:
                        // the first of the current month
                        tmp = tmp.minusDays(tmp.getDayOfMonth() - 1);
                        // substract the month and a minute before
                        dateFrom = tmp.minusMonths(fromSub).minusMinutes(1);
                        // add the month
                        dateTo = tmp.plusMonths(rangeCount);
                        ret = new DateTime[] { dateFrom, dateTo };
                        break;
                    case YEAR:
                        tmp = tmp.minusDays(tmp.getDayOfYear() - 1);
                        dateFrom = tmp.minusYears(fromSub).minusMinutes(1);
                        dateTo = tmp.plusYears(rangeCount);
                        ret = new DateTime[] { dateFrom, dateTo };
                        break;
                    case ALL:
                        ret = new String[] {"*"};
                        break;
                    case NONE:
                        break;
                    default:
                        ret = new String[] {range + "*"};
                        break;
                }
            }
        }
        return ret;
    }
}
