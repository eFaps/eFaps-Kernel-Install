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
package org.efaps.esjp.admin.index;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.ISearch;
import org.efaps.admin.index.Indexer;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIAdminIndex;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ResultTableProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("02edb104-402e-4860-91cf-3102d5bfa1af")
@EFapsApplication("eFaps-Kernel")
public abstract class Search_Base
    implements ISearch
{

    /** The Constant CACHEKEY. */
    protected static final String CACHEKEY = Search.class.getName() + ".CacheKey";
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /** The query. */
    private String query;

    /** The initialized. */
    private boolean initialized;

    /** The result fields. */
    private Map<String, Collection<String>> resultFields;

    /** The result fields. */
    private Map<String, String> resultLabels;

    /**
     * Inits the.
     */
    private void init()
    {
        if (!this.initialized) {
            try {
                this.initialized = true;
                this.resultFields = new LinkedHashMap<>();
                this.resultLabels = new HashMap<>();
                // this should be replaced by an actual evaluation of which one
                // of the searches should be used
                final QueryBuilder indxQueryBldr = new QueryBuilder(CIAdminIndex.IndexSearch);
                indxQueryBldr.setLimit(1);
                final List<Instance> searches = indxQueryBldr.getCachedQuery(Search.CACHEKEY).execute();
                Instance searchInst = null;
                if (!searches.isEmpty()) {
                    searchInst = searches.get(0);
                }
                if (InstanceUtils.isValid(searchInst)) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminIndex.IndexSearchResultField);
                    queryBldr.addWhereAttrEqValue(CIAdminIndex.IndexSearchResultField.SearchLink, searchInst);
                    queryBldr.addOrderByAttributeAsc(CIAdminIndex.IndexSearchResultField.Priority);
                    final MultiPrintQuery multi = queryBldr.getCachedPrint(Search.CACHEKEY);
                    multi.setEnforceSorted(true);
                    multi.addAttribute(CIAdminIndex.IndexSearchResultField.Key,
                                    CIAdminIndex.IndexSearchResultField.Label);
                    multi.execute();
                    while (multi.next()) {
                        final String key = multi.getAttribute(CIAdminIndex.IndexSearchResultField.Key);
                        final String label = multi.getAttribute(CIAdminIndex.IndexSearchResultField.Label);
                        final QueryBuilder keyQueryBldr = new QueryBuilder(CIAdminIndex.IndexSearchResultFieldKey);
                        keyQueryBldr.addWhereAttrEqValue(CIAdminIndex.IndexSearchResultFieldKey.ResultFieldLink, multi
                                        .getCurrentInstance());
                        final MultiPrintQuery keyMulti = keyQueryBldr.getCachedPrint(Search.CACHEKEY);
                        keyMulti.addAttribute(CIAdminIndex.IndexSearchResultField.Key);
                        keyMulti.execute();
                        final Set<String> keys = new HashSet<>();
                        while (keyMulti.next()) {
                            final String keyTmp = keyMulti.<String>getAttribute(
                                            CIAdminIndex.IndexSearchResultFieldKey.Key);
                            if (EnumUtils.isValidEnum(Indexer.Key.class, keyTmp)) {
                                keys.add(keyTmp);
                            } else {
                                keys.add(DBProperties.getProperty(keyTmp));
                            }
                        }
                        this.resultFields.put(key, keys);
                        this.resultLabels.put(key, DBProperties.getProperty(label));
                    }
                }
            } catch (final EFapsException e) {
                LOG.error("Catched", e);
            }
        }
    }

    @Override
    public void setQuery(final String _query)
    {
        this.query = _query;
    }

    @Override
    public String getQuery()
    {
        return this.query;
    }

    @Override
    public Map<String, Collection<String>> getResultFields()
    {
        init();
        return this.resultFields;
    }

    @Override
    public Map<String, String> getResultLabel()
    {
        init();
        return this.resultLabels;
    }
}
