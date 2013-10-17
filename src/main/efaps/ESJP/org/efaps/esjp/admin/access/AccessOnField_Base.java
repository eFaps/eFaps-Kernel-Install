/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.admin.access;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * Class is used as an esjp to give access on a field. It contains methods that
 * allows to hide fields when necessary.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ed8d307f-faed-4e4b-9251-1280658d2945")
@EFapsRevision("$Rev$")
@Deprecated
public abstract class AccessOnField_Base
    extends AccessCheck4UI
{
}
