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

import net.sf.jasperreports.engine.fill.JRBaseFiller;
import net.sf.jasperreports.engine.fill.JRFillSubreport;
import net.sf.jasperreports.engine.fill.JRSubreportRunner;
import net.sf.jasperreports.engine.fill.JRSubreportRunnerFactory;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
//CHECKSTYLE:OFF
@EFapsUUID("f6fbd1f1-dce6-4637-8df7-6a7897b6f535")
@EFapsApplication("eFaps-Kernel")
public class SubReportRunnerFactory
    implements JRSubreportRunnerFactory
{
//CHECKSTYLE:ON
    @Override
    public JRSubreportRunner createSubreportRunner(final JRFillSubreport _fillSubreport,
                                                   final JRBaseFiller _subreportFiller)
    {
        return new SubReportRunner(_fillSubreport, _subreportFiller);
    }

}
