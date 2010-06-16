/*
 * Copyright 2003 - 2010 The eFaps Team
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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignField;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.FormatedStringType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4675dffe-6551-477b-b069-8968901aeff4")
@EFapsRevision("$Rev$")
public abstract class EFapsDataSource_Base
    implements IeFapsDataSource
{
    /**
     * Field name.
     */
    public static final String OIDFIELDNAME = "eFaps_Field_4_Parent_2_Child_Relation";

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

    private JRDataSource parentSource;

    private final List<String> selects = new ArrayList<String>();

    private String expand;

    private String typeName;

    private boolean expandChild;

    private boolean useInstance;

    private Parameter parameter;

    private JasperReport jasperReport;

    /**
     * Method to initialize this datasource.
     * @param _jasperReport jasperreport this datasource belongs to
     * @param _parameter    Parameter as passed to an esjp by eFaps
     * @param _parentSource parent source
     * @throws EFapsException on error
     */
    public void init(final JasperReport _jasperReport,
                     final Parameter _parameter,
                     final JRDataSource _parentSource) throws EFapsException
    {
        this.parameter = _parameter;
        this.jasperReport = _jasperReport;
        for (final JRParameter para : this.jasperReport.getMainDataset().getParameters()) {
            if ("EFAPS_DEFINITION".equals(para.getName())) {
                if (para.hasProperties()) {
                    this.typeName  = para.getPropertiesMap().getProperty("Type");
                    this.expand  = para.getPropertiesMap().getProperty("Expand");
                    this.hasSubReport = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("hasSubReport"));
                    this.expandChild = !"false".equalsIgnoreCase(para.getPropertiesMap()
                                    .getProperty("expandChildTypes"));
                    this.useInstance = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("Instance"));
                    this.isSubDataSource = "true".equalsIgnoreCase(para.getPropertiesMap()
                                    .getProperty("useInstanceFromParent"));
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

    protected void analyze()
        throws EFapsException
    {
        final List<Instance> instances = new ArrayList<Instance>();
        if (this.typeName != null) {
            final QueryBuilder queryBuilder = new QueryBuilder(Type.get(this.typeName));
            final InstanceQuery query = queryBuilder.getQuery();
            query.setIncludeChildTypes(this.expandChild);
            query.execute();
            instances.addAll(query.getValues());
        } else  if (this.expand != null) {
            final SearchQuery query = new SearchQuery();
            query.setExpand(this.instance, this.expand);
            query.setExpandChildTypes(this.expandChild);
            query.addSelect("OID");
            query.execute();
            while (query.next()) {
                instances.add(Instance.get((String) query.get("OID")));
            }
            query.close();
        } else if (this.useInstance) {
            instances.add(this.parameter.getInstance());
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
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     * @param _field JRField
     * @return value for the given field
     * @throws JRException on error
     */
    public Object getFieldValue(final JRField _field) throws JRException
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
                        ret = HtmlMarkupConverter_Base.getConvertedString((String) ret);
                    }
                } catch (final EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    protected Instance getParentInstance()
        throws JRException
    {
        final JRDesignField field = new JRDesignField();
        field.setName(EFapsDataSource_Base.OIDFIELDNAME);
        return (Instance) this.parentSource.getFieldValue(field);
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     * @return true if a next value exist, else false
     * @throws JRException on error
     */
    public boolean next()
        throws JRException
    {
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
        return this.print == null ? tmp : this.print.next();
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
     * Getter method for the instance variable {@link #expand}.
     *
     * @return value of instance variable {@link #expand}
     */
    public String getExpand()
    {
        return this.expand;
    }


    /**
     * Setter method for instance variable {@link #expand}.
     *
     * @param _expand value for instance variable {@link #expand}
     */

    public void setExpand(final String _expand)
    {
        this.expand = _expand;
    }


    /**
     * Getter method for the instance variable {@link #typeName}.
     *
     * @return value of instance variable {@link #typeName}
     */
    public String getTypeName()
    {
        return this.typeName;
    }


    /**
     * Setter method for instance variable {@link #typeName}.
     *
     * @param _typeName value for instance variable {@link #typeName}
     */

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

    /**
     * Getter method for the instance variable {@link #parameter}.
     *
     * @return value of instance variable {@link #parameter}
     */
    public Parameter getParameter()
    {
        return this.parameter;
    }
}
