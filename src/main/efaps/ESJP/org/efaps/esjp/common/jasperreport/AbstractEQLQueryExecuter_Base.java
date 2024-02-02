/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.common.jasperreport;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRQueryExecuter;

@EFapsUUID("40394ef6-c833-4b27-8cfb-c14493ee2178")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractEQLQueryExecuter_Base
    implements JRQueryExecuter
{

    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEQLQueryExecuter.class);

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
    public AbstractEQLQueryExecuter_Base(final JasperReportsContext _jasperReportsContext,
                                         final JRDataset _dataset,
                                         final Map<String, ? extends JRValueParameter> _parameters)
    {
        jasperReportsContext = _jasperReportsContext;
        dataset = _dataset;
        parameters = _parameters;
    }

    /**
     * Getter method for the instance variable {@link #jasperReportsContext}.
     *
     * @return value of instance variable {@link #jasperReportsContext}
     */
    public JasperReportsContext getJasperReportsContext()
    {
        return jasperReportsContext;
    }

    /**
     * Getter method for the instance variable {@link #parameters}.
     *
     * @return value of instance variable {@link #parameters}
     */
    public Map<String, ? extends JRValueParameter> getParameters()
    {
        return parameters;
    }

    /**
     * Getter method for the instance variable {@link #dataset}.
     *
     * @return value of instance variable {@link #dataset}
     */
    public JRDataset getDataset()
    {
        return dataset;
    }

    /**
     * Getter method for the instance variable {@link #pattern}.
     *
     * @return value of instance variable {@link #pattern}
     */
    protected Pattern getParameterPattern()
    {
        return parameterPattern;
    }

    /**
     * Getter method for the instance variable {@link #keyPattern}.
     *
     * @return value of instance variable {@link #keyPattern}
     */
    protected Pattern getKeyPattern()
    {
        return keyPattern;
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
            replaceMap.put(mainStr, getStringValue(_stmtStr, subMatcher.group()));
        }
        String ret = _stmtStr;
        for (final Entry<String, String> entry  :replaceMap.entrySet()) {
            ret = ret.replace(entry.getKey(), entry.getValue());
        }
        LOG.debug("Stmt with replaced Parameters: {}", ret);
        return ret;
    }

    /**
     * @param _stmtStr stamentStr the parameters will be replace for
     * @param _key key the value will bee searched for
     * @return String representation of the object
     */
    protected String getStringValue(final String _stmtStr,
                                    final String _key)
    {
        String ret = "";
        if (_key != null) {
            final JRValueParameter parameter = getParameters().get(_key);
            if (parameter != null) {
                final Object object = parameter.getValue();
                LOG.trace("Found object to be replaced: {}", object);
                if (object instanceof Instance) {
                    if (_stmtStr.matches("(?s)^print *obj.*") || _stmtStr.trim().startsWith("execute")) {
                        ret = ((Instance) object).getOid();
                    } else {
                        ret = Long.valueOf(((Instance) object).getId()).toString();
                    }
                } else if (object != null) {
                    ret = object.toString();
                }
            }
        }
        return ret;
    }

    @Override
    public void close()
    {
    }

    @Override
    public boolean cancelQuery()
        throws JRException
    {
        return false;
    }
}
