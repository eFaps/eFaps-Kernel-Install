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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.esjp.common.jasperreport;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperReport;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * Interface for DataSources use dby the StandartReport.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("bdbde7bc-7a28-4b71-a77f-ec70662fa7c5")
@EFapsApplication("eFaps-Kernel")
public interface IeFapsDataSource
    extends JRDataSource
{
    /**
     * Executed on initialization of the DataSource.
     *
     * @param _jasperReport JasperReport this DataSource belongs to
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _parentSource parent DataSource in case that this DataSource
     *                      belongs to a subreport
     * @param _jrParameters map that contains the report parameters
     * @throws EFapsException on error
     */
    void init(final JasperReport _jasperReport,
              final Parameter _parameter,
              final JRDataSource _parentSource,
              final Map<String, Object> _jrParameters)
        throws EFapsException;
}
