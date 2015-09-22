/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev: 9459 $
 * Last Changed:    $Date: 2013-05-17 15:09:07 -0500 (vie, 17 may 2013) $
 * Last Changed By: $Author: jorge.cueva@moxter.net $
 */

package org.efaps.esjp.common.uitable;

import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsNoUpdate;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * This class will be replaced by the MultiPrint in Webapp-install. It serves as
 * a placeholder to be able to link to the esjp during a first time
 * installation.
 *
 * @author The eFaps Team
 * @version $Id: MultiPrint.java 9459 2013-05-17 20:09:07Z
 *          jorge.cueva@moxter.net $
 */
@EFapsUUID("cbab3ae6-fe28-4604-838c-4c6d260156fb")
@EFapsApplication("eFaps-Kernel")
@EFapsNoUpdate
public class MultiPrint
{

    public List<Instance> getInstances(final Parameter _parameter)
        throws EFapsException
    {
        return null;
    }

    protected void add2QueryBldr(final Parameter _parameter,
                                 final QueryBuilder _queryBldr)
        throws EFapsException
    {

    }
}
