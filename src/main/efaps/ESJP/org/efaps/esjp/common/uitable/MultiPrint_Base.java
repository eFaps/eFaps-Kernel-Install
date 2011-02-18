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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.uitable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic class to get the Instances for a UI-table.
 *
 * <table>
 * <tr>
 * <th>Property</th>
 * <th>Value</th>
 * <th>Mandatory</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Types</td>
 * <td>Array with Names of Types, seperated by ";"</td>
 * <td>true</td>
 * <td>-</td>
 * <td>Array with Names of Types used for the QueryBuilder.</td>
 * </tr>
 * <tr>
 * <td>LinkFroms</td>
 * <td>Array with Attributes of the related Type, seperated by ";"</td>
 * <td>false</td>
 * <td>-</td>
 * <td>Array with Names of Attributes of the related Types used for the
 * QueryBuilder as part of the Where Criteria.</td>
 * </tr>
 * <tr>
 * <td>Selects</td>
 * <td>Array of Selects, seperated by ";"</td>
 * <td>false</td>
 * <td>-</td>
 * <td>Selects use to expand the returned Innstances to other objects instances.
 * </td>
 * </tr>
 * <tr>
 * <td>ExpandChildTypes</td>
 * <td>true/false</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Must the ChildTypes be expanded.</td>
 * </tr>
 * </table>
 * <br>
 * Example:<br>
 * <code>
 * &lt;property name=&quot;Types&quot;&gt;Admin_User_Person2Company;Admin_User_Person2Role&lt;/property&gt;<br>
 * &lt;property name=&quot;LinkFroms&quot;&gt;UserLink;UserLink&lt;/property&gt;
 * </code>
 *
 * @author The eFaps Team
 * @version $Id: MultiPrint_Base.java 5369 2010-08-19 18:10:22Z miguel.a.aranya
 *          $
 */
@EFapsUUID("49c223e8-e500-4c91-a949-576b63c4fb31")
@EFapsRevision("$Rev$")
public abstract class MultiPrint_Base
{
    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(MultiPrint.class);

    /**
     * @param _parameter Parameter
     * @return Return with List of instances
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final Map<?, ?> filter = (Map<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final String typesStr = (String) properties.get("Types");
        final String linkFromsStr = (String) properties.get("LinkFroms");
        final String selectStr = (String) properties.get("Selects");
        final boolean includeChildTypes = !"false".equalsIgnoreCase((String) properties.get("ExpandChildTypes"));

        if (MultiPrint_Base.LOG.isDebugEnabled()) {
            MultiPrint_Base.LOG.debug("Types: {}\n LinkFroms: {}\n Selects: {}\n ExpandChildTypes: {}",
                                            new Object[]{typesStr, linkFromsStr, selectStr, includeChildTypes});
        }
        final Map<QueryBuilder, Boolean> queryBldrs2exec = new LinkedHashMap<QueryBuilder, Boolean>();

        final List<Instance> instances = new ArrayList<Instance>();
        if (typesStr != null) {
            final String[] typesArray = typesStr.split(";");
            for (int x = 0; x < typesArray.length; x++) {
                final Type type = Type.get(typesArray[x]);
                final QueryBuilder queryBldr = new QueryBuilder(type);
                if (linkFromsStr != null) {
                    final String[] linkFroms = linkFromsStr.split(";");
                    queryBldr.addWhereAttrEqValue(linkFroms[x], instance.getId());
                }
                add2QueryBldr(_parameter, queryBldr);
                queryBldrs2exec.put(queryBldr, analyzeTable(_parameter, filter, queryBldr, type));
            }
            int count = 0;
            for (final Entry<QueryBuilder, Boolean> entry : queryBldrs2exec.entrySet()) {
                if (entry.getValue()) {
                    if (selectStr != null) {
                        final String[] selects = selectStr.split(":");
                        final MultiPrintQuery multi = entry.getKey().getPrint();
                        if (count < selects.length) {
                            multi.addSelect(selects[count]);
                        }
                        multi.executeWithoutAccessCheck();
                        instances.addAll(count < selects.length ? multi.getInstances4Select(selects[count])
                                                                 : multi.getInstanceList());
                    } else {
                        final InstanceQuery query = entry.getKey().getQuery();
                        query.setIncludeChildTypes(includeChildTypes);
                        instances.addAll(query.execute());
                    }
                }
                count++;
            }
        } else {
            final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
            MultiPrint_Base.LOG.warn("No 'Types' property given for executed Command: '{}'", command.getName());
        }
        ret.put(ReturnValues.VALUES, instances);
        return ret;
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
     * @param _filter map of filters
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type the query is based on
     * @return List of instance
     * @throws EFapsException on error
     */
    protected boolean analyzeTable(final Parameter _parameter,
                                   final Map<?, ?> _filter,
                                   final QueryBuilder _queryBldr,
                                   final Type _type)
        throws EFapsException
    {
        boolean exec = true;
        if (_filter != null && _filter.size() > 0) {
            final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
            // filtering
            for (final Entry<?, ?> entry : _filter.entrySet()) {
                final String fieldName = (String) entry.getKey();
                final Field field = command.getTargetTable().getField(fieldName);
                if (!field.isFilterPickList()) {
                    String attrName = field.getAttribute();
                    String[] attrNames = null;
                    if (field.getFilterAttributes() != null) {
                        if (field.getFilterAttributes().contains(",")) {
                            attrNames = field.getFilterAttributes().split(",");
                        } else {
                            attrName = field.getFilterAttributes();
                        }
                    }
                    if (attrNames != null) {
                        exec = addInsideRangeFilter(entry, _queryBldr, _type, attrNames, field);
                    } else {
                        exec = addFilter(entry, _queryBldr, _type, attrName, field);
                    }
                    if (!exec) {
                        break;
                    }
                }
            }
        }
        return exec;
    }

