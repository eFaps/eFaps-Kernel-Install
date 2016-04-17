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
package org.efaps.esjp.db;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
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
