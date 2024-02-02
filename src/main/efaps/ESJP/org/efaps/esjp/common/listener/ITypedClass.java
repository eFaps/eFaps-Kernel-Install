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
package org.efaps.esjp.common.listener;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIType;
import org.efaps.util.EFapsException;


/**
 * Class is used to have a simple identifier for the class calling.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("9b47df4d-5504-4af5-ac83-b32b01a40b29")
@EFapsApplication("eFaps-Kernel")
public interface ITypedClass
{
    /**
     * Method is used to have a simple identifier for the class calling.
     *
     * @return CIType the class belongs to
     * @throws EFapsException on error
     */
    CIType getCIType() throws EFapsException;
}
