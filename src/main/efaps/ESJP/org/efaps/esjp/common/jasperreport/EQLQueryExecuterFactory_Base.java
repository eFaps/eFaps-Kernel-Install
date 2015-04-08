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

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3c343e41-5abc-4682-862f-d0ee15756663")
@EFapsApplication("eFaps-Kernel")
public abstract class EQLQueryExecuterFactory_Base
    implements QueryExecuterFactory
{
    /**
     * Returns the built-in parameters associated with this query type.
     * <p/>
     * These parameters will be created as system-defined parameters for each
     * report/dataset having a query of this type.
     * <p/>
     * The returned array should contain consecutive pairs of parameter names and parameter classes
     * (e.g. <code>{"Param1", String.class, "Param2", "List.class"}</code>).
     *
     * @return array of built-in parameter names and types associated with this query type
     */
    @Override
    public Object[] getBuiltinParameters()
    {
        return null;
    }


    /**
     * Creates a query executer.
     * <p/>
     * This method is called at fill time for reports/datasets having a query supported by
     * this factory.
     *
     * @param _jasperReportsContext the JasperReportsContext
     * @param _dataset the dataset containing the query, fields, etc
     * @param _parameters map of value parameters (instances of {@link JRValueParameter JRValueParameter})
     *  indexed by name
     *
     * @return a query executer
     * @throws JRException on error
     */
    @Override
    public JRQueryExecuter createQueryExecuter(final JasperReportsContext _jasperReportsContext,
                                               final JRDataset _dataset,
                                               final Map<String, ? extends JRValueParameter> _parameters)
        throws JRException
    {
        return new EQLQueryExecuter(_jasperReportsContext, _dataset, _parameters);
    }

    /**
     * Decides whether the query executers created by this factory support a query parameter type.
     * <p/>
     * This check is performed for all $P{..} parameters in the query.
     *
     * @param _className the value class name of the parameter
     * @return whether the parameter value type is supported
     */
    @Override
    public boolean supportsQueryParameterType(final String _className)
    {
        return false;
    }

    @Override
    public JRQueryExecuter createQueryExecuter(final JRDataset _dataset,
                                               final Map<String, ? extends JRValueParameter> _parameters)
        throws JRException
    {
        // deprecated therefore empty
        return null;
    }
}
