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

package org.efaps.esjp.admin.common.systemconfiguration;

import java.util.ArrayList;
import java.util.Arrays;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.admin.index.LucenceQueryBuilder;

/**
 * Add additional System Configuration attributes that belongto the kernel but
 * are not invoked from the kernel jar but from the esjps and thereforte can be
 * stored ina an esjp also.
 *
 * @author The eFaps Team
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("7b4c9ebc-abec-4b02-a6e4-c03a7e4370b4")
public final class KernelConfigurations
{
    /** The base. */
    public static final String  BASE = "org.efaps.kernel.";

    /** See description. */
    public static final PropertiesSysConfAttribute ACCESS4OBJECT = new PropertiesSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key(BASE + "AccessCheck4Object")
                    .concatenate(true)
                    .description("Properties to configure the Access for Object mechanism.\n"
                                    + "TYPE.Grant.AccessSet: PropertyMap, UUID or name\n"
                                    + "TYPE.Grant.Person.InRole: PropertyMap, UUID or name\n"
                                    + "TYPE.Grant.User: PropertyMap, UUID or name\n"
                                    + "TYPE.Insert.AccessSet4Creator: AccesSet to be assigned for the creator\n"
                                    + "TYPE.Insert.User: PropertyMap, UUID or name\n"
                                    + "TYPE.Insert.AccessSet: PropertyMap Map, UUID or name\n"
                                    + "TYPE.SimpleAccess4Type.User: PropertyMap, UUID or name\n"
                                    + "TYPE.Delete.User: PropertyMap, UUID or name\n"
                                    + "TYPE.Delete.AccessSet: PropertyMap, UUID or name"
                                    );

    /** See description. */
    public static final StringSysConfAttribute INDEXBASEFOLDER = new StringSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key(BASE + "index.Basefolder")
                    .defaultValue("/eFaps/index")
                    .description("The base folder where the different indexes will be stored in.");

    /** See description. */
    public static final ListSysConfAttribute INDEXLANG = new ListSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key(BASE + "index.Languages")
                    .defaultValue(new ArrayList<>(Arrays.asList(new String[] { "en", "es" })))
                    .description("List of languages the indexes willbe created for.");

    /** See description. */
    public static final StringSysConfAttribute INDEXQUERYBLDR = new StringSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key(BASE + "index.QueryBuilder")
                    .defaultValue(LucenceQueryBuilder.class.getName())
                    .description("Class name of the class invoked to build Lucene Queries.");

    /**
     * Instantiates a new kernel configurations.
     */
    private KernelConfigurations()
    {
    }
}
