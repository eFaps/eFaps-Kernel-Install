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
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAttribute;

/**
 * The Class CIAdminProgram.
 *
 * @author The eFaps Team
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("27c71204-71ed-4e4f-bd85-e5a587b6c0b5")
// CHECKSTYLE:OFF
public class CIAdminProgram
    extends org.efaps.ci.CIAdminProgram
{
    public static final _Markdown Markdown = new _Markdown("afef5876-17e9-473c-80d6-5b602d0afab5");

    public static class _Markdown
        extends _Abstract
    {
        protected _Markdown(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Markdown2Command Markdown2Command = new _Markdown2Command("ecf1350d-4cf9-45db-bc60-2d5515ebc852");

    public static class _Markdown2Command
        extends CIAdminCommon._Abstract2Abstract
    {
        protected _Markdown2Command(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute FromLink = new CIAttribute(this, "FromLink");
        public final CIAttribute ToLink = new CIAttribute(this, "ToLink");
    }

}
