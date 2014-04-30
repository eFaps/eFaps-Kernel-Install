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


package org.efaps.esjp.admin.util;

import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e1d69c77-0498-4c84-914d-b9a6ac4168c5")
@EFapsRevision("$Rev$")
public class DBPropertyUtil
    extends AbstractUtil
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DBPropertyUtil.class);

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @throws EFapsException
     */
    public void removeDBProperties(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            final String value = _parameter.getParameterValue("valueField");
            // Admin_Common_DBPropertiesBundle
            final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString("03c51ba4-00a6-47ac-b987-270308fe68d8"));
            if (isUUID(value)) {
                queryBldr.addWhereAttrEqValue("UUID", value);
            } else {
                queryBldr.addWhereAttrEqValue("Name", value);
            }
            final InstanceQuery query = queryBldr.getQuery();
            if (query.execute().isEmpty()) {
                DBPropertyUtil.LOG.info("No bundle found for value: '{}'", value);
            } else {
                query.next();
                final Instance bundleInst = query.getCurrentValue();
                DBPropertyUtil.LOG.info("Found bundle  '{}'", bundleInst);

                // Admin_Common_DBProperties
                final QueryBuilder attrQueryBldr = new QueryBuilder(
                                UUID.fromString("8c839380-ea1c-44a7-97aa-67934b94aca9"));
                attrQueryBldr.addWhereAttrEqValue("BundleID", bundleInst);
                final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery("ID");

                // Admin_Common_DBPropertiesLocal
                final QueryBuilder localQueryBldr = new QueryBuilder(
                                UUID.fromString("da18fd98-8e42-44ba-be74-443aecc87b4d"));
                localQueryBldr.addWhereAttrInQuery("PropertyID", attrQuery);
                final InstanceQuery localQuery = localQueryBldr.getQuery();
                localQuery.execute();
                DBPropertyUtil.LOG.info("Removing the local DBProperties");
                while (localQuery.next()) {
                    DBPropertyUtil.LOG.debug("Removing local DBProperties: {}", localQuery.getCurrentValue());
                    new Delete(localQuery.getCurrentValue()).execute();
                }

                // Admin_Common_DBProperties
                final QueryBuilder propQueryBldr = new QueryBuilder(
                                UUID.fromString("8c839380-ea1c-44a7-97aa-67934b94aca9"));
                propQueryBldr.addWhereAttrEqValue("BundleID", bundleInst);
                final InstanceQuery propQuery = propQueryBldr.getQuery();
                propQuery.execute();
                DBPropertyUtil.LOG.info("Removing the DBProperties");

                while (propQuery.next()) {
                    DBPropertyUtil.LOG.debug("Removing DBProperties: {}", propQuery.getCurrentValue());
                    new Delete(propQuery.getCurrentValue()).execute();
                }
            }
        }
    }
}
