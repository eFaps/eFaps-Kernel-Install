/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.esjp.ci;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class CICommon
{

    public static final _HistoryAbstract HistoryAbstract = new _HistoryAbstract("66a75f87-1e07-410d-a0c4-c6ec1671b9a1");

    public static class _HistoryAbstract
        extends CIType
    {

        protected _HistoryAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute GeneralInstanceLink = new CIAttribute(this, "GeneralInstanceLink");
        public final CIAttribute Value = new CIAttribute(this, "Value");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
    }

    public static final _HistoryCreate HistoryCreate = new _HistoryCreate("510ced6e-101a-434c-b891-069861e64fc2");

    public static class _HistoryCreate
        extends _HistoryAbstract
    {

        protected _HistoryCreate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryUpdate HistoryUpdate = new _HistoryUpdate("fdc408a6-c94c-425c-86fa-6d632281010d");

    public static class _HistoryUpdate
        extends _HistoryAbstract
    {

        protected _HistoryUpdate(final String _uuid)
        {
            super(_uuid);
        }
    }
}
