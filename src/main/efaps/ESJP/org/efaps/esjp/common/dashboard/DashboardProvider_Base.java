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
package org.efaps.esjp.common.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IDashboard;
import org.efaps.api.ui.IDashboardProvider;
import org.efaps.db.Context;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;

/**
 * The Class DashboardProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("d8d6cd10-a6a2-45bf-9d3e-de48acc7e2a0")
@EFapsApplication("eFaps-Kernel")
public abstract class DashboardProvider_Base
    implements IDashboardProvider
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public List<IDashboard> getDashboards()
        throws EFapsException
    {
        final List<IDashboard> ret = new ArrayList<>();
        if (Context.getThreadContext().getCompany() != null) {
            final QueryBuilder queryBldr = new QueryBuilder(CICommon.DashboardDefault);
            queryBldr.addOrderByAttributeAsc(CICommon.DashboardDefault.Weight);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.setEnforceSorted(true);
            multi.addAttribute(CICommon.DashboardDefault.Title);
            multi.execute();
            while (multi.next()) {
                final String title = multi.getAttribute(CICommon.DashboardDefault.Title);
                final Dashboard dashboard = new Dashboard();
                dashboard.setTitle(title);
                dashboard.setDashboardInst(multi.getCurrentInstance());
                ret.add(dashboard);
            }
        }
        return ret;
    }
}
