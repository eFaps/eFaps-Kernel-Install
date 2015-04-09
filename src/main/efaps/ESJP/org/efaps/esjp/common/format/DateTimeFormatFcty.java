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

package org.efaps.esjp.common.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
@EFapsUUID("4724d655-3070-41a1-9ba3-1b7b8d02d4a3")
@EFapsApplication("eFaps-Kernel")
@EFapsFormatFactory(name = "datetime")
public class DateTimeFormatFcty
    implements FormatFactory
{

    /**
     * static Format instance.
     */
    private static final Map<String, Format> FORMATS = new HashMap<>();

    @Override
    public Format getFormat(final String _name,
                            final String _arguments,
                            final Locale _locale)
    {
        Format ret;
        final String key = _locale.toLanguageTag() + "_" + _arguments;
        if (DateTimeFormatFcty.FORMATS.containsKey(key)) {
            ret = DateTimeFormatFcty.FORMATS.get(key);
        } else {
            ret = new DateTimeFormat(_arguments, _locale);
            DateTimeFormatFcty.FORMATS.put(key, ret);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return "DateTimeFormat";
    }

    /**
     * The actual format class.
     */
    private static class DateTimeFormat
        extends Format
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * Pattern for this format,
         */
        private final String pattern;

        /**
         * Locale for this format.
         */
        private final Locale locale;

        /**
         * @param _arguments arguments
         * @param _locale Locale
         */
        public DateTimeFormat(final String _pattern,
                              final Locale _locale)
        {
            this.pattern = _pattern;
            this.locale = _locale;
        }

        @Override
        public StringBuffer format(final Object _obj,
                                   final StringBuffer _toAppendTo,
                                   final FieldPosition _pos)
        {
            return _toAppendTo.append(((DateTime) _obj).toString(this.pattern, this.locale));
        }

        @Override
        public Object parseObject(final String _source,
                                  final ParsePosition _pos)
        {
            throw new UnsupportedOperationException();
        }
    }
}
