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
package org.efaps.esjp.ci;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;

/**
 * TODO comment!
 *
 * @author The eFaps Team

 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("79b051da-f4c5-4ff5-9420-635ca91c7e49")
//CHECKSTYLE:OFF
public class CIAdmin
{

    public static final _DataModel_StatusAbstract DataModel_StatusAbstract = new _DataModel_StatusAbstract(
                    "f3eaf2f3-b24c-43c0-9ea1-0f6354438c81");

    public static class _DataModel_StatusAbstract
        extends CIType
    {

        protected _DataModel_StatusAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Key = new CIAttribute(this, "Key");
        public final CIAttribute Description = new CIAttribute(this, "Description");
    }
}
