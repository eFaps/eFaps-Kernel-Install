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
package org.efaps.esjp.common.datetime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.DateDefaultValues;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.esjp.common.properties.PropertiesUtil;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;

@EFapsUUID("460c9222-dd48-47b3-8718-a853a863dd4e")
@EFapsApplication("eFaps-Kernel")
public class DateAndTimeUtils
{
    public static OffsetDateTime getDefaultValue(final Parameter _parameter,
                                           final Properties _props)
        throws EFapsException
    {
        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.PROPERTIES, _props);
        return getDefaultValue(parameter);
    }

    /**
     * @param _parameter parameter as passed by the eFaps API
     * @return new DateTime
     * @throws EFapsException on error
     */
    public static OffsetDateTime getDefaultValue(final Parameter _parameter)
        throws EFapsException
    {
        OffsetDateTime ret = OffsetDateTime.now(DateTimeUtil.getDBZoneId())
                        .truncatedTo(ChronoUnit.DAYS);
        for (final DateDefaultValues value : DateDefaultValues.values()) {
            if (PropertiesUtil.containsProperty(_parameter, value.toString())) {
                final String strValue = PropertiesUtil.getProperty(_parameter, value.toString());
                switch (value) {
                    case TODAY:
                        ret = OffsetDateTime.now(DateTimeUtil.getDBZoneId()).truncatedTo(ChronoUnit.DAYS);
                        break;
                    case WEEKS:
                        ret = ret.plusWeeks(Integer.valueOf(strValue));
                        break;
                    case MONTHS:
                        ret = ret.plusMonths(Integer.valueOf(strValue));
                        break;
                    case YEARS:
                        ret = ret.plusYears(Integer.valueOf(strValue));
                        break;
                    case WITHDAYOFMONTH:
                        ret = ret.withDayOfMonth(Integer.valueOf(strValue));
                        break;
                    case WITHDAYOFWEEK:
                        ret = ret.with(DayOfWeek.of(Integer.valueOf(strValue)));
                        break;
                    case LASTDAYOFMONTH:
                        ret = ret.withDayOfMonth(ret.getMonth().maxLength());
                        break;
                    case WITHDAYOFYEAR:
                        ret = ret.withDayOfYear(Integer.valueOf(strValue));
                    default:
                        break;
                }
            }
        }
        return ret;
    }

    public static OffsetDateTime getDateForQuery(final String _isoDateTime)
        throws EFapsException
    {
        final LocalDate localDate = LocalDate.parse(_isoDateTime.subSequence(0, 10));
        return OffsetDateTime.now(DateTimeUtil.getDBZoneId())
            .withYear(localDate.getYear())
            .withMonth(localDate.getMonthValue())
            .withDayOfMonth(localDate.getDayOfMonth())
            .truncatedTo(ChronoUnit.DAYS);
    }
}
