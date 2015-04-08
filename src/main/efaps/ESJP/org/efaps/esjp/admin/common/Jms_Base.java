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

package org.efaps.esjp.admin.common;

import java.util.UUID;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("30a8dff1-e0e3-43ff-bf7b-3ee23a580ee1")
@EFapsApplication("eFaps-Kernel")
public abstract class Jms_Base
{

    public void insertOrUpdate(final String _typeUuid,
                               final String _name,
                               final String _connectionFactoryJNDI,
                               final String _destinationJNDI,
                               final String _esjpName)
        throws EFapsException
    {
        final UUID typeUUID = UUID.fromString(_typeUuid);
        Instance esjpInst = null;
        if (_esjpName != null && !_esjpName.isEmpty()) {
            final QueryBuilder esjpQueryBldr = new QueryBuilder(CIAdminProgram.Java);
            esjpQueryBldr.addWhereAttrEqValue(CIAdminProgram.Java.Name, _esjpName);
            final InstanceQuery esjpQuery = esjpQueryBldr.getQuery();
            esjpQuery.execute();
            if (esjpQuery.next()) {
                 esjpInst = esjpQuery.getCurrentValue();
            }
        }

        final QueryBuilder queryBldr = new QueryBuilder(typeUUID);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.JmsAbstract.Name, _name);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        final Update update;
        if (query.next()) {
            update = new Update(query.getCurrentValue());
        } else {
            update = new Insert(typeUUID);
        }
        update.add(CIAdminCommon.JmsAbstract.Name, _name);
        update.add(CIAdminCommon.JmsAbstract.ConnectionFactoryJNDI, _connectionFactoryJNDI);
        update.add(CIAdminCommon.JmsAbstract.DestinationJNDI, _destinationJNDI);
        if (esjpInst != null && esjpInst.isValid()) {
            update.add(CIAdminCommon.JmsAbstract.ESJPLink, esjpInst.getId());
        }
        update.executeWithoutAccessCheck();

    }
}
