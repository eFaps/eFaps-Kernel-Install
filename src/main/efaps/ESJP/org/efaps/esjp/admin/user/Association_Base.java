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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Insert;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
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
@EFapsUUID("28a90294-bed6-4bfb-8775-be71b568a257")
@EFapsRevision("$Rev$")
public abstract class Association_Base
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(RoleUpdate.class);


    public Return insertPostTrigger4PersonRelation(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        Long userId = null;
        final Map<Attribute, Object> values = (Map<Attribute, Object>) _parameter.get(ParameterValues.NEW_VALUES);
        for (final Entry<Attribute,Object> entry : values.entrySet()) {
            if ("UserFromLink".equals(entry.getKey().getName())) {
                userId = (Long) entry.getValue();
                break;
            }
        }
        if (userId != null) {
            final List<Long> roleIds = new ArrayList<Long>();
            final List<Long> groupIds = new ArrayList<Long>();

            final QueryBuilder groupQueryBldr = new QueryBuilder(CIAdminUser.Person2Group);
            groupQueryBldr.addWhereAttrEqValue(CIAdminUser.Person2Group.UserFromLink, userId);
            final MultiPrintQuery groupMulti = groupQueryBldr.getPrint();
            groupMulti.addAttribute(CIAdminUser.Person2Group.UserToLink);
            groupMulti.executeWithoutAccessCheck();
            while (groupMulti.next()) {
                groupIds.add(groupMulti.<Long>getAttribute(CIAdminUser.Person2Group.UserToLink));
            }
            if (!groupIds.isEmpty()) {
                final QueryBuilder roleQueryBldr = new QueryBuilder(CIAdminUser.Person2Role);
                roleQueryBldr.addWhereAttrEqValue(CIAdminUser.Person2Role.UserFromLink, userId);
                final MultiPrintQuery roleMulti = roleQueryBldr.getPrint();
                roleMulti.addAttribute(CIAdminUser.Person2Role.UserToLink);
                roleMulti.executeWithoutAccessCheck();
                while (roleMulti.next()) {
                    roleIds.add(roleMulti.<Long>getAttribute(CIAdminUser.Person2Role.UserToLink));
                }

                for (final Long roleId : roleIds) {
                    for (final Long groupId : groupIds) {
                        final QueryBuilder assQueryBldr = new QueryBuilder(CIAdminUser.Association);
                        assQueryBldr.addWhereAttrEqValue(CIAdminUser.Association.GroupLink, groupId);
                        assQueryBldr.addWhereAttrEqValue(CIAdminUser.Association.RoleLink, roleId);
                        final InstanceQuery query = assQueryBldr.getQuery();
                        query.executeWithoutAccessCheck();
                        if (!query.next()) {
                            Association_Base.LOG.debug("Adding new Association with GroupID: {}, RoleID {}", groupId, roleId);
                            final Insert insert = new Insert(CIAdminUser.Association);
                            insert.add(CIAdminUser.Association.GroupLink, groupId);
                            insert.add(CIAdminUser.Association.RoleLink, roleId);
                            insert.executeWithoutTrigger();
                        }
                    }
                }
            }
        }
        return ret;
    }
}
