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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.stmt.IEQLStmt;
import org.efaps.eql.stmt.parts.ISelectStmtPart;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("547e75b2-b76b-4cc6-83d7-516554fadf22")
@EFapsApplication("eFaps-Kernel")
public abstract class EQLQueryExecuter_Base
    extends AbstractEQLQueryExecuter
{
    /**
     * Logger used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EQLQueryExecuter.class);

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
        super(_jasperReportsContext, _dataset, _parameters);
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
            final String stmtStr = getDataset().getQuery().getText();
            EQLQueryExecuter_Base.LOG.debug("Stmt: {}", stmtStr);
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(replaceParameters(stmtStr));
            if (stmt instanceof ISelectStmtPart) {
                list.addAll(((ISelectStmtPart) stmt).getData());
            }
        } catch (final EFapsException e) {
            EQLQueryExecuter_Base.LOG.error("Catched Exception", e);
        } catch (final Exception e) {
            EQLQueryExecuter_Base.LOG.error("Catched Exception", e);
        }
        final JRMapCollectionDataSource ret = new JRMapCollectionDataSource(list);
        return ret;
    }

}
