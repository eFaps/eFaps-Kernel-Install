/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.esjp.common.pivot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IOption;
import org.efaps.api.ui.IPivotProvider;
import org.efaps.db.Instance;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql.EQL;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("f18b2558-4df0-4924-abcc-45bb9c84d2bf")
@EFapsApplication("eFaps-Kernel")
public abstract class PivotProvider_Base
    implements IPivotProvider
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PivotProvider.class);

    @Override
    public List<IOption> getDataSources()
    {
        final List<IOption> ret = new ArrayList<>();
        try {
            final Evaluator eval = EQL.print(CICommon.PivotDataSource)
                            .attribute(CICommon.PivotDataSource.Name)
                            .stmt()
                            .evaluate();
            while (eval.next()) {
                final String label = eval.get(CICommon.PivotDataSource.Name);
                final String oid = eval.inst().getOid();
                ret.add(new IOption()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getLabel()
                    {
                        return label;
                    }

                    @Override
                    public Object getValue()
                    {
                        return oid;
                    }

                    @Override
                    public boolean isSelected()
                    {
                        return false;
                    }
                });
            }
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }

    @Override
    public CharSequence getJsonData(final String _dataSource)
    {
        CharSequence ret = "[]";
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final Instance dsInt =  Instance.get(_dataSource);
            if (InstanceUtils.isKindOf(dsInt, CICommon.PivotDataSource)) {
                final String eqlStmt = EQL.print(dsInt).attribute(CICommon.PivotDataSource.EQLStmt)
                    .stmt()
                    .evaluate()
                    .get(CICommon.PivotDataSource.EQLStmt);

                final PrintStmt stmt = (PrintStmt) EQL.getStatement(eqlStmt);
                final Collection<Map<String, ?>> data = stmt.evaluate().getData();
                ret  = mapper.writeValueAsString(data);
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }

    @Override
    public List<IOption> getReports()
    {
        final List<IOption> ret = new ArrayList<>();
        try {
            final Evaluator eval = EQL.print(CICommon.PivotReport)
                            .attribute(CICommon.PivotReport.Name)
                            .stmt()
                            .evaluate();
            while (eval.next()) {
                final String label = eval.get(CICommon.PivotReport.Name);
                final String oid = eval.inst().getOid();
                ret.add(new IOption()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getLabel()
                    {
                        return label;
                    }

                    @Override
                    public Object getValue()
                    {
                        return oid;
                    }

                    @Override
                    public boolean isSelected()
                    {
                        return false;
                    }
                });
            }
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }

    @Override
    public String save(final String _reportName, final String _pivotReport)
    {
        String ret = null;
        try {
            final Instance instance = EQL.insert(CICommon.PivotReport)
                .set(CICommon.PivotReport.Name, _reportName)
                .set(CICommon.PivotReport.Report, _pivotReport)
                .stmt()
                .execute();
            ret = instance.getOid();
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }

    @Override
    public CharSequence getReport(final String _reportKey)
    {
        CharSequence ret = "{}";
        try {
            final Instance dsInt =  Instance.get(_reportKey);
            if (InstanceUtils.isKindOf(dsInt, CICommon.PivotReport)) {
                ret = EQL.print(dsInt).attribute(CICommon.PivotReport.Report)
                    .stmt()
                    .evaluate()
                    .get(CICommon.PivotReport.Report);
            }
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }
}
