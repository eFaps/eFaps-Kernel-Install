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

package org.efaps.esjp.common.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.ExtendedMessageFormat;
import org.apache.commons.lang3.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.efaps.util.MsgFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("689bf0c2-b5fb-466d-bd28-e6bbba09072b")
@EFapsApplication("eFaps-Kernel")
@EFapsFormatFactory(name = "eval")
public class EvalFormatFcty
    implements FormatFactory
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EvalFormatFcty.class);

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
        if (EvalFormatFcty.FORMATS.containsKey(key)) {
            ret = EvalFormatFcty.FORMATS.get(key);
        } else {
            ret = new EvalFormat(_arguments, _locale);
            EvalFormatFcty.FORMATS.put(key, ret);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return "EvalFormat";
    }

    /**
     * The actual format class.
     */
    private static class EvalFormat
        extends Format
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The pattern. */
        private final String pattern;

        /** The locale. */
        private final Locale locale;

        /** The criteria. */
        private String criteria;

        /**
         * Instantiates a new left format.
         *
         * @param _pattern the _pattern
         * @param _locale the _locale
         */
        EvalFormat(final String _pattern,
                   final Locale _locale)
        {
            final String[] patternTmp = _pattern.split("\\|");
            if (patternTmp.length == 1) {
                this.criteria = null;
                this.pattern = StringEscapeUtils.unescapeJava(patternTmp[0]);
            } else {
                this.criteria = StringEscapeUtils.unescapeJava(patternTmp[0]);
                this.pattern = StringEscapeUtils.unescapeJava(patternTmp[1]);
            }
            this.locale = _locale;
        }

        @Override
        public StringBuffer format(final Object _obj,
                                   final StringBuffer _toAppendTo,
                                   final FieldPosition _pos)
        {
            if (_obj != null && StringUtils.isNotEmpty(String.valueOf(_obj))
                            && (this.criteria == null || this.criteria.equals(String.valueOf(_obj)))) {
                try {
                    final ExtendedMessageFormat format = MsgFormat.getFormat(this.pattern, this.locale);
                    _toAppendTo.append(format.format(new Object[] { _obj }));
                } catch (final EFapsException e) {
                    LOG.error("Catched EFapsException", e);
                }
            }
            return _toAppendTo;
        }

        @Override
        public Object parseObject(final String _source,
                                  final ParsePosition _pos)
        {
            throw new UnsupportedOperationException();
        }
    }
}
