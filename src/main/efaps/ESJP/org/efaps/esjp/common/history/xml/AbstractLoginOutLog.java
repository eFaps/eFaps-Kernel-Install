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

package org.efaps.esjp.common.history.xml;

import javax.xml.bind.annotation.XmlElement;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.history.IHistoryHtml;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b76aac71-3387-4d0a-839c-51d94a1b530b")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractLoginOutLog
    extends AbstractHistoryLog
    implements IHistoryHtml
{
    /**
     * Name of the login.
     */
    @XmlElement(name = "userName")
    private String userName;

    /**
     * SessionID.
     */
    @XmlElement(name = "sessionID")
    private String sessionID;

    /**
     * IpAddress.
     */
    @XmlElement(name = "ipAddress")
    private String ipAddress;

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder getDescriptionColumnValue()
        throws EFapsException
    {

        final StringBuilder ret = new StringBuilder();
        ret.append("userName: ").append(getUserName()).append("<br/>");
        ret.append("ipAddress: ").append(getIpAddress()).append("<br/>");
        ret.append("sessionID: ").append(getSessionID());
        return ret;
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
