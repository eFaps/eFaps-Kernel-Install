/*
 * Copyright 2003 - 2010 The eFaps Team
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


package org.efaps.esjp.admin.user;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("34badd85-b416-46fc-89ff-0f1e54c1eb9a")
@EFapsRevision("$Rev$")
public class RoleUpdate
{
    /**
     * Role to be updated.
     */
    private final Role role;

    /**
     * @param _uuid UUID of the role to be updated
     * @throws CacheReloadException
     */
    public RoleUpdate(final String _uuid)
        throws CacheReloadException
    {
        this.role = Role.get(UUID.fromString(_uuid));
        System.out.println(this.role);
    }


    /**
     * @param _uuid the UserIntferace Object to be connected to this role
     * @throws EFapsException on error
     */
    public void addUI(final String _uuid)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Direct);
        queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.UUID, _uuid);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        query.next();
        final Instance uiInst = query.getCurrentValue();
        if (uiInst.isValid()) {
            //Admin_UI_Access
            final Insert insert = new Insert(Type.get(UUID.fromString("7e59cebf-21ff-4ee1-8140-44139c90a658")));
            insert.add("UserLink", this.role.getId());
            insert.add("UILink", uiInst.getId());
            insert.execute();
        }
    }


    public void removeUI(final String _uuid)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Direct);
        queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.UUID, _uuid);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        query.next();
        final Instance uiInst = query.getCurrentValue();
        if (uiInst.isValid()) {
          //Admin_UI_Access
            final QueryBuilder relQueryBldr = new QueryBuilder(
                            Type.get(UUID.fromString("7e59cebf-21ff-4ee1-8140-44139c90a658")));
            relQueryBldr.addWhereAttrEqValue("UserLink", this.role.getId());
            relQueryBldr.addWhereAttrEqValue("UILink", uiInst.getId());
            final InstanceQuery relQuery = relQueryBldr.getQuery();
            relQuery.execute();
            if (relQuery.next()) {
                final Delete del = new Delete(relQuery.getCurrentValue());
                del.execute();
            }
        }
    }
}
