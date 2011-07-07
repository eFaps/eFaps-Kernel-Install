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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ef631aef-45d8-4192-90c9-56898175228d")
@EFapsRevision("$Rev$")
abstract class SubReportContainer_Base
    extends HashMap<String, JRDataSource>
{
    /**
     * Logging instance used to give logging information of this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(SubReportContainer_Base.class);
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Parameter for this esjp.
     */
    private final Parameter parameter;

    /**
     * JRDataSource used for this subreport.
     */
    private final JRDataSource dataSource;

    /**
     * Map that contains the report parameter.
     */
    private final Map<String, Object> jrParameters;

    /**
     * Constructor.
     * @param _parameter   Parameter
     * @param _dataSource  JRDataSource used for this subreport
     * @param _jrParameters map that contains the report parameters
     */
    public SubReportContainer_Base(final Parameter _parameter,
                                   final JRDataSource _dataSource,
                                   final Map<String, Object> _jrParameters)
    {
        this.parameter = _parameter;
        this.dataSource = _dataSource;
        this.jrParameters = _jrParameters;
    }

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     * @param _key key to the JRDataSource
     * @return JRDataSource
     */
    @Override
    public JRDataSource get(final Object _key)
    {
        JRDataSource ret = super.get(_key);
        if (ret == null) {
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
                queryBldr.addWhereAttrEqValue("Name", _key);
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                Instance instance = null;
                if (query.next()) {
                    instance = query.getCurrentValue();
                }
                final Checkout checkout = new Checkout(instance);
                final InputStream iin = checkout.execute();
                final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(iin);
                iin.close();
                IeFapsDataSource dataSourceNew;
                if (this.dataSource != null) {
                    final Class<?> clazz = Class.forName(this.dataSource.getClass().getName());
                    final Method method = clazz.getMethod("init",
                                    new Class[] { JasperReport.class, Parameter.class, JRDataSource.class, Map.class });
                    dataSourceNew = (IeFapsDataSource) clazz.newInstance();
                    method.invoke(dataSourceNew, jasperReport, this.parameter, this.dataSource, this.jrParameters);
                } else {
                    dataSourceNew = new EFapsDataSource();
                    dataSourceNew.init(jasperReport, this.parameter, this.dataSource, this.jrParameters);
                }
                ret = dataSourceNew;
                super.put((String) _key, ret);
            } catch (final JRException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final EFapsException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final ClassNotFoundException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final SecurityException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final NoSuchMethodException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final InstantiationException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final IllegalAccessException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final IllegalArgumentException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final InvocationTargetException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            } catch (final IOException e) {
                SubReportContainer_Base.LOG.error("error accessing JRDataSource from SubReportContainer", e);
            }
        }
        return ret;
    }
}
