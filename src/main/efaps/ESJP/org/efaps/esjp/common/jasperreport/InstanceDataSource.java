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


package org.efaps.esjp.common.jasperreport;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("a2fcf326-26cf-4005-8328-94959742f99f")
public class InstanceDataSource
    extends InstanceDataSource_Base
{

    /**
     * Instantiates a new instance data source.
     *
     * @param _instance the instance
     * @param _jasperReport the jasper report
     */
    public InstanceDataSource(final Instance _instance,
                              final String _jasperReport)
    {
        super(_instance, _jasperReport);
    }
}
