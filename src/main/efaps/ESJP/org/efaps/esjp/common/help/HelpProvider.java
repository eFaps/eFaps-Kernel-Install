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
package org.efaps.esjp.common.help;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsListener;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("9794280c-0ca5-4882-ad62-c9d6591e145d")
@EFapsApplication("eFaps-Kernel")
@EFapsListener
public class HelpProvider
    extends HelpProvider_Base
{

    /** The cachekey. */
    public static final String CACHEKEY = HelpProvider_Base.CACHEKEY;

    /** The togglekey. */
    public static final String TOGGLEKEY = HelpProvider_Base.TOGGLEKEY;


    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

}
