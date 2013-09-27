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
 * Revision:        $Rev: 9255 $
 * Last Changed:    $Date: 2013-04-23 12:19:39 -0500 (mar, 23 abr 2013) $
 * Last Changed By: $Author: jorge.cueva@moxter.net $
 */

package org.efaps.esjp.common.jasperreport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractDynamicReport_Base.java 9255 2013-04-23 17:19:39Z jorge.cueva@moxter.net $
 */
@EFapsUUID("a797b419-5cde-420d-9e6e-7ec0302163cf")
@EFapsRevision("$Rev: 9255 $")
public abstract class EFapsTextReport_Base
{
    protected final static Logger LOG = LoggerFactory.getLogger(EFapsTextReport_Base.class);

    public static enum Type
    {
        /** */
        STRINGTYPE,
        /** */
        NUMBERTYPE,
        /** */
        DATETYPE;
    }

    public Return getTextReport(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final StringBuilder data = buildReport(_parameter);

        try {
            final File file = getEmptyFile4TextReport();
            final PrintWriter writer = new PrintWriter(file);
            writer.print(data.toString());
            writer.close();
            ret.put(ReturnValues.VALUES, file);
            ret.put(ReturnValues.TRUE, true);
        } catch (final IOException e) {
            throw new EFapsException(EFapsTextReport_Base.class, "execute.IOException", e);
        }

        return ret;
    }

    protected StringBuilder buildReport(final Parameter _parameter)
        throws EFapsException
    {
        final List<ColumnDefinition> columnsList = new ArrayList<ColumnDefinition>();
        addColumnDefinition(_parameter, columnsList);
        final List<List<Object>> dataSrcList = createDataSource(_parameter);

        final List<List<Column>> values = new ArrayList<List<Column>>();
        for (final List<Object> row : dataSrcList) {
            Integer col = 0;
            final List<Column> rowValues = new ArrayList<Column>();
            for (final Object colObj : row) {
                final ColumnDefinition colDef = columnsList.get(col);
                final Column colVal = new Column(colObj, colDef);
                rowValues.add(colVal);
                col++;
            }
            values.add(rowValues);
        }

        final List<ColumnDefinition> headColList = new ArrayList<ColumnDefinition>();
        addHeaderDefinition(_parameter, headColList);
        final List<Object> headData = getHeaderData(_parameter);
        final List<Column> headValues = new ArrayList<Column>();
        final boolean hasHead = !headColList.isEmpty() && headData != null;
        if (hasHead) {
            Integer headCol = 0;
            for (final Object colObj : headData) {
                final ColumnDefinition colDef = headColList.get(headCol);
                final Column colVal = new Column(colObj, colDef);
                headValues.add(colVal);
                headCol++;
            }
        }

        final StringBuilder data = new StringBuilder();
        if (hasHead) {
            data.append(buildHeader4TextReport(headValues));
        }
        data.append(buildBody4TextReport(values));

        return data;
    }

    /**
     * The buildName4TextReport method should be called to build the file name
     *
     * @return
     * @throws EFapsException on error
     */
    protected abstract File getEmptyFile4TextReport()
        throws EFapsException;

    /**
     * The buildHeader4TextReport method should be called to build the report header.
     *
     * @return
     * @throws EFapsException on error.
     */
    protected abstract void addHeaderDefinition(Parameter _parameter,
                                                  List<ColumnDefinition> _columnsList)
        throws EFapsException;

    /**
     * @param _parameter
     * @return
     */
    protected abstract List<Object> getHeaderData(Parameter _parameter)
        throws EFapsException;

    /**
     * @param _parameter on error.
     * @param _columnsList
     * @throws EFapsException
     */
    protected abstract void addColumnDefinition(Parameter _parameter,
                                                List<ColumnDefinition> _columnsList)
        throws EFapsException;

