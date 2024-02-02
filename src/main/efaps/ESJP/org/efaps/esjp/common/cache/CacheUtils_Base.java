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
package org.efaps.esjp.common.cache;

import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.QueryCache;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains some generic methods used by its subclasses.
 *
 * @author The eFaps Team
 * @version $Id: AbstractCommon_Base.java 9574 2016-06-07 00:23:17Z
 *          jan@moxter.net $
 */
@EFapsUUID("50b92f6d-8c95-4016-a1f7-db9f4b770516")
@EFapsApplication("eFaps-Kernel")
public abstract class CacheUtils_Base
    extends AbstractCommon
{

  private static final Logger LOG = LoggerFactory.getLogger(CacheUtils.class);
    /**
     * Clean keys form the QueryCache on a trigger.
     * @param _parameter parameter as passed by the efasp API
     * @return new empty return
     * @throws EFapsException on error
     */
    public Return cleanQueryCacheTrigger(final Parameter _parameter)
        throws EFapsException
    {
        final Map<Integer, String> caches = analyseProperty(_parameter, "CacheName");
        for (final String cacheName : caches.values()) {
          LOG.debug("Clean cache by name: {}", cacheName);
          QueryCache.cleanByKey(cacheName);
        }
        return new Return();
    }
}
