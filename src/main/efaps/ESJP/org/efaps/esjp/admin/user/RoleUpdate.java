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


package org.efaps.esjp.admin.user;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminAccess;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("34badd85-b416-46fc-89ff-0f1e54c1eb9a")
@EFapsApplication("eFaps-Kernel")
public class RoleUpdate
    extends AbstractCommon
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RoleUpdate.class);

    /**
     * Role to be updated.
     */
    private final Role role;

    /**
     * @param _key UUID/Name of the role to be updated
     * @throws CacheReloadException on error
     */
    public RoleUpdate(final String _key)
        throws CacheReloadException
    {
        RoleUpdate.LOG.debug("start RoleUpdate for UUID/Name '{}'", _key);
        this.role = isUUID(_key) ? Role.get(UUID.fromString(_key)) : Role.get(_key);
        RoleUpdate.LOG.debug("initialized RoleUpdate for Role '{}'", this.role.getName());
    }

    /**
     * @param _key the UserIntferace Object to be connected to this role
     * @throws EFapsException on error
     */
    public void addUI(final String _key)
        throws EFapsException
    {
        RoleUpdate.LOG.debug("adding UserInterface '{}'", _key);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Direct);
        if (isUUID(_key)) {
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.UUID, _key);
        } else {
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.Name, _key);
        }
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        if (query.next()) {
            final Instance uiInst = query.getCurrentValue();
            if (uiInst.isValid()) {
                //Admin_UI_Access
                final QueryBuilder accQueryBldr = new QueryBuilder(Type.get(
                                UUID.fromString("7e59cebf-21ff-4ee1-8140-44139c90a658")));
                accQueryBldr.addWhereAttrEqValue("UserLink", this.role.getId());
                accQueryBldr.addWhereAttrEqValue("UILink", uiInst);
                final InstanceQuery accQuery = accQueryBldr.getQuery();
                if (accQuery.execute().isEmpty()) {
                    //Admin_UI_Access
                    final Insert insert = new Insert(Type.get(UUID.fromString("7e59cebf-21ff-4ee1-8140-44139c90a658")));
                    insert.add("UserLink", this.role.getId());
                    insert.add("UILink", uiInst);
                    insert.execute();
                }
            }
        }
    }

    /**
     * @param _key the UserIntferace Object to be removed from this role
     * @throws EFapsException on error
     */
    public void removeUI(final String _key)
        throws EFapsException
    {
        RoleUpdate.LOG.debug("removing UserInterface '{}'", _key);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Direct);
        if (isUUID(_key)) {
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.UUID, _key);
        } else {
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Direct.Name, _key);
        }
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        query.next();
        final Instance uiInst = query.getCurrentValue();
        if (uiInst.isValid()) {
            //Admin_UI_Access
            final QueryBuilder relQueryBldr = new QueryBuilder(
                            Type.get(UUID.fromString("7e59cebf-21ff-4ee1-8140-44139c90a658")));
            relQueryBldr.addWhereAttrEqValue("UserLink", this.role.getId());
            relQueryBldr.addWhereAttrEqValue("UILink", uiInst);
            final InstanceQuery relQuery = relQueryBldr.getQuery();
            relQuery.execute();
            if (relQuery.next()) {
                final Delete del = new Delete(relQuery.getCurrentValue());
                del.execute();
            }
        }
    }

    /**
     * @param _key the AccessSet to be connected to this role
     * @throws EFapsException on error
     */
    public void addAccessSet(final String _key)
        throws EFapsException
    {
        RoleUpdate.LOG.debug("adding AccessSet '{}'", _key);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet);
        if (isUUID(_key)) {
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet.UUID, _key);
        } else {
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet.Name, _key);
        }
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        if (query.next()) {
            final Instance uiInst = query.getCurrentValue();
            if (uiInst.isValid()) {
                //Admin_Access_AccessSet2UserAbstract
                final QueryBuilder accQueryBldr = new QueryBuilder(Type.get(
                                UUID.fromString("92975695-7a98-4221-bc9a-75c3dcb0e152")));
                accQueryBldr.addWhereAttrEqValue("UserAbstractLink", this.role.getId());
                accQueryBldr.addWhereAttrEqValue("AccessSetLink", uiInst);
                final InstanceQuery accQuery = accQueryBldr.getQuery();
                if (accQuery.execute().isEmpty()) {
                    //Admin_Access_AccessSet2UserAbstract
                    final Insert insert = new Insert(Type.get(UUID.fromString("92975695-7a98-4221-bc9a-75c3dcb0e152")));
                    insert.add("UserAbstractLink", this.role.getId());
                    insert.add("AccessSetLink", uiInst);
                    insert.execute();
                }
            }
        }
    }

    /**
     * @param _key the AccessSet to be removed from this role
     * @throws EFapsException on error
     */
    public void removeAccessSet(final String _key)
        throws EFapsException
    {
        RoleUpdate.LOG.debug("removing AccessSet '{}'", _key);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet);
        if (isUUID(_key)) {
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet.UUID, _key);
        } else {
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet.Name, _key);
        }
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        query.next();
        final Instance uiInst = query.getCurrentValue();
        if (uiInst.isValid()) {
            //Admin_Access_AccessSet2UserAbstract
            final QueryBuilder relQueryBldr = new QueryBuilder(
                            Type.get(UUID.fromString("92975695-7a98-4221-bc9a-75c3dcb0e152")));
            relQueryBldr.addWhereAttrEqValue("UserAbstractLink", this.role.getId());
            relQueryBldr.addWhereAttrEqValue("AccessSetLink", uiInst);
            final InstanceQuery relQuery = relQueryBldr.getQuery();
            relQuery.execute();
            if (relQuery.next()) {
                final Delete del = new Delete(relQuery.getCurrentValue());
                del.execute();
            }
        }
    }
}
