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

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.esjp.common.jasperreport.datatype.DateTimeDate;
import org.efaps.esjp.common.jasperreport.datatype.DateTimeExpression;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperHtmlExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.constant.SizeUnit;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("599905c7-373a-4c9c-928f-2cf2714d30b3")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractDynamicReport_Base
    extends AbstractCommon
{
    /**
     * ExportType enum.
     */
    public enum ExportType
    {
        /**
         * PDF, EXCEL, HTML.
         */
        PDF, EXCEL, HTML;
    }

    /**
     * Logging instance used to give logging information of this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractDynamicReport_Base.class);

    /**
     * Reportbuilder this class is based on.
     */
    private JasperReportBuilder report;

    /**
     * Reportbuilder this class is based on.
     */
    private final ReportTemplateBuilder reportTemplate = DynamicReports.template();

    /**
     * Name for the created file.
     */
    private String fileName;

    /**
     * Current ExportType.
     */
    private ExportType exType;

    /**
     * Name of the template to be used. (PDF)
     */
    private String templateName;

    /**
     * Name of the template used as design. (PDF)
     */
    private String templateDesign;

    /**
     * PageOrientation.
     */
    private PageOrientation pageOrientation = PageOrientation.PORTRAIT;

    /**
     * PageType.
     */
    private PageType pageType = PageType.A4;

    /**
     * Include the default header.
     */
    private boolean includeHeader = false;

    /**
     * Include the default footer.
     */
    private boolean includeFooter = false;

    /**
     * Parameters.
     */
    private Map<String, Object> parameters = new HashMap<>();;

    /**
     * Get the style for the columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     */
    protected void configure4Html(final Parameter _parameter)
        throws EFapsException
    {
        getStyleTemplate().setColumnTitleStyle(getColumnTitleStyle4Html(_parameter))
            .setColumnStyle(getColumnStyle4Html(_parameter))
            .setGroupStyle(getGroupStyle4Html(_parameter))
            .setGroupTitleStyle(getGroupStyle4Html(_parameter))
            .setSubtotalStyle(getSubtotalStyle4Html(_parameter))
            .setCrosstabGroupStyle(getCrossTabGroupStyle4Html(_parameter))
            .setCrosstabCellStyle(getCrossTabCellStyle4Html(_parameter))
            .setCrosstabGroupTotalStyle(getCrossTabGroupTotalStyle4Html(_parameter))
            .setCrosstabGrandTotalStyle(getCrossTabGrandTotalStyle4Html(_parameter))
            .setIgnorePageWidth(true)
            .setIgnorePagination(true)
            .setCrosstabHighlightEvenRows(true)
            .setHighlightDetailOddRows(true);
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
        getStyleTemplate().setColumnTitleStyle(getColumnTitleStyle4Excel(_parameter))
            .setColumnStyle(getColumnStyle4Excel(_parameter))
            .setGroupStyle(getGroupStyle4Excel(_parameter))
            .setGroupTitleStyle(getGroupStyle4Excel(_parameter))
            .setSubtotalStyle(getSubtotalStyle4Excel(_parameter))
            .setCrosstabGroupStyle(getCrossTabGroupStyle4Excel(_parameter))
            .setCrosstabCellStyle(getCrossTabCellStyle4Excel(_parameter))
            .setCrosstabGroupTotalStyle(getCrossTabGroupTotalStyle4Excel(_parameter))
            .setCrosstabGrandTotalStyle(getCrossTabGrandTotalStyle4Excel(_parameter))
            .setIgnorePageWidth(true).setIgnorePagination(true);
    }

    /**
     * Get the style for the columns in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     *
     */
    protected void configure4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        getStyleTemplate().setColumnTitleStyle(getColumnTitleStyle4Pdf(_parameter))
            .setColumnStyle(getColumnStyle4Pdf(_parameter))
            .setGroupStyle(getGroupStyle4Pdf(_parameter))
            .setGroupTitleStyle(getGroupStyle4Pdf(_parameter))
            .setSubtotalStyle(getSubtotalStyle4Pdf(_parameter))
            .setCrosstabGroupStyle(getCrossTabGroupStyle4Pdf(_parameter))
            .setCrosstabCellStyle(getCrossTabCellStyle4Pdf(_parameter))
            .setCrosstabGroupTotalStyle(getCrossTabGroupTotalStyle4Pdf(_parameter))
            .setCrosstabGrandTotalStyle(getCrossTabGrandTotalStyle4Pdf(_parameter))
            .setIgnorePageWidth(false)
            .setIgnorePagination(false)
            .setHighlightDetailEvenRows(true)
            .setCrosstabHighlightEvenRows(true);
    }

    /**
     * Get style for the columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(DynamicReports.stl.padding(5));
    }

    /**
     * Get style for the columns in case of a excel document.
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
     * Get style for the columns in case of a pdf document.
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
                        .setRightBorder(DynamicReports.stl.pen1Point())
                        .setFontSize(8);
    }

    /**
     * Get style for the title columns in case of a html document.
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
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold().setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setPadding(DynamicReports.stl.padding(5)).setSpacingAfter(4);
    }

    /**
     * Get style for the title columns in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getColumnTitleStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setPadding(2)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold();
    }

    /**
     * Get style for the title columns in case of a pdf document.
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
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold().setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get style for the group columns in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected StyleBuilder getGroupStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get style for the group columns in case of an excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected StyleBuilder getGroupStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get style for the group columns in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected StyleBuilder getGroupStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                        .setPadding(DynamicReports.stl.padding(2));
    }

    /**
     * Get style for the subtotal in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected ReportStyleBuilder getSubtotalStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setTopBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get style for the subtotal in case of an excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected ReportStyleBuilder getSubtotalStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setTopBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get style for the subtotal in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return style as StyleBuilder
     * @throws EFapsException on error
     */
    protected ReportStyleBuilder getSubtotalStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().bold()
                        .setTopBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get style for cross tab group in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold();
    }

    /**
     * Get style for cross tab group in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style();
    }

    /**
     * Get style for cross tab group in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold();
    }

    /**
     * Get style for cross tab cell in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabCellStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get style for cross tab cell in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabCellStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style();
    }

    /**
     * Get style for cross tab cell in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabCellStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point());
    }

    /**
     * Get style for cross tab group total in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupTotalStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold()
                        .setBackgroundColor(new Color(170, 170, 170));
    }

    /**
     * Get style for cross tab group total in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupTotalStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style();
    }

    /**
     * Get style for cross tab group total in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGroupTotalStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold()
                        .setBackgroundColor(new Color(170, 170, 170));
    }

    /**
     * Get style for cross tab grand total in case of a html document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGrandTotalStyle4Html(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold()
                        .setBackgroundColor(new Color(140, 140, 140));
    }

    /**
     * Get style for cross tab grand total in case of a excel document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGrandTotalStyle4Excel(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style();
    }

    /**
     * Get style for cross tab grand total in case of a pdf document.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    protected StyleBuilder getCrossTabGrandTotalStyle4Pdf(final Parameter _parameter)
        throws EFapsException
    {
        return DynamicReports.stl.style().setPadding(2)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setBackgroundColor(Color.LIGHT_GRAY)
                        .bold()
                        .setBackgroundColor(new Color(140, 140, 140));
    }

    /**
     * Configure the page. Will only be called if no template is given.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _report report to configure
     * @throws EFapsException on error
     */
    protected void configurePage(final Parameter _parameter,
                                 final JasperReportBuilder _report)
        throws EFapsException
    {
        _report.setPageFormat(getPageType(), getPageOrientation());
        _report.pageHeader(getPageHeader(_parameter, _report));
        _report.pageFooter(getPageFooter(_parameter, _report));
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _report Report builder
     * @return the component to add
     * @throws EFapsException on error
     */
    protected ComponentBuilder<?, ?> getPageFooter(final Parameter _parameter,
                                                  final JasperReportBuilder _report)
        throws EFapsException
    {
        final StyleBuilder boldCenteredStyle = DynamicReports.stl.style().bold()
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        return DynamicReports.cmp.pageXslashY().setStyle(boldCenteredStyle);
    }

    /**
     * @param _parameter        Parameter as passed by the eFaps API
     * @param _report           Report builder
     * @return the component to add
     * @throws EFapsException on error
     */
    protected ComponentBuilder<?, ?> getPageHeader(final Parameter _parameter,
                                                   final JasperReportBuilder _report)
        throws EFapsException
    {
        final StyleBuilder style = DynamicReports.stl.style().bold()
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                        .setFontSize(10);

        final HorizontalListBuilder ret = DynamicReports.cmp.horizontalList();
        // Commons-Configuration
        final SystemConfiguration config = SystemConfiguration.get(UUID
                        .fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3"));
        if (config != null) {
            final String companyName = config.getAttributeValue("org.efaps.commons.CompanyName");
            if (companyName != null) {
                ret.add(DynamicReports.cmp.text(companyName).setStyle(style)
                                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT));
            }
            final String companyTax = config.getAttributeValue("org.efaps.commons.CompanyTaxNumber");
            if (companyName != null) {
                ret.add(DynamicReports.cmp.text(companyTax).setStyle(style)
                                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT));
            }
        }

        final TextFieldBuilder<String> dateField = DynamicReports.cmp.text(new DateTimeExpression(new DateTime(),
                        DateTimeDate.get()));

        ret.add(dateField.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT));
        ret.newRow();
        ret.add(DynamicReports.cmp.text(DBProperties.getProperty(getClass().getName() + ".Title"))
                        .setStyle(DynamicReports.stl.style(style).setFontSize(12))
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return a JRDataSource
     * @throws EFapsException on error
     */
    protected abstract JRDataSource createDataSource(Parameter _parameter)
        throws EFapsException;

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _builder Report builder
     * @throws EFapsException on error
     */
    protected abstract void addColumnDefintion(Parameter _parameter,
                                               JasperReportBuilder _builder)
        throws EFapsException;

    /**
     * Getter method for the instance variable {@link #report}.
     *
     * @return value of instance variable {@link #report}
     * @throws EFapsException on error
     */
    public JasperReportBuilder getReport()
        throws EFapsException
    {
        if (this.report == null) {
            this.report = DynamicReports.report().setLocale(Context.getThreadContext().getLocale());
        }
        return this.report;
    }

    /**
     * Getter method for the instance variable {@link #reportTemplate}.
     *
     * @return value of instance variable {@link #reportTemplate}
     */
    public ReportTemplateBuilder getStyleTemplate()
    {
        return this.reportTemplate;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    public String getHtml(final Parameter _parameter)
        throws EFapsException
    {
        return getHtml(_parameter, false);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    public File getPDF(final Parameter _parameter)
        throws EFapsException
    {
        init(_parameter);
        File file = null;
        this.exType = ExportType.PDF;
        try {
            if (getTemplateName() != null) {
                final JasperReportBuilder subreport = DynamicReports.report();
                configure4Pdf(_parameter);
                subreport.setTemplate(getStyleTemplate());
                addColumnDefintion(_parameter, subreport);

                final SubreportBuilder sub = DynamicReports.cmp.subreport(subreport);
                sub.setDataSource(createDataSource(_parameter));

                getReport().detail(sub);

                final InputStream in = getTemplate(_parameter, getTemplateName());
                getReport().setTemplateDesign(in);

                final EFapsDataSource ds = new EFapsDataSource();
                ds.init(getReport().toJasperReport(), _parameter, null, getReport().getJasperParameters());

                getReport().setLocale(Context.getThreadContext().getLocale()).setDataSource(ds);
            } else if (getTemplateDesign() != null) {
                final InputStream in = getTemplate(_parameter, getTemplateDesign());
                getReport().setTemplateDesign(in);
                addColumnDefintion(_parameter, getReport());
                getReport().setLocale(Context.getThreadContext().getLocale())
                                .setDataSource(createDataSource(_parameter));
            } else {
                addColumnDefintion(_parameter, getReport());
                getReport().setLocale(Context.getThreadContext().getLocale())
                                .setDataSource(createDataSource(_parameter));
                configurePage(_parameter, getReport());
            }

            file = new FileUtil().getFile(getFileName() == null ? "PDF" : getFileName(), "pdf");
            final JasperPdfExporterBuilder exporter = Exporters.pdfExporter(file);
            configure4Pdf(_parameter);
            getReport().setTemplate(getStyleTemplate());
            add2ReportParameter(_parameter);
            getReport().setParameters(getParameters());
            getReport().toPdf(exporter);
        } catch (final DRException e) {
            AbstractDynamicReport_Base.LOG.error("catched DRException", e);
        }
        return file;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    public File getExcel(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        File file = null;
        this.exType = ExportType.EXCEL;
        try {
            if (properties.get("Template") != null) {
                final String template = String.valueOf(properties.get("Template"));
                final JasperReportBuilder subreport = DynamicReports.report();
                configure4Excel(_parameter);
                subreport.setTemplate(getStyleTemplate());
                addColumnDefintion(_parameter, subreport);

                final SubreportBuilder sub = DynamicReports.cmp.subreport(subreport);
                sub.setDataSource(createDataSource(_parameter));

                getReport().detail(sub);

                final InputStream in = getTemplate(_parameter, template);
                getReport().setTemplateDesign(in);

                final EFapsDataSource ds = new EFapsDataSource();
                ds.init(getReport().toJasperReport(), _parameter, null, getReport().getJasperParameters());

                getReport().setLocale(Context.getThreadContext().getLocale())
                    .setDataSource(ds);
            }   else if (properties.containsKey("TemplateDesign4XLS")) {
                final String template = String.valueOf(properties.get("TemplateDesign4XLS"));
                final InputStream in = getTemplate(_parameter, template);
                getReport().setTemplateDesign(in);
                addColumnDefintion(_parameter, getReport());
                getReport().setLocale(Context.getThreadContext().getLocale())
                                .setDataSource(createDataSource(_parameter));
            } else {
                addColumnDefintion(_parameter, getReport());
                getReport().setLocale(Context.getThreadContext().getLocale())
                                .setDataSource(createDataSource(_parameter));
            }

            file = new FileUtil().getFile(getFileName() == null ? "XLS" : getFileName(), "xls");
            final JasperXlsExporterBuilder exporter = Exporters.xlsExporter(file);
            exporter.setIgnorePageMargins(true)
                    .setDetectCellType(true)
                    .setIgnoreCellBackground(true)
                    .setWhitePageBackground(false)
                    .setRemoveEmptySpaceBetweenColumns(true);
            configure4Excel(_parameter);
            getReport().setTemplate(getStyleTemplate());
            add2ReportParameter(_parameter);
            getReport().setParameters(getParameters());
            getReport().toXls(exporter);
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
     * @param _fileName value for instance variable {@link #fileName}
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
            this.exType = ExportType.HTML;
            addColumnDefintion(_parameter, getReport());

            final JasperHtmlExporterBuilder exporter = Exporters.htmlExporter(writer);
            if (_strip) {
                exporter.setHtmlHeader("").setHtmlFooter("");
            }
            exporter.setIgnorePageMargins(true).setSizeUnit(SizeUnit.PIXEL);
            configure4Html(_parameter);
            getReport().setTemplate(getStyleTemplate());
            getReport().setLocale(Context.getThreadContext().getLocale())
                .setDataSource(createDataSource(_parameter)).toHtml(exporter);
        } catch (final DRException e) {
            AbstractDynamicReport_Base.LOG.error("catched DRException", e);
        }
        return writer.toString();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return html document as String
     * @throws EFapsException on error
     */
    public String getHtmlSnipplet(final Parameter _parameter)
        throws EFapsException
    {
        return "<div class=\"eFapsJR\">" + getHtml(_parameter, true) + "</div>";
    }

    /**
     * @param _parameter        Parameter as passed by the eFaps API
     * @param _jasperFileName   file Name
     * @return inputStream
     */
    public InputStream getTemplate(final Parameter _parameter,
                                   final String _jasperFileName)
    {
        AbstractDynamicReport_Base.LOG.debug("getting Template '{}'", _jasperFileName);
        InputStream ret = null;
        try {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReport);
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

    /**
     * Getter method for the instance variable {@link #exType}.
     *
     * @return value of instance variable {@link #exType}
     */
    protected ExportType getExType()
    {
        return this.exType;
    }

    /**
     * Setter method for instance variable {@link #exType}.
     *
     * @param _exType value for instance variable {@link #exType}
     */
    protected void setExType(final ExportType _exType)
    {
        this.exType = _exType;
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @throws EFapsException on error
     */
    protected void init(final Parameter _parameter)
        throws EFapsException
    {
        setTemplateName(getProperty(_parameter, "Template"));
        setTemplateDesign(getProperty(_parameter, "TemplateDesign"));

        if (containsProperty(_parameter, "PageType")) {
            setPageType(PageType.valueOf(getProperty(_parameter, "PageType")));
        }
        if (containsProperty(_parameter, "PageOrientation")) {
            setPageOrientation(PageOrientation.valueOf(getProperty(_parameter, "PageOrientation")));
        }
        setIncludeHeader("true".equalsIgnoreCase(getProperty(_parameter, "IncludeHeader")));
        setIncludeFooter("true".equalsIgnoreCase(getProperty(_parameter, "IncludeFooter")));
    }

    /**
     * Getter method for the instance variable {@link #templateName}.
     *
     * @return value of instance variable {@link #templateName}
     */
    protected String getTemplateName()
    {
        return this.templateName;
    }

    /**
     * Setter method for instance variable {@link #templateName}.
     *
     * @param _templateName value for instance variable {@link #templateName}
     */
    protected void setTemplateName(final String _templateName)
    {
        this.templateName = _templateName;
    }


    /**
     * Getter method for the instance variable {@link #templateDesign}.
     *
     * @return value of instance variable {@link #templateDesign}
     */
    protected String getTemplateDesign()
    {
        return this.templateDesign;
    }

    /**
     * Setter method for instance variable {@link #templateDesign}.
     *
     * @param _templateDesign value for instance variable {@link #templateDesign}
     */
    protected void setTemplateDesign(final String _templateDesign)
    {
        this.templateDesign = _templateDesign;
    }

    /**
     * Getter method for the instance variable {@link #pageOrientation}.
     *
     * @return value of instance variable {@link #pageOrientation}
     */
    protected PageOrientation getPageOrientation()
    {
        return this.pageOrientation;
    }

    /**
     * Setter method for instance variable {@link #pageOrientation}.
     *
     * @param _pageOrientation value for instance variable {@link #pageOrientation}
     */
    protected void setPageOrientation(final PageOrientation _pageOrientation)
    {
        this.pageOrientation = _pageOrientation;
    }

    /**
     * Getter method for the instance variable {@link #pageType}.
     *
     * @return value of instance variable {@link #pageType}
     */
    protected PageType getPageType()
    {
        return this.pageType;
    }

    /**
     * Setter method for instance variable {@link #pageType}.
     *
     * @param _pageType value for instance variable {@link #pageType}
     */
    protected void setPageType(final PageType _pageType)
    {
        this.pageType = _pageType;
    }

    /**
     * Setter method to adding parameter to report.
     * @param _parameters Parameter to set
     */
    public void setParameters(final Map<String, Object> _parameters)
    {
        this.parameters = _parameters;
    }

    /**
     * @return Parameters.
     */
    protected Map<String, Object> getParameters()
    {
        return this.parameters;
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    protected void add2ReportParameter(final Parameter _parameter)
        throws EFapsException
    {
        getParameters().put(JRParameter.REPORT_LOCALE, Context.getThreadContext().getLocale());

        final Instance inst = _parameter.getInstance();
        if (inst != null && inst.isValid()) {
            getParameters().put(ParameterValues.INSTANCE.name(), inst);
        }

        final Instance callInst = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        if (callInst != null && callInst.isValid()) {
            getParameters().put(ParameterValues.CALL_INSTANCE.name(), callInst);
        }
    }

    /**
     * Getter method for the instance variable {@link #includeHeader}.
     *
     * @return value of instance variable {@link #includeHeader}
     */
    protected boolean isIncludeHeader()
    {
        return this.includeHeader;
    }

    /**
     * Setter method for instance variable {@link #includeHeader}.
     *
     * @param _includeHeader value for instance variable {@link #includeHeader}
     */
    protected void setIncludeHeader(final boolean _includeHeader)
    {
        this.includeHeader = _includeHeader;
    }

    /**
     * Getter method for the instance variable {@link #includeFooter}.
     *
     * @return value of instance variable {@link #includeFooter}
     */
    protected boolean isIncludeFooter()
    {
        return this.includeFooter;
    }

    /**
     * Setter method for instance variable {@link #includeFooter}.
     *
     * @param _includeFooter value for instance variable {@link #includeFooter}
     */
    protected void setIncludeFooter(final boolean _includeFooter)
    {
        this.includeFooter = _includeFooter;
    }

    /**
     * Column.
     *
     * @param <T> the generic type
     * @param _title the _title
     * @param _fieldName the _field name
     * @param _dataType the _data type
     * @return the text column builder
     */
    public static <T> TextColumnBuilder<T> column(final String _title,
                                                  final String _fieldName,
                                                  final DRIDataType<? super T, T> _dataType)
    {
        return DynamicReports.col.column(_title, DynamicReports.field(_fieldName, _dataType));
    }
}
