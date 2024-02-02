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
package org.efaps.esjp.common.jasperreport.datatype;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("cdc45ed6-b73c-4dc6-9870-f338997d474c")
@EFapsApplication("eFaps-Kernel")
public final class DateTimeMonth
    extends AbstractDateTime
{

    /**
     * for static access.
     */
    private static final Map<String, DateTimeMonth> DATATYPES = new HashMap<>();

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new date time month.
     *
     * @param _pattern the _pattern
     */
    private DateTimeMonth(final String _pattern)
    {
        super(_pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DateTimeFormatter getFormatter(final Locale _locale)
    {
        return DateTimeFormat.forPattern(getPattern()).withLocale(_locale);
    }

    /**
     * @return the datatype instance
     */
    public static DateTimeMonth get()
    {
        return DateTimeMonth.get("MMM");
    }

    /**
     * Gets the.
     *
     * @param _pattern the _pattern
     * @return the datatype instance
     */
    public static DateTimeMonth get(final String _pattern)
    {
        DateTimeMonth ret;
        if (DATATYPES.containsKey(_pattern)) {
            ret = DATATYPES.get(_pattern);
        } else {
            ret = new DateTimeMonth(_pattern);
            DATATYPES.put(_pattern, ret);
        }
        return ret;
    }
}
