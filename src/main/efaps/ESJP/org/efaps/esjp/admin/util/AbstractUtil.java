/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.esjp.admin.util;

import org.efaps.admin.KernelSettings;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.esjp.common.AbstractCommon_Base;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("8de7e7ff-d1ae-4326-a577-a0379465676d")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractUtil
    extends AbstractCommon_Base
{

    /**
     * @return true if access
     * @throws EFapsException on error
     */
    protected boolean checkAccess()
        throws EFapsException
    {
        return Context.getThreadContext().getPerson().isAssigned(Role.get(KernelSettings.USER_ROLE_ADMINISTRATION));
    }
}
