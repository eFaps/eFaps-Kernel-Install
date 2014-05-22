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

package org.efaps.esjp.common.jasperreport.datatype;

import java.util.Locale;

import net.sf.dynamicreports.report.base.datatype.AbstractDataType;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.exception.DRException;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractDateTime.java 12838 2014-05-22 18:05:51Z jan@moxter.net
 *          $
 */
@EFapsUUID("fc139d09-0fc7-435f-bf96-843784930a47")
@EFapsRevision("$Rev$")
public abstract class AbstractDateTime
    extends AbstractDataType<DateTime, DateTime>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String valueToString(final DateTime _value,
                                final Locale _locale)
    {
        String ret = null;
        if (_value != null) {
            ret = _value.toString(getFormatter(_locale));
        }
        return ret;
    }

    @Override
    public DateTime stringToValue(final String _value,
                                  final Locale _locale)
        throws DRException
    {
        DateTime ret = null;
        if (_value != null) {
            ret = getFormatter(_locale).parseDateTime(_value);
        }
        return ret;
    }

    /**
     * @param _locale locale to be used
     * @return the formatter to be applied
     */
    protected abstract DateTimeFormatter getFormatter(final Locale _locale);

    @Override
    public DRIValueFormatter<?, ? extends DateTime> getValueFormatter()
    {
        return new DateTimeValueFormatter(this);
    }

    /**
     * Formats the values.
     */
    public static class DateTimeValueFormatter
        extends AbstractValueFormatter<String, DateTime>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * datatype.
         */
        private final AbstractDateTime datatype;

        /**
         * @param _datatype datatype used to format
         */
        public DateTimeValueFormatter(final AbstractDateTime _datatype)
        {
            this.datatype = _datatype;
        }

        @Override
        public String format(final DateTime _value,
                             final ReportParameters _reportParameters)
        {
            return this.datatype.valueToString(_value, _reportParameters.getLocale());
        }
    }
}
