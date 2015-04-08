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

import java.util.Date;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AbstractLoginOutLog;
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
@EFapsUUID("43bf53c8-83be-49ce-9d67-e68fa7c7f34f")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractLoginOutHistory_Base
    extends AbstractHistory
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLoginOutHistory.class);

    /**
     * User login name.
     */
    private String userName;

    /**
     * SessionID.
     */
    private String sessionID;

    /**
     * IP Address.
     */
    private String ipAddress;

    /**
     * @param _userName username
     * @param _sessionID    id of the session
     * @param _ipAddress    ip adress
     * @throws EFapsException on error
     */
    public void register(final String _userName,
                         final String _sessionID,
                         final String _ipAddress)
        throws EFapsException
    {
        setUserName(_userName);
        setSessionID(_sessionID);
        setIpAddress(_ipAddress);
        final AbstractHistoryLog log = getLogObject(null);
        log.setDate(new Date());
        final Instance instance = getPersonInstance(_userName);

        final InstObj instObj = new InstObj();
        instObj.setOid(instance.getOid());
        instObj.setTypeUUID(instance.getType().getUUID());
        log.setInstance(instObj);

        add2LogObject(null, log);

        final Insert insert = new Insert(getHistoryType());
        insert.add(CICommon.HistoryAbstract.GeneralInstanceLink, instance.getGeneralId());
        insert.add(CICommon.HistoryAbstract.Value, log);
        insert.executeWithoutTrigger();
        AbstractLoginOutHistory_Base.LOG.debug("Registered for History: {}", log);
    }

    /**
     * @param _userName username
     * @return instance for the person
     * @throws EFapsException on error
     */
    protected Instance getPersonInstance(final String _userName)
        throws EFapsException
    {
        final Person person = Person.get(_userName);
        return Instance.get(CIAdminUser.Person.getType(), person.getId());
    }

    @Override
    protected void add2LogObject(final Parameter _parameter,
                                 final AbstractHistoryLog _log)
        throws EFapsException
    {
        final AbstractLoginOutLog log = (AbstractLoginOutLog) _log;
        log.setUserName(getUserName());
        log.setIpAddress(getIpAddress());
        log.setSessionID(getSessionID());
    }

    /**
     * Getter method for the instance variable {@link #userName}.
     *
     * @return value of instance variable {@link #userName}
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Setter method for instance variable {@link #userName}.
     *
     * @param _userName value for instance variable {@link #userName}
     */
    public void setUserName(final String _userName)
    {
        this.userName = _userName;
    }

    /**
     * Getter method for the instance variable {@link #sessionID}.
     *
     * @return value of instance variable {@link #sessionID}
     */
    public String getSessionID()
    {
        return this.sessionID;
    }

    /**
     * Setter method for instance variable {@link #sessionID}.
     *
     * @param _sessionID value for instance variable {@link #sessionID}
     */
    public void setSessionID(final String _sessionID)
    {
        this.sessionID = _sessionID;
    }

    /**
     * Getter method for the instance variable {@link #ipAddress}.
     *
     * @return value of instance variable {@link #ipAddress}
     */
    public String getIpAddress()
    {
        return this.ipAddress;
    }

    /**
     * Setter method for instance variable {@link #ipAddress}.
     *
     * @param _ipAddress value for instance variable {@link #ipAddress}
     */
    public void setIpAddress(final String _ipAddress)
    {
        this.ipAddress = _ipAddress;
    }
}
