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

import org.efaps.admin.datamodel.IJaxb;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.esjp.common.history.xml.AbstractConnectLog;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AbstractInstObj;
import org.efaps.esjp.common.history.xml.AttributeValue;
import org.efaps.esjp.common.history.xml.ConnectInstObj;
import org.efaps.esjp.common.history.xml.ConnectLog;
import org.efaps.esjp.common.history.xml.CreateLog;
import org.efaps.esjp.common.history.xml.DisconnectLog;
import org.efaps.esjp.common.history.xml.HistoryInstObj;
import org.efaps.esjp.common.history.xml.InstObj;
import org.efaps.esjp.common.history.xml.LoginLog;
import org.efaps.esjp.common.history.xml.LogoutLog;
import org.efaps.esjp.common.history.xml.UpdateLog;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("8215b397-73b0-4ba8-a0d6-6b33118367b3")
@EFapsRevision("$Rev$")
public abstract class HistoryAttribute_Base
    implements IJaxb
{

    @Override
    public Class<?>[] getClasses()
    {
        return new Class<?>[] { AbstractHistoryLog.class, AbstractConnectLog.class, ConnectLog.class,
                                DisconnectLog.class, AbstractInstObj.class, InstObj.class, HistoryInstObj.class,
                                ConnectInstObj.class, AttributeValue.class, UpdateLog.class, CreateLog.class,
                                LoginLog.class, LogoutLog.class };
    }

    @Override
    public String getUISnipplet(final TargetMode _mode,
                                final UIValue _value)
    {
        // NOT USED IN THIS CASE
        return "";
    }
}
