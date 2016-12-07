/*
 * Copyright 2003 - 2015 The eFaps Team
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.update.schema.program.jasperreport.JasperReportImporter.FakeQueryExecuterFactory;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOdtReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

// TODO: Auto-generated Javadoc
/**
 * "Mime" as property in the calling command, or "mime" as parameter from a
 * form. Command overrules!
 *
 * @author The eFaps Team
 */
@EFapsUUID("c3a1f5f8-b263-4ad4-b144-db68437074cc")
@EFapsApplication("eFaps-Kernel")
public abstract class StandartReport_Base
    extends AbstractCommon
    implements EventExecution
{

    /**
     * Key used to store a map in the Session.
     */
    protected static final String SESSIONKEY = StandartReport.class.getName() + ".SessionKey";

    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StandartReport.class);

    /**
     * Parameter map that will be passed to the jasper FillManager.
     */
    private final Map<String, Object> jrParameters = new HashMap<>();

    /**
     * The name for the returned file.
     */
    private String fileName = null;

    /**
     * Execute.
     *
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
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
     * Gets the prompt parameters field value.
     *
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     */
    public Return getPromptParametersFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        final Instance reportInst;
        if (InstanceUtils.isValid(_parameter.getInstance())
                        && _parameter.getInstance().getType().isKindOf(CIAdminProgram.JasperReport)) {
            reportInst = _parameter.getInstance();
        } else {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReport);
            queryBldr.addWhereAttrEqValue(CIAdminProgram.JasperReport.Name,
                            getProperty(_parameter, "JasperReport"));
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            query.next();
            reportInst = query.getCurrentValue();
        }
        final Checkout checkout = new Checkout(reportInst);
        final InputStream is = checkout.execute();

        DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.query.executer.factory.eFaps",
                        FakeQueryExecuterFactory.class.getName());
        try {
            final JasperDesign jasperDesign = new JRXmlLoader(DefaultJasperReportsContext.getInstance(),
                            JRXmlDigesterFactory.createDigester(DefaultJasperReportsContext.getInstance())).loadXML(is);
            final Set<JRParameter> paras = new HashSet<>();
            for (final JRParameter parameter : jasperDesign.getParameters()) {
                if (parameter.isForPrompting() && !parameter.isSystemDefined()) {
                    paras.add(parameter);
                    final String name = parameter.getName();
                    final String descr = parameter.getDescription();
                    final JRExpression expr = parameter.getDefaultValueExpression();
                    final String defaultValue;
                    if (expr != null) {
                        defaultValue = expr.getText();
                    } else {
                        defaultValue = "";
                    }
                    final String inputType;
                    switch (parameter.getValueClassName()) {
                        case "java.lang.Integer":
                            inputType = "number";
                            break;
                        default:
                            inputType = "text";
                            break;
                    }
                    html.append("<div><span>").append(name).append(": ").append(descr).append("</span>")
                            .append("<input type=\"").append(inputType).append("\" name=\"para_")
                            .append(name).append("\" value=\"")
                            .append(defaultValue == null ? "" : defaultValue).append("\"></div>");
                }
            }
            Context.getThreadContext().setSessionAttribute(SESSIONKEY, paras);
        } catch (final JRException | ParserConfigurationException | SAXException e) {
            LOG.error("Catched Error: ", e);
        }
        if (html.length() == 0) {
            html.append("&nbsp;");
        }
        final IUIValue uiObject = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (org.efaps.admin.ui.field.Field.Display.HIDDEN.equals(uiObject.getDisplay())) {
            ret.put(ReturnValues.SNIPLETT, "<input type=\"hidden\">");
        } else {
            ret.put(ReturnValues.SNIPLETT, html.toString());
        }
        return ret;
    }

    /**
     * Create4 jasper.
     *
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     */
    @SuppressWarnings("unchecked")
    public Return create4Jasper(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        if (InstanceUtils.isValid(instance) && instance.getType().isKindOf(CIAdminProgram.JasperReport)) {
            final PrintQuery print = new PrintQuery(instance);
            print.addAttribute(CIAdminProgram.JasperReport.Name);
            print.execute();
            ParameterUtil.setProperty(_parameter, "JasperReport",
                            print.<String>getAttribute(CIAdminProgram.JasperReport.Name));
        }

        ParameterUtil.setProperty(_parameter, "NoDataSource", "true");
        final Set<JRParameter> paras;
        if (Context.getThreadContext().containsSessionAttribute(SESSIONKEY)) {
            paras =  (Set<JRParameter>) Context.getThreadContext().getSessionAttribute(SESSIONKEY);
        } else {
            paras = SetUtils.emptySet();
        }

        for (final JRParameter jrParameter : paras) {
            final String value = _parameter.getParameterValue("para_" + jrParameter.getName());
            Object obj = null;
            switch (jrParameter.getValueClassName()) {
                case "java.lang.Integer":
                    obj = Integer.valueOf(value);
                    break;
                case "java.lang.Long":
                    obj = Long.valueOf(value);
                    break;
                case "java.lang.Boolean":
                    obj = BooleanUtils.toBoolean(value);
                    break;
                default:
                    obj = value;
                    break;
            }
            this.jrParameters.put(jrParameter.getName(), obj);
        }
        return execute(_parameter);
    }

    /**
     * Add2 report parameter.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    protected void add2ReportParameter(final Parameter _parameter)
        throws EFapsException
    {
        this.jrParameters.put(JRParameter.REPORT_LOCALE, Context.getThreadContext().getLocale());
        this.jrParameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, new EFapsResourceBundle());

        final Instance inst = _parameter.getInstance();
        if (inst != null && inst.isValid()) {
            getJrParameters().put(ParameterValues.INSTANCE.name(), inst);
        }

        final Instance callInst = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        if (callInst != null && callInst.isValid()) {
            getJrParameters().put(ParameterValues.CALL_INSTANCE.name(), callInst);
        }
    }

    /**
     * Get the instance of the JasperReport.
     * <ol>
     * <li>JasperReport</li>
     * <li>JasperConfig with JasperConfigReport</li>
     * <li>JasperKey</li>
     * </ol>
     * @param _parameter Parameter as passed by the eFasp API
     * @return Instance of the jasperreport
     * @throws EFapsException on error
     */
    protected Instance getJasperReportInstance(final Parameter _parameter)
        throws EFapsException
    {
        Instance ret = null;
        String name = null;
        // first priority special Config explicitly set
        if (containsProperty(_parameter, "JasperConfig") && containsProperty(_parameter, "JasperConfigReport")) {
            final String config = getProperty(_parameter, "JasperConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            name = sysConf.getAttributeValue(getProperty(_parameter, "JasperConfigReport"));
        }
        // second priority the general SystemConfiguration from Commons
        if (name == null && containsProperty(_parameter, "JasperKey")) {
            // Commons-Configuration
            final SystemConfiguration sysConf = SystemConfiguration.get(UUID
                            .fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3"));
            final String key = getProperty(_parameter, "JasperKey");
            if (sysConf != null && key != null) {
                final Properties props = sysConf.getAttributeValueAsProperties("org.efaps.commons.JasperKey", true);
                name = props.getProperty(key);
            }
        }
        // last the properties of the command
        if (name == null && containsProperty(_parameter, "JasperReport")) {
            name = getProperty(_parameter, "JasperReport");
        }
        if (name == null) {
            StandartReport_Base.LOG.debug("Neither JasperReport, JasperKey nor properties lead to valid Report Name");
        } else {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
            queryBldr.addWhereAttrEqValue(CIAdminProgram.JasperReportCompiled.Name, name);
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            if (query.next()) {
                ret = query.getCurrentValue();
            }
        }
        return ret;
    }

    /**
     * Gets the mime.
     *
     * @param _parameter the _parameter
     * @return the mime
     * @throws EFapsException on error
     */
    protected String getDataSourceClass(final Parameter _parameter)
        throws EFapsException
    {
        String ret = null;
        if (containsProperty(_parameter, "DataSourceClass")) {
            ret = getProperty(_parameter, "DataSourceClass");
        } else if (containsProperty(_parameter, "JasperConfig")) {
            final String config = getProperty(_parameter, "JasperConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            ret = sysConf.getAttributeValue(getProperty(_parameter, "JasperConfigDataSourceClass"));
        } else if (_parameter.getParameterValue("DataSourceClass") != null) {
            ret = _parameter.getParameterValue("DataSourceClass");
        }
        return ret;
    }

    /**
     * Gets the file.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return file created
     * @throws EFapsException on error
     */
    public File getFile(final Parameter _parameter)
        throws EFapsException
    {
        File ret = null;

        final String dataSourceClass = getDataSourceClass(_parameter);

        add2ReportParameter(_parameter);

        final Instance jasperInst = getJasperReportInstance(_parameter);
        if (jasperInst != null && jasperInst.isValid()) {
            final Checkout checkout = new Checkout(jasperInst);
            final InputStream iin = checkout.execute();
            try {
                DefaultJasperReportsContext.getInstance().setProperty(
                                "net.sf.jasperreports.query.executer.factory.eFaps",
                                EQLQueryExecuterFactory.class.getName());
                final LocalJasperReportsContext ctx = new LocalJasperReportsContext(
                                DefaultJasperReportsContext.getInstance());
                ctx.setFileResolver(new JasperFileResolver());
                ctx.setClassLoader(EFapsClassLoader.getInstance());

                ctx.setProperty("net.sf.jasperreports.subreport.runner.factory",
                                SubReportRunnerFactory.class.getName());
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
                    // add an abort criteria
                }

                // check for a file name, if null search in the properties
                if (getFileName() == null) {
                    setFileName(getProperty(_parameter, "FileName"));
                    // last chance search in the jasper Parameters
                    if (getFileName() == null) {
                        setFileName((String) this.jrParameters.get("FileName"));
                    }
                }
                final JasperPrint print = runner.getJasperPrint();
                LOG.debug("print created: '{}'", print);
                if (print != null) {
                    ret = getFile(print, getMime(_parameter));
                }

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
        return getFile(_jasperPrint, EnumUtils.<JasperMime>getEnum(JasperMime.class, _mime.toUpperCase()));
    }

    /**
     * Gets the mime.
     *
     * @param _parameter the _parameter
     * @return the mime
     * @throws EFapsException on error
     */
    public JasperMime getMime(final Parameter _parameter)
        throws EFapsException
    {
        String mime = null;
        if (containsProperty(_parameter, "Mime")) {
            mime = getProperty(_parameter, "Mime");
        } else if (containsProperty(_parameter, "JasperConfig")) {
            final String config = getProperty(_parameter, "JasperConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            mime = sysConf.getAttributeValue(getProperty(_parameter, "JasperConfigMime"));
        } else if (_parameter.getParameterValue("mime") != null) {
            mime = _parameter.getParameterValue("mime");
        }

        final JasperMime ret;
        if (mime != null && !mime.isEmpty()) {
            ret = EnumUtils.<JasperMime>getEnum(JasperMime.class, mime.toUpperCase());
        } else {
            ret = JasperMime.PDF;
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
                           final JasperMime _mime)
        throws IOException, JRException, EFapsException
    {
        File file = null;
        switch (_mime) {
            case PDF:
                file = new FileUtil().getFile(getFileName() == null ? "PDF" : getFileName(),
                                JasperMime.PDF.getExtension());
                final FileOutputStream os = new FileOutputStream(file);
                JasperExportManager.exportReportToPdfStream(_jasperPrint, os);
                os.close();
                break;
            case ODT:
                file = new FileUtil().getFile(getFileName() == null ? "ODT" : getFileName(),
                                JasperMime.ODT.getExtension());
                final SimpleOdtReportConfiguration config = new SimpleOdtReportConfiguration();
                final JROdtExporter odtExp = new JROdtExporter();
                odtExp.setConfiguration(config);
                odtExp.setExporterInput(new SimpleExporterInput(_jasperPrint));
                odtExp.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                odtExp.exportReport();
                break;
            case ODS:
                file = new FileUtil().getFile(getFileName() == null ? "ODS" : getFileName(),
                                JasperMime.ODS.getExtension());
                final JROdsExporter odsExp = new JROdsExporter();
                odsExp.setExporterInput(new SimpleExporterInput(_jasperPrint));
                odsExp.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                odsExp.exportReport();
                break;
            case XLS:
                file = new FileUtil().getFile(getFileName() == null ? "XLS" : getFileName(),
                                JasperMime.XLS.getExtension());
                final JRXlsExporter xlsExp = new JRXlsExporter();
                _jasperPrint.setName(_jasperPrint.getName().replaceAll("[\\\\/:\"*?<>|]+", "-"));
                xlsExp.setExporterInput(new SimpleExporterInput(_jasperPrint));
                xlsExp.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                final SimpleXlsReportConfiguration xlsConfig = new SimpleXlsReportConfiguration();
                xlsConfig.setDetectCellType(true);
                xlsConfig.setIgnoreCellBorder(true);
                xlsConfig.setRemoveEmptySpaceBetweenColumns(true);
                xlsConfig.setRemoveEmptySpaceBetweenRows(true);
                xlsConfig.setWhitePageBackground(false);
                xlsConfig.setOnePagePerSheet(false);
                xlsConfig.setIgnorePageMargins(true);
                xlsExp.setConfiguration(xlsConfig);
                xlsExp.exportReport();
                break;
            case DOCX:
                file = new FileUtil().getFile(getFileName() == null ? "DOCX" : getFileName(),
                                JasperMime.DOCX.getExtension());
                final JRDocxExporter docxExp = new JRDocxExporter();
                docxExp.setExporterInput(new SimpleExporterInput(_jasperPrint));
                docxExp.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                docxExp.exportReport();
                break;
            case XLSX:
                final JRXlsxExporter xlsxExp = new JRXlsxExporter();
                _jasperPrint.setName(_jasperPrint.getName().replaceAll("[\\\\/:\"*?<>|]+", "-"));
                xlsxExp.setExporterInput(new SimpleExporterInput(_jasperPrint));
                xlsxExp.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                final SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
                xlsxConfig.setDetectCellType(true);
                xlsxConfig.setIgnoreCellBorder(true);
                xlsxConfig.setRemoveEmptySpaceBetweenColumns(true);
                xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
                xlsxConfig.setWhitePageBackground(false);
                xlsxConfig.setOnePagePerSheet(false);
                xlsxConfig.setIgnorePageMargins(true);
                xlsxExp.setConfiguration(xlsxConfig);
                xlsxExp.exportReport();
                break;
            case CSV:
            case HTML:
            case RTF:
            case XML:
                LOG.warn("NOT IMPLEMENTED YET!");
                break;
            case TXT:
            default:
                file = new FileUtil().getFile(getFileName() == null ? "TXT" : getFileName(),
                                JasperMime.TXT.getExtension());
                final JRTextExporter txtExporter = new JRTextExporter();
                txtExporter.setExporterInput(new SimpleExporterInput(_jasperPrint));
                txtExporter.setExporterOutput(new SimpleWriterExporterOutput(file));
                final SimpleTextReportConfiguration txtConfig = new SimpleTextReportConfiguration();
                txtConfig.setCharHeight(new Float(10));
                txtConfig.setCharWidth(new Float(6));
                txtExporter.exportReport();
                break;
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

    /**
     * The Enum JasperMime.
     *
     */
    public enum JasperMime
    {
        /** The csv. */
        CSV("csv"),
        /** The docx. */
        DOCX("docx"),
        /** The html. */
        HTML("html"),
        /** The ods. */
        ODS("ods"),
        /** The odt. */
        ODT("odt"),
        /** The pdf. */
        PDF("pdf"),
        /** The pptx. */
        PPTX("pptx"),
        /** The rtf. */
        RTF("rtf"),
        /** The txt. */
        TXT("txt"),
        /** The xls. */
        XLS("xls"),
        /** The xlsx. */
        XLSX("xlsx"),
        /** The xml. */
        XML("xml");

        /** The extension. */
        private final String extension;

        /**
         * Instantiates a new jasper mime.
         *
         * @param _extension the extension
         */
        JasperMime(final String _extension)
        {
            this.extension = _extension;
        }

        /**
         * Getter method for the instance variable {@link #extension}.
         *
         * @return value of instance variable {@link #extension}
         */
        public String getExtension()
        {
            return this.extension;
        }
    }

    /**
     * The Enum JasperMime.
     *
     */
    public enum JasperActivation
    {

        /** The oncreate. */
        ONCREATE,

        /** The onedit. */
        ONEDIT;
    }
}
