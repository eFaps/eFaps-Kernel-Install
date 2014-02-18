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
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("5cd048cd-1ee0-47cd-8c8e-1480d18b5dae")
@EFapsRevision("$Rev$")
public class ProgramUtil
    extends AbstractUtil
{
    /**
     * Delete all events related to Attributes, Commands and Menus.
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    public void removeProgram(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            // Admin_Program_Java
            final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString("11043a35-f73c-481c-8c77-00306dbce824"));
            queryBldr.addWhereAttrEqValue("Name", _parameter.getParameterValue("valueField"));
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            if (query.next()) {
                final Instance prgInst = query.getCurrentValue();
                // Admin_Program_JavaClass
                final QueryBuilder classQueryBldr = new QueryBuilder(
                                UUID.fromString("9118e1e3-ed4c-425d-8578-8d1f1d385110"));
                classQueryBldr.addWhereAttrEqValue("ProgramLink", prgInst);
                final InstanceQuery classQuery = classQueryBldr.getQuery();
                classQuery.execute();
                while (classQuery.next()) {
                    final Delete del = new Delete(classQuery.getCurrentValue());
                    del.execute();
                }
                final Delete del = new Delete(prgInst);
                del.execute();
            }
        }
    }
}
