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

package org.efaps.esjp.common.jasperreport;

import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("da0d7ba7-8969-4436-be97-8ab65dfb8e26")
@EFapsRevision("$Rev$")
public class ReportRunner
    implements Runnable
{

    private final JasperReport jasperReport;
    private final Map<String, Object> jrParameters;
    private final IeFapsDataSource dataSource;
    private final LocalJasperReportsContext ctx;
    private JasperPrint jasperPrint;

    /**
     * @param _ctx
     * @param _jasperReport
     * @param _jrParameters
     * @param _dataSource
     */
    public ReportRunner(final LocalJasperReportsContext _ctx,
                        final JasperReport _jasperReport,
                        final Map<String, Object> _jrParameters,
                        final IeFapsDataSource _dataSource)
    {
        this.ctx = _ctx;
        this.jasperReport = _jasperReport;
        this.jrParameters = _jrParameters;
        this.dataSource = _dataSource;
    }

    @Override
    public void run()
    {
        final JasperFillManager fillmgr = JasperFillManager.getInstance(this.ctx);
        try {
            this.jasperPrint = fillmgr.fill(this.jasperReport, this.jrParameters, this.dataSource);
        } catch (final JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Getter method for the instance variable {@link #jasperPrint}.
     *
     * @return value of instance variable {@link #jasperPrint}
     */
    public JasperPrint getJasperPrint()
    {
        return this.jasperPrint;
    }
}
