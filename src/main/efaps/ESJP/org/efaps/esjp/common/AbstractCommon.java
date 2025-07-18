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
package org.efaps.esjp.common;

import java.util.Properties;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("734c92f8-aaf6-43f0-a13a-6fb941611477")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractCommon
    extends AbstractCommon_Base
{

    /**
     * Gets the query bldr from properties.
     *
     * @param _properties the _properties
     * @return the query bldr from properties
     * @throws EFapsException on error
     */
    public static QueryBuilder getQueryBldrFromProperties(final Properties _properties)
        throws EFapsException
    {
        return AbstractCommon_Base.getQueryBldrFromProperties(_properties);
    }

    public static boolean isRest()
    {
        return AbstractCommon_Base.isRest();
    }
}
