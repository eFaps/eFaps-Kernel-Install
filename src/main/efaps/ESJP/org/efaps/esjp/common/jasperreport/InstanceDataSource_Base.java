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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class InstanceDataSource_Base
    implements JRRewindableDataSource
{
    private boolean next = true;

    private Instance instance;

    private PrintQuery print;

    private final List<String> selects = new ArrayList<>();

    public InstanceDataSource_Base(final Instance _instance,
                                   final String _name)

    {
        this.instance = _instance;
        try {
            if (_name != null) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
                queryBldr.addWhereAttrEqValue("Name", _name);
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
                if (jasperReport.getMainDataset().getFields() != null) {
                    for (final JRField field : jasperReport.getMainDataset().getFields()) {
                        final String select = field.getPropertiesMap().getProperty("Select");
                        if (select != null) {
                            this.selects.add(select);
                        }
                    }
                }
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    @Override
    public boolean next()
        throws JRException
    {
        final boolean ret = this.next;
        if (ret) {
            this.next = false;
        }
        return ret;
    }

    @Override
    public Object getFieldValue(final JRField _jrField)
        throws JRException
    {
        Object ret = null;
        try {
            final String select = _jrField.getPropertiesMap().getProperty("Select");

            if (this.print == null) {
                this.print = new PrintQuery(getInstance());
                for (final String selectTmp : getSelects()) {
                    this.print.addSelect(selectTmp);
                }
                this.print.execute();
            }
            ret = this.print.getSelect(select);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void moveFirst()
        throws JRException
    {
        this.next = true;
        this.print = null;
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
