/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.esjp.common.datetime;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.OffsetDateTimeField;
import org.joda.time.field.RemainderDateTimeField;
import org.joda.time.field.ScaledDurationField;

/**
 * Joda extensions for business cases.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1c2c5f34-9ac3-486b-9b6e-0d762ec06459")
@EFapsRevision("$Rev$")
public final class JodaTimeUtils
{

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

    private static final DurationFieldType DECADES = new DurationFieldType("decades")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.years(), DECADES, 10);
        }
    };

    private static final DurationFieldType CENTURIES = new DurationFieldType("centuries")
    {

        private static final long serialVersionUID = 1L;

        @Override
        public DurationField getField(final Chronology _chronology)
        {
            return new ScaledDurationField(_chronology.years(), CENTURIES, 100);
        }
    };

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

}
