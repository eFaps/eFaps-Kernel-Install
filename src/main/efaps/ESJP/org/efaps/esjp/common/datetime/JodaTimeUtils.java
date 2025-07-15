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
package org.efaps.esjp.common.datetime;

import java.time.OffsetDateTime;
import java.util.Properties;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.DateDefaultValues;
import org.efaps.db.Context;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.util.EFapsException;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.OffsetDateTimeField;
import org.joda.time.field.RemainderDateTimeField;
import org.joda.time.field.ScaledDurationField;
import org.joda.time.format.DateTimeFormat;

/**
 * Joda extensions for business cases.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1c2c5f34-9ac3-486b-9b6e-0d762ec06459")
@EFapsApplication("eFaps-Kernel")
public final class JodaTimeUtils
    extends AbstractCommon
{

    /** The Constant QUARTERS. */
    private static final DurationFieldType QUARTERS = new DurationFieldType("quarters")
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.months(), QUARTERS, 3);
        }
    };

    /** The Constant HALFYEARS. */
    private static final DurationFieldType HALFYEARS = new DurationFieldType("halfyear")
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.months(), HALFYEARS, 6);
        }
    };

    /** The Constant DECADES. */
    private static final DurationFieldType DECADES = new DurationFieldType("decades")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.years(), DECADES, 10);
        }
    };

    /** The Constant CENTURIES. */
    private static final DurationFieldType CENTURIES = new DurationFieldType("centuries")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.years(), CENTURIES, 100);
        }
    };

    /** The Constant QUARTEROFYEAR. */
    // DateTimeFields
    private static final DateTimeFieldType QUARTEROFYEAR = new DateTimeFieldType("quarterOfYear")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationFieldType getDurationType()
        {
            return QUARTERS;
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return DurationFieldType.years();
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new OffsetDateTimeField(new DividedDateTimeField(new OffsetDateTimeField(_chronology.monthOfYear(),
                            -1), QUARTEROFYEAR, 3), 1);
        }
    };

    /** The Constant HALFYEAROFYEAR. */
    private static final DateTimeFieldType HALFYEAROFYEAR = new DateTimeFieldType("halfYearOfYear")
    {

        private static final long serialVersionUID = -5677872459807379123L;

        @Override
        public DurationFieldType getDurationType()
        {
            return HALFYEARS;
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return DurationFieldType.years();
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new OffsetDateTimeField(new DividedDateTimeField(new OffsetDateTimeField(_chronology.monthOfYear(),
                            -1), HALFYEAROFYEAR, 6), 1);
        }
    };

    /** The Constant MONTHOFQUARTER. */
    private static final DateTimeFieldType MONTHOFQUARTER = new DateTimeFieldType("monthOfQuarter")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationFieldType getDurationType()
        {
            return DurationFieldType.months();
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return QUARTERS;
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new OffsetDateTimeField(new RemainderDateTimeField(new OffsetDateTimeField(_chronology.monthOfYear(),
                            -1), MONTHOFQUARTER, 3), 1);
        }
    };

    /** The Constant MONTHOFHALFYEAR. */
    private static final DateTimeFieldType MONTHOFHALFYEAR = new DateTimeFieldType("monthOfHalfYear")
    {

        private static final long serialVersionUID = -5677872459807379123L;

        @Override
        public DurationFieldType getDurationType()
        {
            return DurationFieldType.months();
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return HALFYEARS;
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new OffsetDateTimeField(new RemainderDateTimeField(new OffsetDateTimeField(_chronology.monthOfYear(),
                            -1), MONTHOFHALFYEAR, 6), 1);
        }
    };

    /** The Constant WEEKOFMONTH. */
    private static final DateTimeFieldType WEEKOFMONTH = new DateTimeFieldType("weekOfMonth")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationFieldType getDurationType()
        {
            return DurationFieldType.weeks();
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return DurationFieldType.months();
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new OffsetDateTimeField(new RemainderDateTimeField(new OffsetDateTimeField(
                            _chronology.weekOfWeekyear(), -1), WEEKOFMONTH, 4), 1);
        }
    };

    /** The Constant DECADEOFCENTURY. */
    private static final DateTimeFieldType DECADEOFCENTURY = new DateTimeFieldType("decadeOfCentury")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationFieldType getDurationType()
        {
            return DECADES;
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return DurationFieldType.centuries();
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new DividedDateTimeField(_chronology.yearOfCentury(), DECADEOFCENTURY, 10);
        }
    };

    /** The Constant YEAROFDECADE. */
    private static final DateTimeFieldType YEAROFDECADE = new DateTimeFieldType("yearOfDecade")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationFieldType getDurationType()
        {
            return DurationFieldType.years();
        }

        @Override
        public DurationFieldType getRangeDurationType()
        {
            return DECADES;
        }

        @Override
        public DateTimeField getField(final Chronology _chronology)
        {
            return new DividedDateTimeField(_chronology.yearOfCentury(), YEAROFDECADE, 10);
        }
    };

    /**
     * Singelton wanted.
     */
    private JodaTimeUtils()
    {
    }

    /**
     * Get the halfyears field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DurationFieldType halfYears()
    {
        return HALFYEARS;
    }

    /**
     * Get the quarters field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DurationFieldType quarters()
    {
        return QUARTERS;
    }

    /**
     * Get the quarters field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DurationFieldType decades()
    {
        return DECADES;
    }

    /**
     * Get the quarters field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DurationFieldType centuries()
    {
        return CENTURIES;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType decadeOfCentury()
    {
        return DECADEOFCENTURY;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType quarterOfYear()
    {
        return QUARTEROFYEAR;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType century()
    {
        return MONTHOFHALFYEAR;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType halfYearOfYear()
    {
        return HALFYEAROFYEAR;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType monthOfQuarter()
    {
        return MONTHOFQUARTER;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType weekOfMonth()
    {
        return WEEKOFMONTH;
    }

    /**
     * Get the year field type.
     *
     * @return the DateTimeFieldType constant
     */
    public static DateTimeFieldType yearOfDecade()
    {
        return YEAROFDECADE;
    }

    /**
     * @param _parameter parameter as passed by the eFaps API
     * @param _props porerties with the defitintion
     * @return new DateTime
     * @throws EFapsException on error
     *
     */
    public static DateTime getDefaultvalue(final Parameter _parameter,
                                           final Properties _props)
        throws EFapsException
    {
        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.PROPERTIES, _props);
        return getDefaultvalue(parameter);
    }

    /**
     * @param _parameter parameter as passed by the eFaps API
     * @return new DateTime
     * @throws EFapsException on error
     */
    public static DateTime getDefaultvalue(final Parameter _parameter)
        throws EFapsException
    {
        final JodaTimeUtils utils = new JodaTimeUtils();
        DateTime ret = new DateTime().withTimeAtStartOfDay().withChronology(Context.getThreadContext().getChronology());
        for (final DateDefaultValues value : DateDefaultValues.values()) {
            if (utils.containsProperty(_parameter, value.toString())) {
                final String strValue = utils.getProperty(_parameter, value.toString());
                switch (value) {
                    case TODAY:
                        ret = new DateTime().withChronology(Context.getThreadContext().getChronology());
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
                        ret = ret.withDayOfWeek(Integer.valueOf(strValue));
                        break;
                    case LASTDAYOFMONTH:
                        ret = ret.dayOfMonth().withMaximumValue();
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

    public static DateTime toDateTime(final OffsetDateTime _original)
    {
        return new DateTime(_original.toInstant().toEpochMilli());
    }

    public static DateTime getDateFromParameter(final String dateStr)
        throws EFapsException
    {
        final var shortFormat = DateTimeFormat.shortDate().withLocale(Context.getThreadContext()
                        .getLocale());
        return shortFormat.parseDateTime(dateStr);
    }
}
