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
package org.efaps.esjp.common.help;

import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.api.ui.IHelpProvider;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.admin.common.IReloadCacheListener;
import org.efaps.esjp.admin.program.Markdown;
import org.efaps.esjp.ci.CIAdminProgram;
import org.efaps.util.EFapsBaseException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HelpProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("35dd8375-2841-49d8-b63d-d99643fde2c9")
@EFapsApplication("eFaps-Kernel")
public abstract class HelpProvider_Base
    implements IHelpProvider, IReloadCacheListener
{

    /** The cachekey. */
    protected static final String CACHEKEY = HelpProvider.class.getName();

    /** The cachekey. */
    protected static final String TOGGLEKEY = HelpProvider.class.getName() + ".EditMode";


    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HelpProvider.class);

    /**
     * Gets the cache.
     *
     * @return the cache
     * @throws EFapsException on error
     */
    protected Cache<Long, String> getCache()
        throws EFapsException
    {
        if (!InfinispanCache.get().exists(HelpProvider.CACHEKEY)) {
            onReloadCache(null);
        }
        return InfinispanCache.get().<Long, String>getCache(HelpProvider.CACHEKEY);
    }

    @Override
    public boolean hasHelp(final Long _cmdId)
        throws EFapsException
    {
        final boolean ret;
        if (_cmdId == null) {
            ret = false;
        } else {
            if (!getCache().containsKey(_cmdId)) {
                loadHelp(_cmdId);
            }
            ret = !getCache().get(_cmdId).equals(HelpProvider.CACHEKEY);
        }
        return ret;
    }

    /**
     * Load help.
     *
     * @param _cmdId the cmd id
     * @throws EFapsException on error
     */
    protected void loadHelp(final Long _cmdId)
        throws EFapsException
    {
        final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminProgram.Markdown2Command);
        attrQueryBldr.addWhereAttrEqValue(CIAdminProgram.Markdown2Command.ToLink, _cmdId);

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.Markdown);
        queryBldr.addWhereAttrInQuery(CIAdminProgram.Markdown.ID, attrQueryBldr.getAttributeQuery(
                        CIAdminProgram.Markdown2Command.FromLink));
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        final StringBuilder html = new StringBuilder();
        while (query.next()) {
            html.append(new Markdown().renderHtml(query.getCurrentValue()));
        }
        getCache().put(_cmdId, html.length() > 0 ? html.toString() : HelpProvider.CACHEKEY);
    }

    @Override
    public CharSequence getHelp(final Long _cmdId)
        throws EFapsException
    {
        return getCache().get(_cmdId);
    }

    @Override
    public void onReloadSystemConfig(final Parameter _parameter)
        throws EFapsException
    {
        // Nothing will be done
    }

    @Override
    public void onReloadCache(final Parameter _parameter)
        throws EFapsException
    {
        if (InfinispanCache.get().exists(HelpProvider.CACHEKEY)) {
            InfinispanCache.get().<Long, String>getCache(HelpProvider.CACHEKEY).clear();
        } else {
            final Cache<Long, String> cache = InfinispanCache.get().initCache(HelpProvider.CACHEKEY);
            cache.addListener(new CacheLogListener(HelpProvider_Base.LOG));
        }
    }

    @Override
    public int getWeight()
    {
        return 0;
    }

    @Override
    public boolean isHelpAdmin()
        throws EFapsBaseException
    {
        //Administration role
        final Role role = Role.get(UUID.fromString("1d89358d-165a-4689-8c78-fc625d37aacd"));
        //Admin_Help_Admin
        final Role role2 = Role.get(UUID.fromString("a2da3b6c-3e3d-4fe8-8daf-dec8ef5c2370"));
        return role != null && Context.getThreadContext().getPerson().isAssigned(role)
                        || role2 != null && Context.getThreadContext().getPerson().isAssigned(role2);
    }

    @Override
    public boolean isEditMode()
        throws EFapsBaseException
    {
        return Context.getThreadContext().containsSessionAttribute(HelpProvider.TOGGLEKEY);
    }

    /**
     * Toggle edit.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return toggleEdit(final Parameter _parameter)
        throws EFapsException
    {
        if (Context.getThreadContext().containsSessionAttribute(HelpProvider.TOGGLEKEY)) {
            Context.getThreadContext().removeSessionAttribute(HelpProvider.TOGGLEKEY);
        } else {
            Context.getThreadContext().setSessionAttribute(HelpProvider.TOGGLEKEY, true);
        }
        return new Return();
    }
}
