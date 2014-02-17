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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.history;

import java.util.Date;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.InstObj;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("2e099cd6-eafe-49df-84f1-0a4a18235d6e")
@EFapsRevision("$Rev$")
public abstract class AbstractHistoryTrigger_Base
    extends AbstractCommon
{

    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractHistoryTrigger.class);

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final AbstractHistoryLog log = getLogObject(_parameter);
        log.setDate(new Date());
        final Instance instance = _parameter.getInstance();

        final InstObj instObj = new InstObj();
        instObj.setOid(instance.getOid());
        instObj.setTypeUUID(instance.getType().getUUID());
        log.setInstance(instObj);

        add2LogObject(_parameter, log);

        final Insert insert = new Insert(getHistoryType());
        insert.add(CICommon.HistoryAbstract.GeneralInstanceLink, getHistoryInstance(_parameter).getGeneralId());
        insert.add(CICommon.HistoryAbstract.Value, log);
        insert.executeWithoutTrigger();

        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return instance that belongs to the HistoryTrigger
     * @throws EFapsException on error
     */
    protected Instance getHistoryInstance(final Parameter _parameter)
        throws EFapsException
    {
        return _parameter.getInstance();
    }

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
