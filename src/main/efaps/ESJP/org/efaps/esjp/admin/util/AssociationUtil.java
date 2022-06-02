/*
 * Copyright 2003 - 2022 The eFaps Team
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
 */

package org.efaps.esjp.admin.util;

import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("d38d19fc-dce2-4026-aaac-d83250f4fdd0")
@EFapsApplication("eFaps-Kernel")
public class AssociationUtil
    extends AbstractUtil
{
    private static final Logger LOG = LoggerFactory.getLogger(AssociationUtil.class);

    public void assignAssociation(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            final String[] keys = _parameter.getParameterValues("keyField");
            final String[] values = _parameter.getParameterValues("valueField");
            int i = 0;
            String typeStr = null;
            String associationId = null;
            for (final var key: keys) {
                if (key.equals("type")) {
                    typeStr = values[i];
                }
                if (key.equals("associationId")) {
                    associationId = values[i];
                }
                i++;
            }
            LOG.info("assignAssociation =>  type: '{}', associationId: '{}'", typeStr, associationId);

            final var type = Type.get(typeStr);

            final Context context = Context.getThreadContext();
            final StringBuilder cmd = new StringBuilder()
                            .append("UPDATE ")
                            .append(type.getMainTable().getSqlTable())
                            .append(" SET ")
                            .append(type.getAssociationAttribute().getSqlColNames().get(0))
                            .append(" = ")
                            .append(associationId)
                            .append(" WHERE ")
                            .append(type.getAssociationAttribute().getSqlColNames().get(0))
                            .append(" IS NULL");

            ConnectionResource con = null;
            try {
                con = context.getConnectionResource();
                LOG.info("Updating Association with: {}", cmd);
                Statement stmt = null;
                try {
                    stmt = con.createStatement();
                    stmt.executeUpdate(cmd.toString());
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            } catch (final SQLException e) {
                LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
            }
        }
    }
}
