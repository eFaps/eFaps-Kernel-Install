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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignField;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.FormatedStringType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for a DataSource as it is needed for a JasperReport.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4675dffe-6551-477b-b069-8968901aeff4")
@EFapsApplication("eFaps-Kernel")
public abstract class EFapsDataSource_Base
    implements IeFapsDataSource, JRRewindableDataSource
{
    /**
     * Field name.
     */
    public static final String OIDFIELDNAME = "eFaps_Field_4_Parent_2_Child_Relation";

    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(EFapsDataSource.class);

    /**
     * PrintQuery for this datasource.
     */
    private MultiPrintQuery print;

    /**
     * has this report a subreport? Used to return on the first call of
     * {@link #next()} true in all cases.
     */
    private boolean hasSubReport;

    /**
     * Is this datasource inside a subreport.
     */
    private boolean isSubDataSource = false;

    /**
     * Instance.
     */
    private Instance instance;

    /**
     * Parent source for this source.
     */
    private JRDataSource parentSource;

    /**
     * List of selects used for the underlying query.
     */
    private final List<String> selects = new ArrayList<String>();

    /**
     * Expand string.
     * @deprecated use typeNames with linkforms instead
     */
    @Deprecated
    private String expand;

    /**
     * Name of the type.
     * @deprecated use typeNames instead
     */
    @Deprecated
    private String typeName;

    /**
     * Must the child types be included or not.
     */
    private boolean expandChild;

    /**
     * Should an instance be used.
     */
    private boolean useInstance;

    /**
     * Parameter as passed from the calling event.
     */
    private Parameter parameter;

    /**
     * JasperReport this datasource belongs to.
     */
    private JasperReport jasperReport;

    /**
     * String with list of types separated by ";".
     */
    private String typeNames;


    /**
     * String with list of linkFroms separated by ";".
     */
    private String linkFroms;

    private boolean usedOids;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final JasperReport _jasperReport,
                     final Parameter _parameter,
                     final JRDataSource _parentSource,
                     final Map<String, Object> _jrParameters)
        throws EFapsException
    {
        this.parameter = _parameter;
        this.jasperReport = _jasperReport;
        for (final JRParameter para : this.jasperReport.getMainDataset().getParameters()) {
            if ("EFAPS_DEFINITION".equals(para.getName())) {
                if (para.hasProperties()) {
                    this.typeNames = para.getPropertiesMap().getProperty("Types");
                    this.linkFroms = para.getPropertiesMap().getProperty("LinkFroms");

                    this.typeName  = para.getPropertiesMap().getProperty("Type");
                    this.expand  = para.getPropertiesMap().getProperty("Expand");
                    this.hasSubReport = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("hasSubReport"));
                    this.expandChild = !"false".equalsIgnoreCase(para.getPropertiesMap()
                                    .getProperty("expandChildTypes"));
                    this.useInstance = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("Instance"));
                    this.isSubDataSource = "true".equalsIgnoreCase(para.getPropertiesMap()
                                    .getProperty("useInstanceFromParent"));
                    this.usedOids  = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("UseOIDs"));
                }
                break;
            }
        }
        this.parentSource = _parentSource;
        try {
            this.instance = this.parameter.getInstance();
            if (this.isSubDataSource) {
                this.instance = getParentInstance();
            }
            analyze();
        } catch (final JRException e) {
            throw new EFapsException(EFapsDataSource_Base.class, "init.JRException", e);
        }
    }

    /**
     * Analyze the datasource and get the data.
     * @throws EFapsException on error
     */
    protected void analyze()
        throws EFapsException
    {
        final List<Instance> instances = new ArrayList<Instance>();
        //expand
        if (getTypeNames() != null) {
            final String[] types = getTypeNames().split(";");
            final String[] linkFromsTmp;
            if (getLinkFroms() != null) {
                linkFromsTmp = getLinkFroms().split(";");
            } else {
                linkFromsTmp = new String[types.length];
            }
            for (int i = 0; i < types.length; i++) {
                final Type type = Type.get(types[0]);
                final QueryBuilder queryBuilder = new QueryBuilder(type);
                if (getLinkFroms() != null) {
                    queryBuilder.addWhereAttrEqValue(linkFromsTmp[i], this.instance.getId());
                }
                final InstanceQuery query = queryBuilder.getQuery();
                query.setIncludeChildTypes(this.expandChild);
                query.execute();
                instances.addAll(query.getValues());
            }
        } else if (this.typeName != null) {
            EFapsDataSource_Base.LOG.error("JasperReport '{0}' implements deprecated API: {1}",
                            this.jasperReport.getName(), this.typeName);
            final QueryBuilder queryBuilder = new QueryBuilder(Type.get(this.typeName));
            final InstanceQuery query = queryBuilder.getQuery();
            query.setIncludeChildTypes(this.expandChild);
            query.execute();
            instances.addAll(query.getValues());
        } else  if (this.expand != null) {
            EFapsDataSource_Base.LOG.error("JasperReport '{0}' implements deprecated API: {1}",
                            this.jasperReport.getName(), this.expand);
            final String[] types = this.expand.split("\\\\");
            if (types.length != 2) {
                throw new EFapsException(EFapsDataSource_Base.class, "expand", this.expand);
            }
            final Type type = Type.get(types[0]);
            final QueryBuilder queryBuilder = new QueryBuilder(type);
            queryBuilder.addWhereAttrEqValue(types[1], this.instance.getId());
            final InstanceQuery query = queryBuilder.getQuery();
            query.setIncludeChildTypes(this.expandChild);
            query.execute();
            instances.addAll(query.getValues());
        } else if (this.useInstance) {
            instances.add(this.parameter.getInstance());
        } else if (this.usedOids) {
            final String[] oids = (String[]) this.parameter.get(ParameterValues.OTHERS);
            for (final String oid : oids) {
                instances.add(Instance.get(oid));
            }
        }
        if (instances.size() > 0) {
            this.print = new MultiPrintQuery(instances);
            if (this.jasperReport.getMainDataset().getFields() != null) {
                for (final JRField field : this.jasperReport.getMainDataset().getFields()) {
                    final String select = field.getPropertiesMap().getProperty("Select");
                    if (select != null) {
                        this.print.addSelect(select);
                        this.selects.add(select);
                    }
                }
            }
            this.print.execute();
        }
    }

    /**
     * @return Instance of the parent
     * @throws JRException on error
     */
    protected Instance getParentInstance()
        throws JRException
    {
        final JRDesignField field = new JRDesignField();
        field.setName(EFapsDataSource_Base.OIDFIELDNAME);
        return (Instance) this.parentSource.getFieldValue(field);
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     * @param _field JRField
     * @return value for the given field
     * @throws JRException on error
     */
    public Object getFieldValue(final JRField _field)
        throws JRException
    {
        Object ret = null;
        if (_field.getName().equals(EFapsDataSource_Base.OIDFIELDNAME)) {
            ret = this.print.getCurrentInstance();
        } else {
            final String select = _field.getPropertiesMap().getProperty("Select");
            if (select != null) {
                try {
                    ret = this.print.getSelect(select);
                    final Attribute attr = this.print.getAttribute4Select(select);
                    if (attr != null && attr.getAttributeType().getClassRepr().equals(FormatedStringType.class)) {
                        ret = new HtmlMarkupConverter().getConvertedString((String) ret);
                    }
                    if (_field.getPropertiesMap().containsProperty("Converter")) {
                        ret = getConvertedValue(ret, _field.getPropertiesMap().getProperty("Converter"));
                    }
                } catch (final EFapsException e) {
                    throw new JRException("Error while getting value for a field", e);
                }
            }
        }
        return ret;
    }

    /**
     * @param _value                Value to be converted
     * @param _converterClassName   classname of the converter
     * @return converted value
     * @throws EFapsException on error
     */
    protected Object getConvertedValue(final Object _value,
                                       final String _converterClassName)
        throws EFapsException
    {
        Object ret = _value;
        try {
            final Class<?> clazz = Class.forName(_converterClassName);
            final IConverter converter = (IConverter) clazz.newInstance();
            ret = converter.getConvertedValue(_value);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException("error with Converter", e);
        } catch (final InstantiationException e) {
            throw new EFapsException("error with Converter", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException("error with Converter", e);
        }
        return ret;
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     * @return true if a next value exist, else false
     * @throws JRException on error
     */
    public boolean next()
        throws JRException
    {
        boolean ret = false;
        final boolean tmp = this.hasSubReport;
        if (this.hasSubReport) {
            this.hasSubReport = false;
        }

        if (this.isSubDataSource && this.instance != null) {
            if (!this.instance.equals(getParentInstance())) {
                try {
                    this.instance = getParentInstance();
                    analyze();
                } catch (final EFapsException e) {
                    throw new JRException(e);
                }
            }
        }
        try {
            ret = this.print == null ? tmp : this.print.next();
        } catch (final EFapsException e) {
            new JRException(e);
        }
        return ret;
    }

    /**
     * Move the dataset to the first value. In this case the query is executed
     * again to get the values.
     * @throws JRException on error
     */
    @Override
    public void moveFirst()
        throws JRException
    {
        try {
            analyze();
        } catch (final EFapsException e) {
            throw new JRException("Error during new retreival of the values.", e);
        }
    }

    /**
     * Getter method for the instance variable {@link #parameter}.
     *
     * @return value of instance variable {@link #parameter}
     */
    public Parameter getParameter()
    {
        return this.parameter;
    }

    /**
     * Setter method for instance variable {@link #parameter}.
     *
     * @param _parameter value for instance variable {@link #parameter}
     */
    public void setParameter(final Parameter _parameter)
    {
        this.parameter = _parameter;
    }

    /**
     * Getter method for instance variable {@link #hasSubReport}.
     *
     * @return value of instance variable {@link #hasSubReport}
     */
    public boolean isSubReport()
    {
        return this.hasSubReport;
    }

    /**
     * Setter method for instance variable {@link #hasSubReport}.
     *
     * @param _subReport value for instance variable {@link #hasSubReport}
     */
    public void setSubReport(final boolean _subReport)
    {
        this.hasSubReport = _subReport;
    }

    /**
     * Getter method for instance variable {@link #print}.
     *
     * @return value of instance variable {@link #print}
     */
    public MultiPrintQuery getPrint()
    {
        return this.print;
    }

    /**
     * Setter method for instance variable {@link #print}.
     *
     * @param _print value for instance variable {@link #print}
     */
    public void setPrint(final MultiPrintQuery _print)
    {
        this.print = _print;
    }

    /**
     * Getter method for the instance variable {@link #hasSubReport}.
     *
     * @return value of instance variable {@link #hasSubReport}
     */
    public boolean isHasSubReport()
    {
        return this.hasSubReport;
    }

    /**
     * Setter method for instance variable {@link #hasSubReport}.
     *
     * @param _hasSubReport value for instance variable {@link #hasSubReport}
     */

    public void setHasSubReport(final boolean _hasSubReport)
    {
        this.hasSubReport = _hasSubReport;
    }

    /**
     * Getter method for the instance variable {@link #isSubDataSource}.
     *
     * @return value of instance variable {@link #isSubDataSource}
     */
    public boolean isSubDataSource()
    {
        return this.isSubDataSource;
    }

    /**
     * Setter method for instance variable {@link #isSubDataSource}.
     *
     * @param _isSubDataSource value for instance variable {@link #isSubDataSource}
     */

    public void setSubDataSource(final boolean _isSubDataSource)
    {
        this.isSubDataSource = _isSubDataSource;
    }

    /**
     * Getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Setter method for instance variable {@link #instance}.
     *
     * @param _instance value for instance variable {@link #instance}
     */

    public void setInstance(final Instance _instance)
    {
        this.instance = _instance;
    }

    /**
     * Getter method for the instance variable {@link #parentSource}.
     *
     * @return value of instance variable {@link #parentSource}
     */
    public JRDataSource getParentSource()
    {
        return this.parentSource;
    }

    /**
     * Setter method for instance variable {@link #parentSource}.
     *
     * @param _parentSource value for instance variable {@link #parentSource}
     */

    public void setParentSource(final JRDataSource _parentSource)
    {
        this.parentSource = _parentSource;
    }

    /**
     * Getter method for the instance variable {@link #typeNames}.
     *
     * @return value of instance variable {@link #typeNames}
     */
    public String getTypeNames()
    {
        return this.typeNames;
    }


    /**
     * Setter method for instance variable {@link #typeNames}.
     *
     * @param _typeNames value for instance variable {@link #typeNames}
     */

    public void setTypeNames(final String _typeNames)
    {
        this.typeNames = _typeNames;
    }


    /**
     * Getter method for the instance variable {@link #linkFroms}.
     *
     * @return value of instance variable {@link #linkFroms}
     */
    public String getLinkFroms()
    {
        return this.linkFroms;
    }


    /**
     * Setter method for instance variable {@link #linkFroms}.
     *
     * @param _linkFroms value for instance variable {@link #linkFroms}
     */

    public void setLinkFroms(final String _linkFroms)
    {
        this.linkFroms = _linkFroms;
    }


    /**
     * Getter method for the instance variable {@link #expand}.
     *
     * @return value of instance variable {@link #expand}
     * @deprecated use typeNames with linkforms instead
     */
    @Deprecated
    public String getExpand()
    {
        return this.expand;
    }

    /**
     * Setter method for instance variable {@link #expand}.
     *
     * @param _expand value for instance variable {@link #expand}
     * @deprecated use typeNames with linkforms instead
     */
    @Deprecated
    public void setExpand(final String _expand)
    {
        this.expand = _expand;
    }

    /**
     * Getter method for the instance variable {@link #typeName}.
     *
     * @return value of instance variable {@link #typeName}
     * @deprecated use typeNames instead
     */
    @Deprecated
    public String getTypeName()
    {
        return this.typeName;
    }

    /**
     * Setter method for instance variable {@link #typeName}.
     *
     * @param _typeName value for instance variable {@link #typeName}
     * @deprecated use typeNames instead
     */
    @Deprecated
    public void setTypeName(final String _typeName)
    {
        this.typeName = _typeName;
    }

    /**
     * Getter method for the instance variable {@link #expandChild}.
     *
     * @return value of instance variable {@link #expandChild}
     */
    public boolean isExpandChild()
    {
        return this.expandChild;
    }

    /**
     * Setter method for instance variable {@link #expandChild}.
     *
     * @param _expandChild value for instance variable {@link #expandChild}
     */

    public void setExpandChild(final boolean _expandChild)
    {
        this.expandChild = _expandChild;
    }

    /**
     * Getter method for the instance variable {@link #useInstance}.
     *
     * @return value of instance variable {@link #useInstance}
     */
    public boolean isUseInstance()
    {
        return this.useInstance;
    }

    /**
     * Setter method for instance variable {@link #useInstance}.
     *
     * @param _useInstance value for instance variable {@link #useInstance}
     */

    public void setUseInstance(final boolean _useInstance)
    {
        this.useInstance = _useInstance;
    }

    /**
     * Getter method for the instance variable {@link #jasperReport}.
     *
     * @return value of instance variable {@link #jasperReport}
     */
    public JasperReport getJasperReport()
    {
        return this.jasperReport;
    }

    /**
     * Setter method for instance variable {@link #jasperReport}.
     *
     * @param _jasperReport value for instance variable {@link #jasperReport}
     */

    public void setJasperReport(final JasperReport _jasperReport)
    {
        this.jasperReport = _jasperReport;
    }

    /**
     * Getter method for the instance variable {@link #selects}.
     *
     * @return value of instance variable {@link #selects}
     */
    public List<String> getSelects()
    {
        return this.selects;
    }
}
