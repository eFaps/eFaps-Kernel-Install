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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * "Mime" as property in the calling command, or "mime" as parameter from a form.
 * Command overrules!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c3a1f5f8-b263-4ad4-b144-db68437074cc")
@EFapsRevision("$Rev$")
public abstract class StandartReport_Base
    extends AbstractCommon
    implements EventExecution
{
    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StandartReport.class);

    /**
     * Parameter map that will be passed to the jasper FillManager.
     */
    private final Map<String, Object> jrParameters = new HashMap<String, Object>();

    /**
     * The name for the returned file.
     */
    private String fileName = null;

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, getFile(_parameter));
        ret.put(ReturnValues.TRUE, true);
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return file created
     * @throws EFapsException on error
     */
    public File getFile(final Parameter _parameter)
        throws EFapsException
    {
        File ret = null;
        String name = null;
        if (containsProperty(_parameter, "JasperReport")) {
            name = getProperty(_parameter, "JasperReport");
        } else {
            // Commons-Configuration
            final SystemConfiguration sysConf = SystemConfiguration.get(UUID
                            .fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3"));
            final String key = getProperty(_parameter, "JasperKey");
            if (sysConf != null && key != null) {
                final Properties props = sysConf.getAttributeValueAsProperties("org.efaps.commons.JasperKey", true);
                name = props.getProperty(key);
            }
        }

        if (name == null) {
            StandartReport_Base.LOG.error("Neither JasperReport nor JasperKey lead to valid Report Name");
        }

        final String dataSourceClass = getProperty(_parameter, "DataSourceClass");
        final boolean noDataSource = "true".equalsIgnoreCase(getProperty(_parameter, "NoDataSource"));

        this.jrParameters.put(JRParameter.REPORT_LOCALE, Context.getThreadContext().getLocale());
        this.jrParameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, new EFapsResourceBundle());

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
        queryBldr.addWhereAttrEqValue(CIAdminProgram.JasperReportCompiled.Name, name);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        Instance instance = null;
        if (query.next()) {
            instance = query.getCurrentValue();
        } else {
            throw new EFapsException(StandartReport_Base.class, "execute.ReportNotFound");
        }
        final Checkout checkout = new Checkout(instance);
        final InputStream iin = checkout.execute();
        try {
            DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.query.executer.factory.eFaps",
                            EQLQueryExecuterFactory.class.getName());
            final LocalJasperReportsContext ctx = new LocalJasperReportsContext(
                            DefaultJasperReportsContext.getInstance());
            ctx.setFileResolver(new JasperFileResolver());
            ctx.setClassLoader(EFapsClassLoader.getInstance());

            ctx.setProperty("net.sf.jasperreports.subreport.runner.factory", SubReportRunnerFactory.class.getName());
            ctx.setProperty("net.sf.jasperreports.query.executer.factory.eFaps",
                            EQLQueryExecuterFactory.class.getName());

            final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(iin);
            iin.close();

            IeFapsDataSource dataSource = null;
            if (dataSourceClass != null) {
                final Class<?> clazz = Class.forName(dataSourceClass);
                final Method method = clazz.getMethod("init",
                                new Class[] { JasperReport.class, Parameter.class, JRDataSource.class, Map.class });
                dataSource = (IeFapsDataSource) clazz.newInstance();
                method.invoke(dataSource, jasperReport, _parameter, null, this.jrParameters);
            } else if (!noDataSource) {
                dataSource = new EFapsDataSource();
                dataSource.init(jasperReport, _parameter, null, this.jrParameters);
            }
            if (dataSource != null) {
                this.jrParameters.put("EFAPS_SUBREPORT", new SubReportContainer(_parameter, dataSource,
                                this.jrParameters));
            }
            final ReportRunner runner = new ReportRunner(ctx, jasperReport, this.jrParameters, dataSource);
            final Thread t = new Thread(runner);
            t.setContextClassLoader(EFapsClassLoader.getInstance());
            t.start();

            while (t.isAlive()) {
            }
            String mime = getProperty(_parameter, "Mime");
            if (mime == null) {
                mime = _parameter.getParameterValue("mime");
            }
            // check for a file name, if null search in the properties
            if (getFileName() == null) {
                setFileName(getProperty(_parameter, "FileName"));
                // last chance search in the jasper Parameters
                if (getFileName() == null) {
                    setFileName((String) this.jrParameters.get("FileName"));
                }
            }
            ret = getFile(runner.getJasperPrint(), mime);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.ClassNotFoundException", e);
        } catch (final SecurityException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.SecurityException", e);
        } catch (final NoSuchMethodException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.NoSuchMethodException", e);
        } catch (final InstantiationException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.IllegalArgumentException", e);
        } catch (final InvocationTargetException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.InvocationTargetException", e);
        } catch (final JRException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.JRException", e);
        } catch (final IOException e) {
            throw new EFapsException(StandartReport_Base.class, "execute.IOException", e);
        }
        return ret;
    }


    /**
     * Method to get the File.
     *
     * @param _jasperPrint jasperprint the file will be created for
     * @param _mime mimetype of the file, default pdf
     * @return File
     * @throws IOException on error
     * @throws JRException on error
     * @throws EFapsException on error
     */
    protected File getFile(final JasperPrint _jasperPrint,
                           final String _mime)
        throws IOException, JRException, EFapsException
    {
        File file = null;
        if ("pdf".equalsIgnoreCase(_mime) || _mime == null) {
            file = new FileUtil().getFile(getFileName() == null ? "PDF" : getFileName(), "pdf");
            final FileOutputStream os = new FileOutputStream(file);
            JasperExportManager.exportReportToPdfStream(_jasperPrint, os);
            os.close();
        } else if ("odt".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "ODT" : getFileName(), "odt");
            final FileOutputStream os = new FileOutputStream(file);
            final JROdtExporter exporter = new JROdtExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("ods".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "ODS" : getFileName(), "ods");
            final FileOutputStream os = new FileOutputStream(file);
            final JROdsExporter exporter = new JROdsExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("xls".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "XLS" : getFileName(), "xls");
            final FileOutputStream os = new FileOutputStream(file);
            final JRXlsExporter exporter = new JRXlsExporter();
            _jasperPrint.setName(_jasperPrint.getName().replaceAll("[\\\\/:\"*?<>|]+", "-"));
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_IGNORE_CELL_BORDER, Boolean.TRUE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
            exporter.setParameter(JRExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
            exporter.exportReport();
            os.close();
        } else if ("rtf".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "RTF" : getFileName(), "rtf");
            final FileOutputStream os = new FileOutputStream(file);
            final JRRtfExporter exporter = new JRRtfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("docx".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "DOCX" : getFileName(), "docx");
            final FileOutputStream os = new FileOutputStream(file);
            final JRDocxExporter exporter = new JRDocxExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("txt".equalsIgnoreCase(_mime)) {
            file = new FileUtil().getFile(getFileName() == null ? "TXT" : getFileName(), "txt");
            final FileOutputStream os = new FileOutputStream(file);
            final JRTextExporter exporter = new JRTextExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, _jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT , new Float(10));
            exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH , new Float(6));
            exporter.exportReport();
            os.close();
        }
        return file;
    }

    /**
     * Getter method for instance variable {@link #jrParameters}.
     *
     * @return value of instance variable {@link #jrParameters}
     */
    public Map<String, Object> getJrParameters()
    {
        return this.jrParameters;
    }

    /**
     * Getter method for instance variable {@link #fileName}.
     *
     * @return value of instance variable {@link #fileName}
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
}

