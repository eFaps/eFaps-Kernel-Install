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

import org.efaps.admin.program.esjp.EFapsRevision;
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
@EFapsRevision("$Rev$")
public class DateTimeMonth
    extends AbstractDateTime
{
    /**
     * for static access.
     */
    private static final DateTimeMonth DATATYPE = new DateTimeMonth();

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected DateTimeFormatter getFormatter(final Locale _locale)
    {
        return DateTimeFormat.forPattern("MMM").withLocale(_locale);
    }

    /**
     * @return the datatype instance
     */
    public static DateTimeMonth get()
    {
        return DateTimeMonth.DATATYPE;
    }
}