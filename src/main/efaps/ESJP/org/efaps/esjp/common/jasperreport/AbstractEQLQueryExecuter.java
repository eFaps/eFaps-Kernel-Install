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

import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("c92dfe30-74e2-4148-b9ab-afa91205b277")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractEQLQueryExecuter
    extends AbstractEQLQueryExecuter_Base
{

    public AbstractEQLQueryExecuter(final JasperReportsContext _jasperReportsContext,
                                    final JRDataset _dataset,
                                    final Map<String, ? extends JRValueParameter> _parameters)
    {
        super(_jasperReportsContext, _dataset, _parameters);
    }
}
