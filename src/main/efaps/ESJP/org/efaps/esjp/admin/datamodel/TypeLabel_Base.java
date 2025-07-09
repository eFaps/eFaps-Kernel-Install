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
package org.efaps.esjp.admin.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("9ed53ac5-6ae2-4c3c-896c-bd0b105d1ed4")
@EFapsApplication("eFaps-Kernel")
public abstract class TypeLabel_Base
{

    /** The Constant REQKEY. */
    private static final String REQKEY = TypeLabel.class.getName() + "ReqKey";

    /**
     * Gets the label field value.
     *
     * @param _parameter the parameter
     * @return the label field value
     * @throws EFapsException the e faps exception
     */
    @SuppressWarnings("unchecked")
    public Return getLabelFieldValue(final Parameter parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        if (Context.getThreadContext().getRequestAttribute("REST") != null) {
            final var uiValue = (IUIValue) parameter.get(ParameterValues.UIOBJECT);
            final var type = Type.get(uiValue.getInstance().getId());
            ret.put(ReturnValues.VALUES, type == null ? "" : type.getLabel());
            return ret;
        }

        final Map<Instance, String> map;
        if (Context.getThreadContext().containsRequestAttribute(REQKEY)) {
            map = (Map<Instance, String>) Context.getThreadContext().getRequestAttribute(REQKEY);
        } else {
            map = new HashMap<>();
            Context.getThreadContext().setRequestAttribute(REQKEY, map);
            final List<Instance> instances = (List<Instance>) parameter.get(ParameterValues.REQUEST_INSTANCES);
            for (final Instance instance : instances) {
                final Type type = Type.get(instance.getId());
                map.put(instance, type == null ? "" : type.getLabel());
            }
        }
        ret.put(ReturnValues.VALUES, map.get(parameter.getInstance()));
        return ret;
    }
}
