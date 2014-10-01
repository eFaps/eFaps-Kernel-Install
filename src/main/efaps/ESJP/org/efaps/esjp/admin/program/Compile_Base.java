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

package org.efaps.esjp.admin.program;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
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
@EFapsRevision("$Rev$")
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
        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        print.addAttribute(CIAdminProgram.JasperReport.Name);
        print.execute();

        final JasperReportCompiler compiler = new JasperReportCompiler(org.efaps.rest.Compile.getClassPathElements());
        final OneJasperReport source = compiler.getNewSource(
                        print.<String>getAttribute(CIAdminProgram.JasperReport.Name),
                        _parameter.getInstance());
        compiler.compile(source);
        return new Return();
    }
}
