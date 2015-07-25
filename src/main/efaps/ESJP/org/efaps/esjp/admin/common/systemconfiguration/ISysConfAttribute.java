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
package org.efaps.esjp.admin.common.systemconfiguration;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Interface ISysConfAttribute.
 *
 * @author The eFaps Team
 */
@EFapsUUID("b50e11ea-59a9-432a-b64a-43d6e8616692")
@EFapsApplication("eFaps-Kernel")
public interface ISysConfAttribute
{

    /**
     * Gets the key.
     *
     * @return the key
     */
    String getKey();

    /**
     * Gets the JS node.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _value the _value
     * @return the JS node
     * @throws EFapsException the efaps exception
     */
    CharSequence getHtml(final Parameter _parameter,
                        final Object _value)
        throws EFapsException;
}
