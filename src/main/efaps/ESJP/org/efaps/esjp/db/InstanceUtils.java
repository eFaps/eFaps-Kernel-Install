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
package org.efaps.esjp.db;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIType;
import org.efaps.db.Instance;

/**
 * The Class InstanceUtils.
 *
 * @author The eFaps Team
 */
@EFapsUUID("2980158f-ed87-46c0-836f-08726b9ea13e")
@EFapsApplication("eFaps-Kernel")
public final class InstanceUtils
{

    /**
     * Instantiates a new instance utils.
     */
    private InstanceUtils()
    {
    }

    /**
     * Checks if Instance is kind of.
     *
     * @param _instance the instance
     * @param _ciType the _ci type
     * @return true, if is valid
     */
    public static boolean isKindOf(final Instance _instance,
                                   final CIType _ciType)
    {
        return isValid(_instance) && _instance.getType().isKindOf(_ciType);
    }

    /**
     * Checks if Instance is type.
     *
     * @param _instance the instance
     * @param _ciType the _ci type
     * @return true, if is valid
     */
    public static boolean isType(final Instance _instance,
                                 final CIType _ciType)
    {
        return isValid(_instance) && _instance.getType().isCIType(_ciType);
    }

    /**
     * Checks if is valid.
     *
     * @param _instance the instance
     * @return true, if is valid
     */
    public static boolean isValid(final Instance _instance)
    {
        return _instance != null && _instance.isValid();
    }

    /**
     * Checks if is not valid.
     *
     * @param _instance the instance
     * @return true, if is not valid
     */
    public static boolean isNotValid(final Instance _instance)
    {
        return !isValid(_instance);
    }
}
