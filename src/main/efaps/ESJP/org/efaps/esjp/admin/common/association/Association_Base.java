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
package org.efaps.esjp.admin.common.association;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.eql.EQL;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.util.EFapsException;

@EFapsUUID("257439a4-70e8-4cad-bed9-2fa3b77c9865")
@EFapsApplication("eFaps-Kernel")
public abstract class Association_Base
{

    public Return getType4MappingOptionListFieldValue(final Parameter parameter)
        throws EFapsException
    {
        final List<DropDownPosition> values = new ArrayList<>();
        final var eval = EQL.builder()
                        .print()
                            .query(CIAdminDataModel.Type)
                        .select()
                            .attribute(CIAdminDataModel.Type.ID)
                        .evaluate();
        while (eval.next()) {
            final Long id = eval.get(CIAdminDataModel.Type.ID);
            final var type = Type.get(id);
            if (type != null && type.hasAssociation()) {
                final DropDownPosition val = new DropDownPosition(type.getId(), type.getName());
                values.add(val);
            }
        }
        values.sort((arg0, arg1) -> arg0.getLabel().compareTo(arg1.getLabel()));
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }
}