    /**
     * Method to add a Filter for a inside Range of two attributes.
     *
     * @param _entry entry to be evaluated
     * @param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @param _attrNames names of the attributes
     * @param _field field the filter belongs to
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addInsideRangeFilter(final Entry<?, ?> _entry,
                                           final QueryBuilder _queryBldr,
                                           final Type _type,
                                           final String[] _attrNames,
                                           final Field _field)
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
        final Map<?, ?> inner = (Map<?, ?>) _entry.getValue();
        final String from = (String) inner.get("from");
        final String to = (String) inner.get("to");
        if ((from == null || to == null) && _field.getFilterDefault() == null) {
            ret = false;
        } else {
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if ((from == null) || (to == null)) {
                    final DateTime[] dates = getFromTo(_field);
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
     * @param _entry entry to be evaluated
     *@param _queryBldr QueryBuilder used to get the instances
     * @param _type type for the query
     * @param _attrName name of the attribute
     * @param _field field the filter belongs to
     * @return true if the query must be executed, else false
     * @throws EFapsException on error
     */
    protected boolean addFilter(final Entry<?, ?> _entry,
                                final QueryBuilder _queryBldr,
                                final Type _type,
                                final String _attrName,
                                final Field _field)
        throws EFapsException
    {
        boolean ret = true;
        final Attribute attr = _type.getAttribute(_attrName);
        final UUID attrTypeUUId = attr.getAttributeType().getUUID();
        final Map<?, ?> inner = (Map<?, ?>) _entry.getValue();
        final String from = (String) inner.get("from");
        final String to = (String) inner.get("to");
        if ((from == null || to == null) && _field.getFilterDefault() == null) {
            ret = false;
        } else {
            // Date or DateTime
            if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)) {
                DateTime dateFrom = null;
                DateTime dateTo = null;
                if (from == null || to == null) {
                    final DateTime[] dates = getFromTo(_field);
                    dateFrom = dates[0];
                    dateTo = dates[1];
                } else {
                    dateFrom = DateTimeUtil.translateFromUI(from).minusSeconds(1);
                    dateTo = DateTimeUtil.translateFromUI(to).plusDays(1);
                }
                _queryBldr.addWhereAttrGreaterValue(_attrName, dateFrom);
                _queryBldr.addWhereAttrLessValue(_attrName, dateTo);
            }
        }
        return ret;
    }

    /**
     * @param _field Field the date is wanted for
     * @return datetime array
     */
    protected DateTime[] getFromTo(final Field _field)
    {
        final String filter = _field.getFilterDefault();
        final String[] parts = filter.split(":");
        final String range = parts[0];
        final int sub = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        DateTime dateFrom = new DateTime();
        DateTime dateTo = new DateTime();
        if ("today".equalsIgnoreCase(range)) {
            final DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
            dateFrom = tmp.toDateTime().minusDays(sub).minusMinutes(1);
            dateTo = dateFrom.plusDays(1).plusSeconds(1);
        } else if ("week".equalsIgnoreCase(range)) {
            DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
            tmp = tmp.minusDays(tmp.getDayOfWeek() - 1);
            dateFrom = tmp.toDateTime().minusWeeks(sub).minusMinutes(1);
            dateTo = tmp.toDateTime().plusWeeks(1);
        } else if ("month".equalsIgnoreCase(range)) {
            DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
            tmp = tmp.minusDays(tmp.getDayOfMonth() - 1);
            dateFrom = tmp.toDateTime().minusMonths(sub).minusMinutes(1);
            dateTo = tmp.toDateTime().plusMonths(1);
        } else if ("year".equalsIgnoreCase(range)) {
            DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
            tmp = tmp.minusDays(tmp.getDayOfYear() - 1);
            dateFrom = tmp.toDateTime().minusYears(sub).minusMinutes(1);
            dateTo = tmp.toDateTime().plusYears(1);
        }
        return new DateTime[] { dateFrom, dateTo };
    }
}
