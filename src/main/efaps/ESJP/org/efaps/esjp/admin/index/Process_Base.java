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
package org.efaps.esjp.admin.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.efaps.admin.common.Association;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.index.IndexContext;
import org.efaps.admin.index.IndexDefinition;
import org.efaps.admin.index.Indexer;
import org.efaps.admin.index.Queue;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.eql.EQL;
import org.efaps.eql2.StmtFlag;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("cb0a815a-b40c-48c8-a2f5-6d19bab6ddcc")
@EFapsApplication("eFaps-Kernel")
public abstract class Process_Base
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Process.class);

    private static boolean indexing = false;

    /**
     * Re index.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return updateIndex(final Parameter _parameter)
        throws EFapsException
    {
        if (indexing) {
            LOG.info("Skipping update index due to running indexing");
        } else {
            final Map<Long, Map<Type, List<Instance>>> instanceMap = new HashMap<>();
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Company);
            for (final Instance inst : queryBldr.getQuery().executeWithoutAccessCheck()) {
                instanceMap.put(inst.getId(), new HashMap<>());
            }

            final Cache<String, String> cache = InfinispanCache.get().<String, String>getCache(Queue.CACHENAME);
            final Set<String> keys = new HashSet<>();
            for (final Entry<String, String> cachEntry : cache.entrySet()) {
                keys.add(cachEntry.getKey());
                final Instance inst = Instance.get(cachEntry.getValue());
                if (inst.getType().isCompanyDependent()) {
                    LOG.debug("Checking instance to be added {}", inst);
                    final PrintQuery print = new PrintQuery(inst);
                    print.addAttribute(inst.getType().getCompanyAttribute());
                    if (print.executeWithoutAccessCheck()) {
                        final Company company = print.getAttribute(inst.getType().getCompanyAttribute());
                        final Map<Type, List<Instance>> subMap = instanceMap.get(company.getId());
                        final List<Instance> instances;
                        if (subMap.containsKey(inst.getType())) {
                            instances = subMap.get(inst.getType());
                        } else {
                            instances = new ArrayList<>();
                            subMap.put(inst.getType(), instances);
                        }
                        instances.add(inst);
                    }
                } else if (inst.getType().hasAssociation()) {
                    LOG.debug("Checking instance to be added {}", inst);
                    final PrintQuery print = new PrintQuery(inst);
                    print.addAttribute(inst.getType().getAssociationAttribute());
                    if (print.executeWithoutAccessCheck()) {
                        final Association association = print.getAttribute(inst.getType().getAssociationAttribute());
                        for (final Company company : association.getCompanies()) {
                            final Map<Type, List<Instance>> subMap = instanceMap.get(company.getId());
                            final List<Instance> instances;
                            if (subMap.containsKey(inst.getType())) {
                                instances = subMap.get(inst.getType());
                            } else {
                                instances = new ArrayList<>();
                                subMap.put(inst.getType(), instances);
                            }
                            instances.add(inst);
                        }
                    }
                } else {
                    for (final Entry<Long, Map<Type, List<Instance>>> entry : instanceMap.entrySet()) {
                        final List<Instance> instances;
                        if (entry.getValue().containsKey(inst.getType())) {
                            instances = entry.getValue().get(inst.getType());
                        } else {
                            instances = new ArrayList<>();
                            entry.getValue().put(inst.getType(), instances);
                        }
                        instances.add(inst);
                    }
                }
            }
            final DirectoryProvider dirProvider = new DirectoryProvider();
            final AnalyzerProvider analyzerProvider = new AnalyzerProvider();
            for (final Entry<Long, Map<Type, List<Instance>>> entry : instanceMap.entrySet()) {
                for (final String language : KernelConfigurations.INDEXLANG.get()) {
                    final Directory directory = dirProvider.getDirectory(entry.getKey(), language,
                                    DirectoryProvider.INDEXPATH);
                    final Directory taxonomyDirectory = dirProvider.getDirectory(entry.getKey(), language,
                                    DirectoryProvider.TAXONOMYPATH);
                    final Analyzer analyzer = analyzerProvider.getAnalyzer(null, language);
                    for (final Entry<Type, List<Instance>> subentry : entry.getValue().entrySet()) {
                        final IndexContext indexContext = new IndexContext()
                                        .setDirectory(directory)
                                        .setTaxonomyDirectory(taxonomyDirectory)
                                        .setAnalyzer(analyzer)
                                        .setLanguage(language)
                                        .setCompanyId(entry.getKey());
                        Indexer.index(indexContext, subentry.getValue());
                    }
                }
            }
            // remove the reindexed keys from the cache
            for (final String key : keys) {
                cache.remove(key);
            }
        }
        return new Return();
    }

    /**
     * Re index.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return reIndex(final Parameter _parameter)
        throws EFapsException

    {
        try {
            indexing = true;
            final List<IndexDefinition> defs = IndexDefinition.get();
            final DirectoryProvider dirProvider = new DirectoryProvider();
            final AnalyzerProvider analyzerProvider = new AnalyzerProvider();

            final QueryBuilder compQueryBldr = new QueryBuilder(CIAdminUser.Company);
            for (final Instance compInst : compQueryBldr.getQuery().executeWithoutAccessCheck()) {
                for (final IndexDefinition def : defs) {
                    int offset = 0;
                    final int limit = 2000;
                    while (offset > -1) {
                        final Type type = Type.get(def.getUUID());
                        LOG.info("Getting {} {} - {} for indexing", type.getName(), offset, offset + limit);
                        final var bldr = EQL.builder().with(StmtFlag.COMPANYINDEPENDENT, StmtFlag.TRIGGEROFF)
                                        .print().query(def.getUUID().toString())
                                        .where();

                        if (type.isCompanyDependent()) {
                            bldr.attribute(type.getCompanyAttribute().getName()).eq(compInst);
                        }
                        if (type.hasAssociation()) {
                            bldr.attribute(type.getAssociationAttribute().getName()).eq(
                                            Association.evaluate(type, compInst.getId()).getId());
                        }
                        final var evaluator = bldr.select().attribute("ID").as("sorter")
                                        .orderBy("sorter").limit(limit).offset(offset)
                                        .evaluate();
                        final List<Instance> instances = new ArrayList<>();
                        while (evaluator.next()) {
                            instances.add(evaluator.inst());
                        }
                        if (instances.isEmpty()) {
                            offset = -1;
                        } else {
                            offset = offset + limit;
                        }
                        for (final String language : KernelConfigurations.INDEXLANG.get()) {
                            final Directory directory = dirProvider.getDirectory(compInst.getId(), language,
                                            DirectoryProvider.INDEXPATH);
                            final Directory taxonomyDirectory = dirProvider.getDirectory(compInst.getId(), language,
                                            DirectoryProvider.TAXONOMYPATH);

                            final Analyzer analyzer = analyzerProvider.getAnalyzer(null, language);
                            final IndexContext indexContext = new IndexContext()
                                            .setDirectory(directory)
                                            .setTaxonomyDirectory(taxonomyDirectory)
                                            .setAnalyzer(analyzer)
                                            .setLanguage(language)
                                            .setCompanyId(compInst.getId());
                            Indexer.index(indexContext, instances);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new EFapsException("", e);
        } finally {
            indexing = false;
        }
        return new Return();
    }
}