    /**
     * @param _parameter
     * @return
     * @throws EFapsException
     */
    protected abstract List<List<Object>> createDataSource(final Parameter _parameter)
        throws EFapsException;

    /**
     * @param _extension
     * @param _values
     * @return
     */
    protected String buildName4TextReport(final String... _values)
    {
        final StringBuilder name = new StringBuilder();
        for (final String value : _values) {
            name.append(value);
        }
        return name.toString();
    }

    /**
     * @param _values
     * @return
     */
    protected String buildHeader4TextReport(final List<Column> _values)
    {
        final StringBuilder head = new StringBuilder();
        for (final Column value : _values) {
            head.append(value.getValue());
        }
        head.append("\r\n");
        return head.toString();
    }

    /**
     * @param _values
     * @return
     */
    protected StringBuilder buildBody4TextReport(final List<List<Column>> _values)
    {
        final StringBuilder rep = new StringBuilder();
        for (final List<Column> cols : _values) {
            for (final Column col : cols) {
                rep.append(col.getValue()).append(col.getColumnDef().getSeparator());
            }
            rep.append("\r\n");
        }
        EFapsTextReport_Base.LOG.debug(rep.toString());

        return rep;
    }

    /**
     * @author jorge
     *
     */
    public class ColumnDefinition
    {
        private final String separator;

        private final Integer length;

        private final Integer decimalLength;

        private final Boolean withDecimalSymbol;

        private final Type type;

        private final String defaultValue;

        private final String formatPattern;

        private final Format formatter;

        public ColumnDefinition(final String _separator,
                                final Integer _length,
                                final Integer _decimalLength,
                                final Boolean _withDecimalSymbol,
                                final Format _formatter,
                                final String _defaultValue,
                                final Type _type,
                                final String _formatPattern)
        {
            this.separator = _separator;
            this.length = _length;
            this.decimalLength = _decimalLength;
            this.withDecimalSymbol = _withDecimalSymbol;
            this.formatter = _formatter;
            this.type = _type;
            this.defaultValue = _defaultValue;
            this.formatPattern = _formatPattern;
        }

        /**
         * @return the separator
         */
        private String getSeparator()
        {
            return separator;
        }

        /**
         * @return the length
         */
        private Integer getLength()
        {
            return length;
        }

        /**
         * @return the decimaLength
         */
        private Integer getDecimalLength()
        {
            return decimalLength;
        }

        /**
         * @return the withDecimalSymbol
         */
        private Boolean isWithDecimalSymbol()
        {
            return withDecimalSymbol;
        }

        /**
         * @return the type
         */
        private Type getType()
        {
            return type;
        }

        /**
         * @return the defaultValue
         */
        private String getDefaultValue()
        {
            return defaultValue;
        }

        /**
         * @return the formatter
         */
        private Format getFormatter()
        {
            return formatter;
        }

        /**
         * @return the fillerPattern
         */
        private String getFormatPattern()
        {
            return formatPattern;
        }

    }

    /**
     * @author jorge
     *
     */
    public class Column
    {
        private final String value;
        private final ColumnDefinition columnDef;

        public Column(final Object _value,
                      final ColumnDefinition _columnDef)
            throws EFapsException
        {
            this.columnDef = _columnDef;
            switch (_columnDef.getType()) {
                case STRINGTYPE:
                    this.value = getCharacterValue(_value, _columnDef);
                    break;
                case NUMBERTYPE:
                    this.value = getNumberValue(_value, _columnDef);
                    break;
                case DATETYPE:
                    this.value = getDateValue(_value, _columnDef);
                    break;
                default:
                    this.value = _columnDef.getDefaultValue();
            }
        }

        /**
         * @return the value
         */
        private String getValue()
        {
            return value;
        }

        /**
         * @return the columnDef
         */
        private ColumnDefinition getColumnDef()
        {
            return columnDef;
        }

