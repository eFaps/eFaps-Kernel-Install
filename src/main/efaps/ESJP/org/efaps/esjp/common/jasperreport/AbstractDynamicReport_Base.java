/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXhtmlExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.constant.SizeUnit;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("599905c7-373a-4c9c-928f-2cf2714d30b3")
@EFapsRevision("$Rev$")
public abstract class AbstractDynamicReport_Base
{

    /**
     * Logging instance used to give logging information of this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractDynamicReport_Base.class);

    /**
     * Reportbuilder this class is based on.
     */
    private final JasperReportBuilder report = DynamicReports.report();


    private String fileName;

    /**
     * Get the style for the columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    protected void configure4Html(final Parameter _parameter)
        throws EFapsException
    {
        getReport().setColumnTitleStyle(getColumnTitleStyle4Html(_parameter))
            .setColumnStyle(getColumnStyle4Html(_parameter))
                        .setIgnorePageWidth(true).setIgnorePagination(true).setHighlightDetailEvenRows(true);
    }

    /**
     * Get the style for the columns in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    protected void configure4Excel(final Parameter _parameter)
        throws EFapsException
    {
        getReport().setColumnTitleStyle(getColumnTitleStyle4Excel(_parameter))
            .setColumnStyle(getColumnStyle4Excel(_parameter)).ignorePageWidth().ignorePagination();
    }

    /**
     * Get the style for the columns in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    protected void configure4Pdf(final Parameter _parameter,
                                 final JasperReportBuilder _builder)
        throws EFapsException
    {
        _builder.setColumnTitleStyle(getColumnTitleStyle4Pdf(_parameter))
            .setColumnStyle(getColumnStyle4Pdf(_parameter))
                        .setIgnorePageWidth(false).setIgnorePagination(false).setHighlightDetailEvenRows(true);
    }

    /**
     * Get the style for the columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get the style for the columns in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style();
    }

    /**
     * Get the style for the columns in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(DynamicReports.stl.padding(2))
                        .setLeftBorder(DynamicReports.stl.pen1Point())
                        .setRightBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get the style for the title columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnTitleStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style()
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold().setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get the style for the title columns in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnTitleStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(2)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold();
    }

    /**
     * Get the style for the title columns in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnTitleStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style()
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold().setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return a JRDataSource
     * @throws EFapsException on error
     */
    protected abstract JRDataSource createDataSource(final Parameter _parameter)
        throws EFapsException;

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _builder Report builder
     * @throws EFapsException on error
     */
    protected abstract void addColumnDefintion(final Parameter _parameter,
                                               final JasperReportBuilder _builder)
        throws EFapsException;

    /**
     * Getter method for the instance variable {@link #report}.
     *
     * @return value of instance variable {@link #report}
     */
    public JasperReportBuilder getReport()
    {
        return this.report;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    public String getHtml(final Parameter _parameter) throws EFapsException
    {
        return getHtml(_parameter, false);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _strip     strip the body tags, so that onlty the table remains
     * @return html document as String
     * @throws EFapsException on error
     */
    public File getPDF(final Parameter _parameter)
        throws EFapsException
    {
        File file = null;
        try {
            final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final String template = String.valueOf(properties.get("Template"));

            final JasperReportBuilder subreport = DynamicReports.report();
            configure4Pdf(_parameter, subreport);
            addColumnDefintion(_parameter, subreport);

            final SubreportBuilder sub = DynamicReports.cmp.subreport(subreport);
            sub.setDataSource(createDataSource(_parameter));

            getReport().detail(sub);
            file = new FileUtil().getFile(getFileName() == null ? "PDF" : getFileName(), "pdf");
            final JasperPdfExporterBuilder exporter = Exporters.pdfExporter(file);

            final InputStream in = getTemplate(_parameter, template);
            getReport().setTemplateDesign(in);

            final EFapsDataSource ds = new EFapsDataSource();
            ds.init(getReport().toJasperReport(), _parameter, null, null);
            getReport().setLocale(Context.getThreadContext().getLocale()).setDataSource(ds).toPdf(exporter);
        } catch (final DRException e) {
            AbstractDynamicReport_Base.LOG.error("catched DRException", e);
        }
        return file;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _strip     strip the body tags, so that onlty the table remains
     * @return html document as String
     * @throws EFapsException on error
     */
    public File getExcel(final Parameter _parameter)
        throws EFapsException
    {
        File file = null;
        try {
            file = new FileUtil().getFile(getFileName() == null ? "XLS" : getFileName(), "xls");
            addColumnDefintion(_parameter, getReport());
            final JasperXlsExporterBuilder exporter = Exporters.xlsExporter(file);
            exporter.setIgnorePageMargins(true)
                    .setDetectCellType(true)
                    .setIgnoreCellBackground(true)
                    .setWhitePageBackground(false)
                    .setRemoveEmptySpaceBetweenColumns(true);
            configure4Excel(_parameter);
            getReport().setLocale(Context.getThreadContext().getLocale())
                .setDataSource(createDataSource(_parameter)).toXls(exporter);
        } catch (final DRException e) {
            AbstractDynamicReport_Base.LOG.error("catched DRException", e);
        }
        return file;
    }

    /**
     * @return filename
     */
    public String getFileName()
    {
        return this.fileName;
    }

    /**
     * Setter method for instance variable {@link #fileName}.
     *
     * @param fileName value for instance variable {@link #fileName}
     */

    public void setFileName(final String _fileName)
    {
        this.fileName = _fileName;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _strip     strip the body tags, so that onlty the table remains
     * @return html document as String
     * @throws EFapsException on error
     */
    public String getHtml(final Parameter _parameter,
                          final boolean _strip)
        throws EFapsException
    {
        final Writer writer = new StringWriter();
        try {
            addColumnDefintion(_parameter, getReport());
            final JasperXhtmlExporterBuilder exporter = Exporters.xhtmlExporter(writer);
            if (_strip) {
                exporter.setHtmlHeader("").setHtmlFooter("");
            }
            exporter.setIgnorePageMargins(true).setSizeUnit(SizeUnit.PIXEL);
            configure4Html(_parameter);
            getReport().setLocale(Context.getThreadContext().getLocale())
                .setDataSource(createDataSource(_parameter)).toXhtml(exporter);
        } catch (final DRException e) {
            AbstractDynamicReport_Base.LOG.error("catched DRException", e);
        }
        return writer.toString();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _strip strip the body tags, so that onlty the table remains
     * @return html document as String
     * @throws EFapsException on error
     */
    public String getHtmlSnipplet(final Parameter _parameter)
        throws EFapsException
    {
       return getHtml(_parameter, true);
    }

    public InputStream getTemplate(final Parameter _parameter,
                                   final String _jasperFileName)
    {
        AbstractDynamicReport_Base.LOG.debug("getting Template '{}'", _jasperFileName);
        InputStream ret = null;
        try {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReport);
            ;
            queryBldr.addWhereAttrEqValue(CIAdminProgram.JasperReport.Name, _jasperFileName);
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            if (query.next()) {
                final Checkout checkout = new Checkout(query.getCurrentValue());
                ret = checkout.execute();
            }
        } catch (final EFapsException e) {
            AbstractDynamicReport_Base.LOG.error("error in getTemplate", e);
        }
        return ret;
    }
}
