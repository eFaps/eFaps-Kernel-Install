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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IOption;
import org.efaps.api.ui.IPivotProvider;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.eql.EQL;
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
        final IOption demo = new IOption()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getLabel()
            {
                return "Demo Data Source";
            }

            @Override
            public Object getValue()
            {
                return "123.456";
            }

            @Override
            public boolean isSelected()
            {
                return false;
            }
        };
        return Collections.singletonList(demo);
    }

    @Override
    public CharSequence getJsonData(final String _dataSource)
    {
        CharSequence ret = "[]";
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final StringBuilder stmtStr = new StringBuilder()
                            .append("print query type Sales_Invoice select attribute[Name] as Name, "
                                        + "linkto[Contact].attribute[Name] as Cliente,"
                                        + "attribute[NetTotal] as NetTotal,"
                                        + "attribute[Date].format[YYYY] as Year,"
                                        + "attribute[Date].format[MM] as Month");

            final PrintStmt stmt = (PrintStmt) EQL.getStatement(stmtStr);
            final Collection<Map<String, ?>> data = stmt.evaluate().getData();
            ret  = mapper.writeValueAsString(data);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }
}
