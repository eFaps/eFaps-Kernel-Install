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


package org.efaps.esjp.admin.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.EnumUtils;
import org.apache.lucene.document.DateTools;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.Indexer;
import org.efaps.admin.index.Indexer.Dimension;
import org.efaps.json.index.result.DimValue;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class LucenceQueryBuilder_Base
{

    /**
     * Gets the query for dim values.
     *
     * @param _baseQuery the base query
     * @param _included the included Dimension Values
     * @param _excluded the excluded Dimension Values
     * @return the query including dimension values
     */
    public String getQuery4DimValues(final String _baseQuery,
                                     final List<DimValue> _included,
                                     final List<DimValue> _excluded)
    {
        final Map<String, List<DimValue>> dim2included = getDimMap(_included);
        final Map<String, List<DimValue>> dim2excluded = getDimMap(_excluded);

        final StringBuilder ret = new StringBuilder()
                        .append(_baseQuery).append(" ");
        if (!dim2excluded.isEmpty()) {
            boolean first = true;
            for (final Entry<String, List<DimValue>> entry : dim2excluded.entrySet()) {
                for (final DimValue dimValue : entry.getValue()) {
                    if (first) {
                        first = false;
                    } else {
                        ret.append(" AND ");
                    }
                    ret.append(" -").append(getCriteria(dimValue));
                }
            }
        }

        for (final Entry<String, List<DimValue>> entry : dim2included.entrySet()) {
            ret.append(" (");
            boolean first = true;
            for (final DimValue dimVal : entry.getValue()) {
                if (first) {
                    first = false;
                } else {
                    ret.append(" OR ");
                }
                ret.append(getCriteria(dimVal));
            }
            ret.append(")");
        }
        return ret.toString();
    }

    /**
     * Gets the criteria.
     *
     * @param _dimValue the _dim value
     * @return the criteria
     */
    private StringBuilder getCriteria(final DimValue _dimValue)
    {
        final StringBuilder ret = new StringBuilder();
        final Dimension dim = EnumUtils.<Indexer.Dimension>getEnum(Indexer.Dimension.class, _dimValue.getPath()[0]);
        switch (dim) {
            case DIMCREATED:
                final DateTime startDate;
                final DateTime endDate;
                // year is selected
                if (_dimValue.getPath().length == 1) {
                    startDate = new DateTime().withYear(Integer.parseInt(_dimValue.getLabel()))
                                    .withDayOfYear(1);
                     endDate = startDate.plusYears(1).minusDays(1);
                } else {
                    startDate = new DateTime().withYear(Integer.parseInt(_dimValue.getPath()[1]))
                                    .withMonthOfYear(Integer.parseInt(_dimValue.getLabel()))
                                    .withDayOfMonth(1);
                    endDate = startDate.plusMonths(1).minusDays(1);
                }
                ret.append(Indexer.Key.CREATEDSTR).append(":[")
                        .append(DateTools.dateToString(startDate.toDate(), DateTools.Resolution.DAY))
                        .append(" TO ")
                        .append(DateTools.dateToString(endDate.toDate(), DateTools.Resolution.DAY))
                        .append("]");
                break;
            case DIMTYPE:
                ret.append(DBProperties.getProperty("index.Type")).append(":\"").append(_dimValue.getLabel())
                                .append("\"");
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * Gets the dim map.
     *
     * @param _dimValues the dim values
     * @return the dim map
     */
    private Map<String, List<DimValue>> getDimMap(final List<DimValue> _dimValues)
    {
        final Map<String, List<DimValue>> ret = new HashMap<>();
        for (final DimValue dimValue : _dimValues) {
            List<DimValue> list;
            if (ret.containsKey(dimValue.getPath()[0])) {
                list = ret.get(dimValue.getPath()[0]);
            } else {
                list = new ArrayList<>();
                ret.put(dimValue.getPath()[0], list);
            }
            list.add(dimValue);
        }
        return ret;
    }
}
