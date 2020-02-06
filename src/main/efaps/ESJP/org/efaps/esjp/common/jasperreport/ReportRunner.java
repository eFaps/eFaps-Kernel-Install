/*
 * Copyright 2003 - 2020 The eFaps Team
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

package org.efaps.esjp.common.jasperreport;

import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("da0d7ba7-8969-4436-be97-8ab65dfb8e26")
@EFapsApplication("eFaps-Kernel")
public class ReportRunner
    implements Runnable
{
    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReportRunner.class);

    /**
     * Jasperreport to be run.
     */
    private final JasperReport jasperReport;

    /**
     * Parameters map fro jasper.
     */
    private final Map<String, Object> jrParameters;

    /**
     * DataSource for the report.
     */
    private final IeFapsDataSource dataSource;

    /**
     * Context to be used.
     */
    private final JasperReportsContext ctx;

    /**
     * Print to be filled.
     */
    private JasperPrint jasperPrint;

    /**
     * @param _ctx              Context for the report
     * @param _jasperReport     jasperreport to be run
     * @param _jrParameters     parameters
     * @param _dataSource       datasource
     */
    public ReportRunner(final JasperReportsContext _ctx,
                        final JasperReport _jasperReport,
                        final Map<String, Object> _jrParameters,
                        final IeFapsDataSource _dataSource)
    {
        ctx = _ctx;
        jasperReport = _jasperReport;
        jrParameters = _jrParameters;
        dataSource = _dataSource;
    }

    @Override
    public void run()
    {
        try {
            final JasperFillManager fillmgr = JasperFillManager.getInstance(ctx);
            if (dataSource == null) {
                jasperPrint = fillmgr.fill(jasperReport, jrParameters);
            } else {
                jasperPrint = fillmgr.fill(jasperReport, jrParameters, dataSource);
            }
        //CHECKSTYLE:OFF
        } catch (final Exception e) {
        //CHECKSTYLE:ON
            LOG.error("Catched error from runner", e);
        }
    }

    /**
     * Getter method for the instance variable {@link #jasperPrint}.
     *
     * @return value of instance variable {@link #jasperPrint}
     */
    public JasperPrint getJasperPrint()
    {
        return jasperPrint;
    }
}
