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
package org.efaps.esjp.common.jasperreport;

import java.util.concurrent.TimeUnit;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.container.entries.InternalCacheEntry;
import org.joda.time.DateTime;

import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * The Class AbstractCachedReport_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("c0142f24-6881-4634-bd5c-826262b49c17")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractCachedReport_Base
    extends AbstractCommon
{

    /**
     * Key used to store the filters in the context.
     */
    protected static final String CACHENAME = AbstractCachedReport.class.getName() + ".Cache";

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    public Cache<String, JRRewindableDataSource> getCache()
    {
        //
        return InfinispanCache.get().getIgnReCache(AbstractCachedReport.CACHENAME);
    }

    /**
     * Gets the cache key.
     *
     * @return the cache key
     * @throws EFapsException the e faps exception
     */
    protected String getCacheKey()
        throws EFapsException
    {
        return Context.getThreadContext().getPerson().getId() + ":" + getClass().getName();
    }

    /**
     * Checks if is cached.
     *
     * @return true, if is cached
     * @throws EFapsException the e faps exception
     */
    public boolean isCached()
        throws EFapsException
    {
        return getCache().containsKey(getCacheKey());
    }

    /**
     * Gets the data source from cache.
     *
     * @return the data source from cache
     * @throws EFapsException the e faps exception
     */
    public JRRewindableDataSource getDataSourceFromCache()
        throws EFapsException
    {
        return getCache().get(getCacheKey());
    }

    /**
     * Clear cache.
     *
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
     * @param _dataSource the _data source
     * @throws EFapsException the e faps exception
     */
    public void cache(final JRRewindableDataSource _dataSource)
        throws EFapsException
    {
        getCache().put(getCacheKey(), _dataSource, getLifespan(), getTimeUnit());
    }

    /**
     * Gets the lifespan.
     *
     * @return the lifespan
     * @throws EFapsException
     */
    protected Long getLifespan()
        throws EFapsException
    {
        return Long.valueOf(5);
    }

    /**
     * Gets the time unit.
     *
     * @return the time unit
     */
    protected TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }

    /**
     * Gets the expiry time.
     *
     * @return the expiry time
     * @throws EFapsException the e faps exception
     */
    public DateTime getExpiryTime()
        throws EFapsException
    {
        DateTime ret = null;
        if (isCached()) {
            final long time = ((InternalCacheEntry<?, ?>) getCache().getAdvancedCache().getCacheEntry(getCacheKey()))
                            .getExpiryTime();
            ret = new DateTime(time);
        }
        return ret;
    }

    /**
     * Gets the cached time.
     *
     * @return the cached time
     * @throws EFapsException the e faps exception
     */
    public DateTime getCachedTime()
        throws EFapsException
    {
        DateTime ret = getExpiryTime();
        if (ret != null) {
            switch (getTimeUnit()) {
                case MINUTES:
                    ret = ret.minusMinutes(getLifespan().intValue());
                    break;
                default:
                    break;
            }
        }
        return ret;
    }
}
