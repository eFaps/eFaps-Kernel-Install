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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.query.JRQueryExecuter;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.eql.Statement;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("547e75b2-b76b-4cc6-83d7-516554fadf22")
@EFapsRevision("$Rev$")
public abstract class EQLQueryExecuter_Base
    implements JRQueryExecuter
{
    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EQLQueryExecuter.class);

    /**
     *  The JasperReportsContext.
     */
    private final JasperReportsContext jasperReportsContext;

    /**
     * The dataset containing the query, fields, etc.
     */
    private final JRDataset dataset;

    /**
     * Map of value parameters.
     */
    private final Map<String, ? extends JRValueParameter> parameters;

    /**
     * Pattern for getting the Parameter including there special symbols.
     */
    private final Pattern parameterPattern = Pattern.compile("\\$P!\\{[a-z,A-Z,1-9,_,-]*\\}");

    /**
     * Pattern for getting the key of the Parameter.
     */
    private final Pattern keyPattern = Pattern.compile("(?<=\\{)([a-z,A-Z,1-9,_,-]*?)(?=\\})");

    /**
     * @param _jasperReportsContext the JasperReportsContext
     * @param _dataset the dataset containing the query, fields, etc
     * @param _parameters map of value parameters (instances of {@link JRValueParameter JRValueParameter})
     *  indexed by name
     */
    public EQLQueryExecuter_Base(final JasperReportsContext _jasperReportsContext,
                                 final JRDataset _dataset,
                                 final Map<String, ? extends JRValueParameter> _parameters)
    {
        this.jasperReportsContext = _jasperReportsContext;
        this.dataset = _dataset;
        this.parameters = _parameters;
    }

    /**
     * Executes the query and creates a {@link JRDataSource JRDataSource} out of the result.
     *
     * @return a {@link JRDataSource JRDataSource} wrapping the query execution result.
     * @throws JRException on error
     */
    @Override
    public JRDataSource createDatasource()
        throws JRException
    {
        final List<Map<String, ?>> list = new ArrayList<>();
        try {
            final String stmtStr = this.dataset.getQuery().getText();
            final Statement stmt = Statement.getStatement(replaceParameters(stmtStr));
            final Map<String, String> mapping = stmt.getAlias2Selects();

            final MultiPrintQuery multi = stmt.getMultiPrint();
            multi.execute();
            while (multi.next()) {
                final Map<String, Object> map = new HashMap<>();
                for (final Entry<String, String> entry : mapping.entrySet()) {
                    map.put(entry.getKey(), multi.getSelect(entry.getValue()));
                }
                list.add(map);
            }
        } catch (final EFapsException e) {
            LOG.error("Catched Exception", e);
        }
        final JRMapCollectionDataSource ret = new JRMapCollectionDataSource(list);
        return ret;
    }

    /**
     * Closes resources kept open during the data source iteration.
     * <p/>
     * This method is called after the report is filled or the dataset is iterated.
     * If a resource is not needed after the data source has been created, it should be
     * released at the end of {@link #createDatasource() createDatasource}.
     */
    @Override
    public void close()
    {
        // nothing must be done
    }

    /**
     * Cancels the query if it's currently running.
     * <p/>
     * This method will be called from a different thread if the client decides to
     * cancel the filling process.
     *
     * @return <code>true</code> if and only if the query was running and it has been canceled
     * @throws JRException on error
     */
    @Override
    public boolean cancelQuery()
        throws JRException
    {
        return false;
    }

    /**
     * Getter method for the instance variable {@link #jasperReportsContext}.
     *
     * @return value of instance variable {@link #jasperReportsContext}
     */
    public JasperReportsContext getJasperReportsContext()
    {
        return this.jasperReportsContext;
    }

    /**
     * Getter method for the instance variable {@link #parameters}.
     *
     * @return value of instance variable {@link #parameters}
     */
    public Map<String, ? extends JRValueParameter> getParameters()
    {
        return this.parameters;
    }

    /**
     * Getter method for the instance variable {@link #dataset}.
     *
     * @return value of instance variable {@link #dataset}
     */
    public JRDataset getDataset()
    {
        return this.dataset;
    }

    /**
     * @param _stmtStr stamentStr the parameters will be replace for
     * @return stmt
     */
    protected String replaceParameters(final String _stmtStr)
    {
        final Pattern mainPattern = getParameterPattern();
        final Pattern subPattern = getKeyPattern();
        final Matcher matcher = mainPattern.matcher(_stmtStr);
        final Map<String, String> replaceMap = new HashMap<>();
        while (matcher.find()) {
            final String mainStr = matcher.group();
            final Matcher subMatcher = subPattern.matcher(mainStr);
            subMatcher.find();
            replaceMap.put(mainStr, getStringValue(subMatcher.group()));
        }
        String ret = _stmtStr;
        for (final Entry<String, String> entry  :replaceMap.entrySet()) {
            ret = ret.replace(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    /**
     * @param _key key the value will bee searched for
     * @return String representation of the object
     */
    protected String getStringValue(final String _key)
    {
        String ret = "";
        if (_key != null) {
            final JRValueParameter parameter = getParameters().get(_key);
            if (parameter != null) {
                final Object object = parameter.getValue();
                if (object instanceof Instance) {
                    ret = ((Instance) object).getOid();
                } else if (object != null) {
                    ret = object.toString();
                }
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #pattern}.
     *
     * @return value of instance variable {@link #pattern}
     */
    protected Pattern getParameterPattern()
    {
        return this.parameterPattern;
    }

    /**
     * Getter method for the instance variable {@link #keyPattern}.
     *
     * @return value of instance variable {@link #keyPattern}
     */
    protected Pattern getKeyPattern()
    {
        return this.keyPattern;
    }
}
