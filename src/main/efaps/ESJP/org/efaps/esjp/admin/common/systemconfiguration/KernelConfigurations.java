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

/**
 * Add additional System Configuration attributes that belongto the kernel but
 * are not invoked from the kernel jar but from the esjps and thereforte can be
 * stored ina an esjp also.
 *
 * @author The eFaps Team
 */
public final class KernelConfigurations
{

    /** See description. */
    public static final StringSysConfAttribute INDEXBASEFOLDER = new StringSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key("org.efaps.kernel.index.Basefolder")
                    .defaultValue("/eFaps/index")
                    .description("The base folder where the different indexes will be stored in.");

    /** See description. */
    public static final ListSysConfAttribute INDEXLANG = new ListSysConfAttribute()
                    .sysConfUUID(EFapsSystemConfiguration.UUID)
                    .key("org.efaps.kernel.index.Languages")
                    .defaultValue(new ArrayList<String>(Arrays.asList(new String[] { "en", "es" })))
                    .description("List of languages the indexes willbe created for.");

    /**
     * Instantiates a new kernel configurations.
     */
    private KernelConfigurations()
    {
    }
}