/*
 * Copyright 2003 - 2020 The eFaps Team
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
package org.efaps.esjp.common.eql;

import java.math.BigDecimal;
import java.util.List;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("ab29429d-9b73-49ac-b5eb-d49d8892def6")
@EFapsApplication("eFaps-Kernel")
public abstract class SumSelect_Base
    extends AbstractSelect
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SumSelect.class);

    @Override
    public void initialize(final List<Instance> _instances,
                           final String... _parameters)
        throws EFapsException
    {
        if (_parameters == null || _parameters.length == 0) {
            LOG.error("No select to summarize defined");
        } else {
            final String select = _parameters[0];
            final MultiPrintQuery multi = new MultiPrintQuery(_instances);
            multi.addSelect(select);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final Object values = multi.getSelect(select);
                BigDecimal sum = BigDecimal.ZERO;
                if (values != null) {
                    if (values instanceof BigDecimal) {
                        sum = sum.add((BigDecimal) values);
                    } else if (values instanceof Iterable) {
                        for (final Object value : (Iterable<?>) values) {
                            if (value instanceof BigDecimal) {
                                sum = sum.add((BigDecimal) value);
                            }
                        }
                    }
                }
                getValues().put(multi.getCurrentInstance(), sum);
            }

        }
    }
}
