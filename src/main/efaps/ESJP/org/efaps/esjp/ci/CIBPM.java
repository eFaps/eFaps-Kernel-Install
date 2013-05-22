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


package org.efaps.esjp.ci;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CIBPM
{
    public static final _Object2ObjectAbstract Object2ObjectAbstract = new _Object2ObjectAbstract("5aafd426-c621-4db4-b217-1a3be6096faa");

    public static class _Object2ObjectAbstract
        extends CIType
    {

        protected _Object2ObjectAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FromObjectAbstract = new CIAttribute(this, "FromObjectAbstract");
        public final CIAttribute ToObjectAbstract = new CIAttribute(this, "ToObjectAbstract");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
    }


    public static final _GeneralInstance2ProcessId GeneralInstance2ProcessId = new _GeneralInstance2ProcessId("f6731331-e3a7-4a98-be35-ad1bb8e88497");

    public static class _GeneralInstance2ProcessId
        extends _Object2ObjectAbstract
    {
        protected _GeneralInstance2ProcessId(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute ProcessId = new CIAttribute(this, "ProcessId");
        public final CIAttribute GeneralInstanceLink = new CIAttribute(this, "GeneralInstanceLink");

    }
}
