/*
 * Copyright 2003 - 2015 The eFaps Team
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
package org.efaps.esjp.common.eql;

import java.util.List;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;

/**
 * The Class InstanceIsKindOfSelect_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("a45ef8c8-28bf-4526-981f-001c754ac4a7")
@EFapsApplication("eFaps-Kernel")
public abstract class InstanceIsKindOfSelect_Base
    extends AbstractSelect
{

    @Override
    public void initialize(final List<Instance> _instances,
                           final String... _parameters)
                               throws EFapsException
    {
        final String typeStr = _parameters[0];
        Type type;
        if (UUIDUtil.isUUID(typeStr)) {
            type = Type.get(UUID.fromString(typeStr));
        } else {
            type = Type.get(typeStr);
        }
        if (_parameters.length == 1) {
            for (final Instance instance : _instances) {
                getValues().put(instance, instance.getType().isKindOf(type));
            }
        } else {
            final String select = _parameters[1];
            final MultiPrintQuery multi = new MultiPrintQuery(_instances);
            multi.addSelect(select);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final Instance instance = multi.getSelect(select);
                getValues().put(multi.getCurrentInstance(), instance.getType().isKindOf(type));
            }
        }
    }
}
