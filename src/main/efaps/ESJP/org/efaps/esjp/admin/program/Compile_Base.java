/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.esjp.admin.program;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler.OneJasperReport;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("bd4c747d-e196-4d99-a99d-1bfbac41cc01")
@EFapsApplication("eFaps-Kernel")
public abstract class Compile_Base
{
    /**
     * @param _parameter parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return compileJasperReport(final Parameter _parameter)
        throws EFapsException
    {
        final List<Instance> instances = new ArrayList<>();

        if (_parameter.getInstance() != null && _parameter.getInstance().isValid()) {
            instances.add(_parameter.getInstance());
        } else {
            final String[] oids = (String[])  _parameter.get(ParameterValues.OTHERS);
            if (oids != null) {
                for (final String oid : oids) {
                    final Instance instance = Instance.get(oid);
                    if (instance.isValid()) {
                        instances.add(instance);
                    }
                }
            }
        }

        for (final Instance instance : instances) {
            final PrintQuery print = new PrintQuery(instance);
            print.addAttribute(CIAdminProgram.JasperReport.Name);
            print.execute();
            final JasperReportCompiler compiler = new JasperReportCompiler(
                            org.efaps.rest.Compile.getClassPathElements());
            final OneJasperReport source = compiler.getNewSource(
                            print.<String>getAttribute(CIAdminProgram.JasperReport.Name), instance);
            compiler.compile(source);
        }
        return new Return();
    }
}
