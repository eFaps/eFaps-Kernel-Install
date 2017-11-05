/*
 * Copyright 2003 - 2017 The eFaps Team
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("b5db289c-f0ab-4596-bf39-3688e4749cd9")
@EFapsApplication("eFaps-Kernel")
@EFapsFormatFactory(name = "left")
public class LeftFormatFcty
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
        final Format ret;
        final String key = _locale.toLanguageTag() + "_" + _arguments;
        if (LeftFormatFcty.FORMATS.containsKey(key)) {
            ret = LeftFormatFcty.FORMATS.get(key);
        } else {
            ret = new LeftFormat(_arguments);
            LeftFormatFcty.FORMATS.put(key, ret);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return "LeftFormat";
    }

    /**
     * The actual format class.
     */
    private static class LeftFormat
        extends Format
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** The length. */
        private final Integer length;

        /**
         * Instantiates a new left format.
         *
         * @param _arguments the _arguments
         */
        LeftFormat(final String _arguments)
        {
            this.length = Integer.valueOf(_arguments);
        }

        @Override
        public StringBuffer format(final Object _obj,
                                   final StringBuffer _toAppendTo,
                                   final FieldPosition _pos)
        {
            return _toAppendTo.append(StringUtils.left(String.valueOf(_obj), this.length));
        }

        @Override
        public Object parseObject(final String _source,
                                  final ParsePosition _pos)
        {
            throw new UnsupportedOperationException();
        }
    }
}
