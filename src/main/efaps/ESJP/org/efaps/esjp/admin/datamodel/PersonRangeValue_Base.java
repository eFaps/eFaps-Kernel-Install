/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.esjp.admin.datamodel;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * A Range Value esjp that is used to get the map for Person.
 *
 * @author The eFaps Team
 */
@EFapsUUID("a4eb532a-6503-4c20-90d5-7279f2633060")
@EFapsApplication("eFaps-Kernel")
public abstract class PersonRangeValue_Base
    extends RangesValue
{

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isSelected(final Parameter _parameter,
                                 final RangeValueOption _option)
        throws EFapsException
    {
        boolean ret = false;
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
            ret = _option.getValue().equals(Context.getThreadContext().getPerson().getId());
        }
        return ret;
    }
}
