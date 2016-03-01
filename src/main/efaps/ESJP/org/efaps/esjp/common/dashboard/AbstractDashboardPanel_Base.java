/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.esjp.common.dashboard;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractDashboardPanel_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("9a965d08-8d92-4718-a9d5-af2f93481daa")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractDashboardPanel_Base
    implements Serializable
{

    /**
     * Key used to store the filters in the context.
     */
    protected static final String CACHENAME = AbstractDashboardPanel.class.getName() + ".Cache";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDashboardPanel.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The config. */
    private String config;

    /**
     * Instantiates a new abstract dashboard panel.
     */
    public AbstractDashboardPanel_Base()
    {
    }

    /**
     * Instantiates a new abstract dashboard panel_ base.
     *
     * @param _config the _config
     */
    public AbstractDashboardPanel_Base(final String _config)
    {
        this.config = _config;
    }

    /**
     * Gets the config.
     *
     * @return the config
     */
    public Properties getConfig()
    {
        final Properties ret = new Properties();
        if (this.config != null && !this.config.isEmpty()) {
            try {
                ret.load(new StringReader(this.config));
            } catch (final IOException e) {
                LOG.error("Catched error on reading properties.");
            }
        }
        return ret;
    }

    /**
     * Sets the config.
     *
     * @param _config the new config
     */
    public void setConfig(final String _config)
    {
        this.config = _config;
    }

    /**
     * Checks if is cached.
     * @return true, if is cached
     * @throws EFapsException the e faps exception
     */
    public boolean isCached()
        throws EFapsException
    {
        boolean ret;
        if ("true".equalsIgnoreCase(getConfig().getProperty("ForceReload", "false"))) {
            ret = false;
        } else {
            ret = getCache().containsKey(getCacheKey());
        }
        return ret;
    }

    /**
     * Gets the CharSequence from cache.
     * @return the data source from cache
     * @throws EFapsException the e faps exception
     */
    public CharSequence getFromCache()
        throws EFapsException
    {
        return getCache().get(getCacheKey());
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    public Cache<String, CharSequence> getCache()
    {
        return InfinispanCache.get().getIgnReCache(AbstractDashboardPanel.CACHENAME);
    }

    /**
     * Clear cache.
     * @throws EFapsException the e faps exception
     */
    public void clearCache()
        throws EFapsException
    {
        getCache().remove(getCacheKey());
    }

    /**
     * Cache.
     *
     * @param _html the _html
     * @throws EFapsException the e faps exception
     */
    public void cache(final CharSequence _html)
        throws EFapsException
    {
        cache(_html, true);
    }

    /**
     * Cache.
     *
     * @param _html the _html
     * @param _addCachedInfo the add cached info
     * @throws EFapsException the eFaps exception
     */
    public void cache(final CharSequence _html,
                      final boolean _addCachedInfo)
        throws EFapsException
    {
        final StringBuilder htm = new StringBuilder().append(_html).append("<div class=\"eFapsCached\">")
                        .append(DBProperties.getFormatedDBProperty(AbstractDashboardPanel.class.getName()
                                        + ".Cached.Label", new Date()))
                        .append("</div>");
        getCache().put(getCacheKey(), htm, getLifespan(), getTimeUnit());
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     * @throws EFapsException the eFaps exception
     */
    public String getIdentifier()
        throws EFapsException
    {
        return getCacheKey();
    }

    /**
     * Gets the cache key.
     * @return the cache key
     * @throws EFapsException the e faps exception
     */
    protected String getCacheKey()
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(Context.getThreadContext().getPerson().getId());
        if (Context.getThreadContext().getCompany() != null) {
            ret.append(":").append(Context.getThreadContext().getCompany().getId());
        }
        ret.append(":").append(getConfig().getProperty("CacheKey", getClass().getName()));
        return ret.toString();
    }

    /**
     * Gets the lifespan.
     * @return the lifespan
     * @throws EFapsException on error
     */
    protected Long getLifespan()
        throws EFapsException
    {
        return Long.valueOf(getConfig().getProperty("Lifespan", "1"));
    }

    /**
     * Gets the time unit.
     * @return the time unit
     */
    protected TimeUnit getTimeUnit()
    {
        return TimeUnit.valueOf(getConfig().getProperty("TimeUnit", "MINUTES"));
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    protected Integer getWidth()
    {
        return Integer.valueOf(getConfig().getProperty("Width", "600"));
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    protected Integer getHeight()
    {
        return Integer.valueOf(getConfig().getProperty("Height", "400"));
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    protected String getTitle()
    {
        return getConfig().getProperty("Title");
    }
}
