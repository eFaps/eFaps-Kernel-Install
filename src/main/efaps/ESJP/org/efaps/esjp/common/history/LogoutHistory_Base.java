/*
no * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.esjp.common.history;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.LogoutLog;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d0b737c4-cfa6-4924-b883-1d6efc9e00a3")
@EFapsApplication("eFaps-Kernel")
public abstract class LogoutHistory_Base
    extends AbstractLoginOutHistory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final String _userName,
                         final String _sessionID,
                         final String _ipAddress)
        throws EFapsException
    {
        if (!Context.isThreadActive()) {
            Context.begin(_userName);
            super.register(_userName, _sessionID, _ipAddress);
            Context.commit();
        } else {
            super.register(_userName, _sessionID, _ipAddress);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getHistoryType()
    {
        return CICommon.HistoryLogout.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractHistoryLog getLogObject(final Parameter _parameter)
        throws EFapsException
    {
        return new LogoutLog();
    }
}
