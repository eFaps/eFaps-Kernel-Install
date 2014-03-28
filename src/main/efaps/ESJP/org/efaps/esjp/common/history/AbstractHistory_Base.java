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


package org.efaps.esjp.common.history;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f3618d8d-b62b-434e-a38a-ea23c928e5ae")
@EFapsRevision("$Rev$")
public abstract class AbstractHistory_Base
    extends AbstractCommon
{
    /**
     * @return the Type for the History insert
     */
    protected abstract Type getHistoryType();

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @param _log log to be added to
     * @throws EFapsException on error
     */
    protected abstract void add2LogObject(final Parameter _parameter,
                                          final AbstractHistoryLog _log)
        throws EFapsException;

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return the log object for this trigger
     * @throws EFapsException on error
     */
    protected abstract AbstractHistoryLog getLogObject(final Parameter _parameter)
        throws EFapsException;
}