        protected String getCharacterValue(final Object _value,
                                           final ColumnDefinition _column)
            throws EFapsException
        {
            String value = _column.getDefaultValue();
            if (_value != null) {
                value = String.valueOf(_value);
            }
            if (value.length() > _column.getLength()) {
                value = value.substring(0, _column.getLength());
            } else if (value.length() < _column.getLength()) {
                final Formatter formatter = new Formatter();
                if (_column.getFormatPattern() != null) {
                    value = formatter.format(_column.getFormatPattern(), value).toString();
                    formatter.close();
                    value = value.replace("\\s", _column.getDefaultValue());
                }
            }
            return value;
        }

        protected String getNumberValue(final Object _value,
                                        final ColumnDefinition _column)
            throws EFapsException
        {
            String valStr = "";
            final Format formatter = _column.getFormatter();
            if (_value != null) {
                if (_value instanceof Integer) {
                    valStr = formatter != null ? formatter.format(new BigDecimal((Integer) _value))
                                                : ((Integer) _value).toString();
                } else if (_value instanceof BigDecimal) {
                    final BigDecimal valTmp = (BigDecimal) _value;
                    if (formatter != null) {
                        valStr = formatter.format(valTmp).toString();
                    }  else {
                        final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Context
                                        .getThreadContext().getLocale());
                        format.setMaximumIntegerDigits(_column.getLength() - _column.getDecimalLength());
                        format.setMaximumFractionDigits(_column.getDecimalLength());
                        format.setMinimumIntegerDigits(_column.getLength() - _column.getDecimalLength());
                        format.setMinimumFractionDigits(_column.getDecimalLength());
                        format.setRoundingMode(RoundingMode.HALF_UP);
                        valStr = format.format(valTmp).toString();
                    }
                } else if (_value instanceof String) {
                    valStr = parseString2Number(_value, _column, formatter);
                }

            } else {
                valStr = formatter.format(new BigDecimal("0"));
            }
            if (!(_value instanceof Integer) && !_column.isWithDecimalSymbol()) {
                if (".".equals(""+((DecimalFormat)formatter).getDecimalFormatSymbols().getDecimalSeparator())) {
                    valStr = valStr.replaceAll("\\.", "");
                } else {
                    valStr = valStr.replaceAll(",", "");
                }
            }
            if (_column.getDefaultValue() != null && !_column.getDefaultValue().equals("0")) {
                valStr.replaceAll("0", _column.getDefaultValue());
            }

            return valStr;
        }

        protected String parseString2Number(final Object _value,
                                            final ColumnDefinition _column,
                                            final Format formatter)
        {
            // TODO Auto-generated method stub
            return null;
        }

        protected String getDateValue(final Object _value,
                                      final ColumnDefinition _column)
        {
            // final Format formatter = new SimpleDateFormat("yyyyMMdd");
            final Format formatter = _column.getFormatter();
            String dateStr = _column.getDefaultValue() != null ? _column.getDefaultValue() : "";
            Date date = null;
            if (_value != null) {
                if (_value instanceof String) {
                    final String value = (String) _value;
                    dateStr = parseString2Date(value, _column, formatter);
                } else if (_value instanceof DateTime) {
                    date = ((DateTime) _value).toDate();
                    dateStr = formatter.format(date);
                } else if (_value instanceof Date) {
                    date = (Date) _value;
                    dateStr = formatter.format(date);
                }
            }
            if (_column.getFormatPattern() != null) {
                final Formatter strFormatTmp = new Formatter();
                dateStr = strFormatTmp.format(_column.getFormatPattern(), dateStr).toString();
                strFormatTmp.close();

                if (_column.getDefaultValue() != null) {
                    dateStr = dateStr.replace("\\s", _column.getDefaultValue());
                }
            }


            if (dateStr.length() > _column.getLength()) {
                dateStr = dateStr.substring(0, _column.getLength());
            }
            return dateStr;
        }

        protected String parseString2Date(final String _value,
                                          final ColumnDefinition _column,
                                          final Format formatter)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
