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
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("9bce3015-6c4c-4aba-b581-73eb726f4c71")
@EFapsApplication("eFaps-Kernel")
public class DateTimeDate
    extends AbstractDateTime
{

    /**
     * for static access.
     */
    private static final Map<String, DateTimeDate> DATATYPES = new HashMap<>();

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new date time month.
     *
     * @param _pattern the _pattern
     */
    private DateTimeDate(final String _pattern)
    {
        super(_pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DateTimeFormatter getFormatter(final Locale _locale)
    {
        DateTimeFormatter ret;
        if (getPattern().matches("^[SMLF-][SMLF-]$")) {
            ret = DateTimeFormat.forStyle(getPattern()).withLocale(_locale);
        } else {
            ret = DateTimeFormat.forPattern(getPattern()).withLocale(_locale);
        }
        return ret;
    }

    /**
     * @return the datatype instance
     */
    public static DateTimeDate get()
        throws EFapsException
    {
        return DateTimeDate.get(KernelConfigurations.JASPER_DATEFORMAT.get());
    }

    /**
     * Gets the.
     *
     * @param _pattern the _pattern
     * @return the datatype instance
     */
    public static DateTimeDate get(final String _pattern)
    {
        DateTimeDate ret;
        if (DATATYPES.containsKey(_pattern)) {
            ret = DATATYPES.get(_pattern);
        } else {
            ret = new DateTimeDate(_pattern);
            DATATYPES.put(_pattern, ret);
        }
        return ret;
    }
}
