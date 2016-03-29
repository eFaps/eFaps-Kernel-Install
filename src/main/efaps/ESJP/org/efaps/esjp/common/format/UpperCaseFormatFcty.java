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
import java.util.Locale;

import org.apache.commons.lang3.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("251b0699-fea9-48c1-9562-5bfeff649c4c")
@EFapsApplication("eFaps-Kernel")
@EFapsFormatFactory(name = "upper")
public class UpperCaseFormatFcty
    implements FormatFactory
{

    /**
     * static Format instance.
     */
    private static final Format UPPER_INSTANCE = new UpperCaseFormat();

    @Override
    public Format getFormat(final String _name,
                            final String _arguments,
                            final Locale _locale)
    {
        return UPPER_INSTANCE;
    }

    @Override
    public String toString()
    {
        return "UpperCaseFormat";
    }

    /**
     * The actual format class.
     */
    private static class UpperCaseFormat
        extends Format
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(final Object _obj,
                                   final StringBuffer _toAppendTo,
                                   final FieldPosition _pos)
        {
            return _toAppendTo.append(String.valueOf(_obj).toUpperCase());
        }

        @Override
        public Object parseObject(final String _source,
                                  final ParsePosition _pos)
        {
            throw new UnsupportedOperationException();
        }
    }
}
