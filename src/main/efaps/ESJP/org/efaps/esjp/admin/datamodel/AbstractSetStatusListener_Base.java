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
package org.efaps.esjp.admin.datamodel;

import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("aeb223a6-912b-4a6d-8cb7-b04d83d2cd66")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractSetStatusListener_Base
    extends AbstractCommon
    implements ISetStatusListener
{
    /** The status. */
    private final Set<Status> status = new HashSet<>();

    @Override
    public void afterSetStatus(final Parameter _parameter,
                               final Instance _instance,
                               final Status _status)
        throws EFapsException
    {
        if (getStatus().contains(_status)) {
            after(_parameter, _instance, _status);
        }
    }

    /**
     * After.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _instance the instance
     * @param _status the status
     * @throws EFapsException on error
     */
    public abstract void after(final Parameter _parameter,
                               final Instance _instance,
                               final Status _status)
        throws EFapsException;

    @Override
    public int getWeight()
    {
        return 0;
    }

    /**
     * Getter method for the instance variable {@link #status}.
     *
     * @return value of instance variable {@link #status}
     * @throws EFapsException on error
     */
    public Set<Status> getStatus()
        throws EFapsException
    {
        return this.status;
    }
}
