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

package org.efaps.esjp.admin.access;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("9e9bc652-f3e4-4c22-a091-07bd11615b16")
@EFapsApplication("eFaps-Kernel")
public class InsertOnlyAccessCheck_Base
    extends SimpleAccessCheckOnType
{

    @Override
    protected boolean checkAccess(final Parameter _parameter,
                                  final Instance _instance,
                                  final AccessType _accessType)
        throws EFapsException
    {
        boolean ret = false;
        if (AccessTypeEnums.READ.getAccessType().equals(_accessType)
                        || AccessTypeEnums.SHOW.getAccessType().equals(_accessType)) {
            ret = super.checkAccess(_parameter, _instance, _accessType);
        } else if (AccessTypeEnums.CREATE.getAccessType().equals(_accessType)) {
            ret = true;
        }
        return ret;
    }

    @Override
    protected Map<Instance, Boolean> checkAccess(final Parameter _parameter,
                                                 final List<?> _instances,
                                                 final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Boolean> ret;
        if (AccessTypeEnums.READ.getAccessType().equals(_accessType)
                        || AccessTypeEnums.SHOW.getAccessType().equals(_accessType)) {
            ret = super.checkAccess(_parameter, _instances, _accessType);
        } else {
            final boolean bolVal = AccessTypeEnums.CREATE.getAccessType().equals(_accessType);
            ret = new HashMap<Instance, Boolean>();
            for (final Object _instance : _instances) {
                ret.put((Instance) _instance, bolVal);
            }
        }
        return ret;
    }
}
