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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IDashboard;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsBaseException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EsjpSnippletFactory_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("159f73ed-f9ee-4015-b6eb-0acbf5fcc812")
@EFapsApplication("eFaps-Kernel")
public abstract class Dashboard_Base
    extends AbstractCommon
    implements IDashboard
{

    /** The Constant CACHEKEY. */
    public static final String CACHEKEY = Dashboard.class.getName() + ".CacheKey";

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Dashboard.class);

    /** The title. */
    private String title;

    /** The dashboard inst. */
    private Instance dashboardInst;

    /**
     * Gets the dashboard inst.
     *
     * @return the dashboard inst
     */
    public Instance getDashboardInst()
    {
        return this.dashboardInst;
    }

    /**
     * Sets the dashboard inst.
     *
     * @param _dashboardInst the new dashboard inst
     */
    public void setDashboardInst(final Instance _dashboardInst)
    {
        this.dashboardInst = _dashboardInst;
    }

    @Override
    public IEsjpSnipplet getEsjpSnipplet(final String _key)
    {
        IEsjpSnipplet ret = null;
        try {
            Instance elInst = null;

            final QueryBuilder elQueryBldr = new QueryBuilder(CICommon.DashboardDefault2Element);
            elQueryBldr.addWhereAttrEqValue(CICommon.DashboardDefault2Element.Field, _key);
            elQueryBldr.addWhereAttrEqValue(CICommon.DashboardDefault2Element.FromLink, getDashboardInst());
            final MultiPrintQuery multi = elQueryBldr.getCachedPrint(CACHEKEY);
            final SelectBuilder selElInst = SelectBuilder.get().linkto(CICommon.DashboardDefault2Element.ToLink)
                            .instance();
            multi.addSelect(selElInst);
            multi.execute();
            if (multi.next()) {
                elInst = multi.getSelect(selElInst);
            }

            if (elInst != null && elInst.isValid()) {
                final PrintQuery print = new CachedPrintQuery(elInst, CACHEKEY);
                final SelectBuilder selEsjpName = SelectBuilder.get().linkto(CICommon.DashboardElement.EsjpLink)
                                .attribute(CIAdminProgram.Java.Name);
                print.addSelect(selEsjpName);
                print.addAttribute(CICommon.DashboardElement.Config);
                if (print.execute()) {
                    final String className = print.getSelect(selEsjpName);
                    final String config = print.getAttribute(CICommon.DashboardElement.Config);
                    final Class<?> clazz = Class.forName(className, false, EFapsClassLoader.getInstance());
                    final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for (final Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0]
                                        .isInstance(new String())) {
                            ret = (IEsjpSnipplet) constructor.newInstance(config);
                            break;
                        }
                    }
                    if (ret == null) {
                        ret = (IEsjpSnipplet) clazz.newInstance();
                    }
                }
            }
        } catch (final ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("Catched error during class instantiation: ", e);
        } catch (final EFapsException e) {
            LOG.error("Catched error: ", e);
        }
        return ret == null ? new DefaultSnipplet() : ret;
    }

    @Override
    public String getTitle()
    {
        return this.title;
    }

    /**
     * Sets the title.
     *
     * @param _title the new title
     */
    public void setTitle(final String _title)
    {
        this.title = _title;
    }

    /**
     * The Class DefaultSnipplet.
     *
     */
    public static class DefaultSnipplet
        implements IEsjpSnipplet
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public CharSequence getHtmlSnipplet()
            throws EFapsBaseException
        {
            return "";
        }

        @Override
        public boolean isVisible()
            throws EFapsBaseException
        {
            return false;
        }

        @Override
        public String getIdentifier()
            throws EFapsBaseException
        {
            return "DefaultSnipplet";
        }
    }
}
