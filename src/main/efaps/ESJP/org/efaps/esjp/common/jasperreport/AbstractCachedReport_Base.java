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

import org.efaps.admin.event.Parameter;
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
        return InfinispanCache.get().getIgnReCache(AbstractCachedReport.CACHENAME);
    }

    /**
     * Gets the cache key.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the cache key
     * @throws EFapsException the e faps exception
     */
    protected String getCacheKey(final Parameter _parameter)
        throws EFapsException
    {
        return Context.getThreadContext().getPerson().getId() + ":" + getClass().getName();
    }

    /**
     * Checks if is cached.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return true, if is cached
     * @throws EFapsException the e faps exception
     */
    public boolean isCached(final Parameter _parameter)
        throws EFapsException
    {
        return getCache().containsKey(getCacheKey(_parameter));
    }

    /**
     * Gets the data source from cache.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the data source from cache
     * @throws EFapsException the e faps exception
     */
    public JRRewindableDataSource getDataSourceFromCache(final Parameter _parameter)
        throws EFapsException
    {
        return getCache().get(getCacheKey(_parameter));
    }

    /**
     * Clear cache.
     * @param _parameter Parameter a as passed by the eFaps API
     * @throws EFapsException the e faps exception
     */
    public void clearCache(final Parameter _parameter)
        throws EFapsException
    {
        getCache().remove(getCacheKey(_parameter));
    }

    /**
     * Cache.
     * @param _parameter Parameter a as passed by the eFaps API
     * @param _dataSource the _data source
     * @throws EFapsException the e faps exception
     */
    public void cache(final Parameter _parameter,
                      final JRRewindableDataSource _dataSource)
        throws EFapsException
    {
        getCache().put(getCacheKey(_parameter), _dataSource, getLifespan(_parameter), getTimeUnit(_parameter));
    }

    /**
     * Gets the lifespan.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the lifespan
     * @throws EFapsException on error
     */
    protected Long getLifespan(final Parameter _parameter)
        throws EFapsException
    {
        return Long.valueOf(5);
    }

    /**
     * Gets the time unit.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the time unit
     */
    protected TimeUnit getTimeUnit(final Parameter _parameter)
    {
        return TimeUnit.MINUTES;
    }

    /**
     * Gets the expiry time.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the expiry time
     * @throws EFapsException the e faps exception
     */
    public DateTime getExpiryTime(final Parameter _parameter)
        throws EFapsException
    {
        DateTime ret = null;
        if (isCached(_parameter)) {
            final long time = ((InternalCacheEntry<?, ?>) getCache().getAdvancedCache()
                            .getCacheEntry(getCacheKey(_parameter))).getExpiryTime();
            ret = new DateTime(time);
        }
        return ret;
    }

    /**
     * Gets the cached time.
     * @param _parameter Parameter a as passed by the eFaps API
     * @return the cached time
     * @throws EFapsException the e faps exception
     */
    public DateTime getCachedTime(final Parameter _parameter)
        throws EFapsException
    {
        DateTime ret = getExpiryTime(_parameter);
        if (ret != null) {
            switch (getTimeUnit(_parameter)) {
                case MINUTES:
                    ret = ret.minusMinutes(getLifespan(_parameter).intValue());
                    break;
                default:
                    break;
            }
        }
        return ret;
    }
}
