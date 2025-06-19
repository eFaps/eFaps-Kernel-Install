/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.common.history;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("b3ef01f5-2be2-43b3-9f2f-39fc1ba8dd32")
@EFapsApplication("eFaps-Kernel")
public class DeleteOverwriteTrigger
{

    private static final Logger LOG = LoggerFactory.getLogger(DeleteOverwriteTrigger.class);

    public Return execute(final Parameter parameter)
        throws EFapsException
    {
        final var instance = parameter.getInstance();
        if (InstanceUtils.isValid(instance)) {
            final var type = instance.getType();
            if (type.isCheckStatus()) {

                final var sqlTable = type.getMainTable();
                var columnName = type.getStatusAttribute().getSqlColNames().get(0);

                final Context context = Context.getThreadContext();
                final StringBuilder cmd = new StringBuilder()
                                .append("SELECT")
                                .append(" DISTINCT ON (geninstid) geninstid,")
                                .append(" statusid, insttypeid, instid")
                                .append(" FROM ")
                                .append(sqlTable.getSqlTable())
                                .append(" WHERE ID = ")
                                .append(instance.getId());

                ConnectionResource con = null;
                try {
                    con = context.getConnectionResource();
                    LOG.debug("Querying StatusHistory with: {}", cmd);
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        final ResultSet rs = stmt.executeQuery(cmd.toString());
                        while (rs.next()) {
                            rs.getLong(2);
                            final long insttypeid = rs.getLong(3);
                            final long instid = rs.getLong(4);
                            Instance.get(Type.get(insttypeid), instid);
                        }
                        rs.close();
                    } finally {
                        if (stmt != null) {
                            stmt.close();
                        }
                    }
                } catch (final SQLException e) {
                    LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
                }
            } else {
                LOG.warn("Cannot overwrite delete due to not being a status dependend object: {}",
                                parameter.getInstance());
            }
        }
        return new Return();
    }
}
