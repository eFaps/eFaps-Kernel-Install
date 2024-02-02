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

import net.sf.jasperreports.engine.fill.AbstractThreadSubreportRunner;
import net.sf.jasperreports.engine.fill.JRBaseFiller;
import net.sf.jasperreports.engine.fill.JRFillSubreport;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("fbde8f43-95f0-4d8e-9d5a-b32283fbcddf")
@EFapsApplication("eFaps-Kernel")
public class SubReportRunner
    extends AbstractThreadSubreportRunner
{

    private Thread fillThread;

    public SubReportRunner(final JRFillSubreport fillSubreport,
                           final JRBaseFiller subreportFiller)
    {
        super(fillSubreport, subreportFiller);
    }

    @Override
    public boolean isFilling()
    {
        return this.fillThread != null;
    }

    @Override
    protected void doStart()
    {
        this.fillThread = new Thread(this, this.subreportFiller.getJasperReport().getName() + " subreport filler");

        this.fillThread.start();
    }

    @Override
    public void reset()
    {
        this.fillThread = null;
    }
}
