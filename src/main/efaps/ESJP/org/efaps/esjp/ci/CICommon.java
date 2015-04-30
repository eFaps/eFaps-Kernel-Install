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

    public static final _HistoryAbstractUpdate HistoryAbstractUpdate = new _HistoryAbstractUpdate("c558afb9-4762-470f-843c-91a528cc103b");

    public static class _HistoryAbstractUpdate
        extends _HistoryAbstract
    {
        protected _HistoryAbstractUpdate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryCreate HistoryCreate = new _HistoryCreate("510ced6e-101a-434c-b891-069861e64fc2");

    public static class _HistoryCreate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryCreate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryRelatedCreate HistoryRelatedCreate = new _HistoryRelatedCreate("1f01a7a0-eba2-464f-a455-8ddd225e46e5");

    public static class _HistoryRelatedCreate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryRelatedCreate(final String _uuid)
        {
            super(_uuid);
        }
    }


    public static final _HistoryClassificationCreate HistoryClassificationCreate = new _HistoryClassificationCreate("18a26ed6-3614-4382-9751-d95a9f5cbab9");

    public static class _HistoryClassificationCreate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryClassificationCreate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryClassificationUpdate HistoryClassificationUpdate = new _HistoryClassificationUpdate("88c2bba1-eb63-458d-a23c-5532513ffdaa");

    public static class _HistoryClassificationUpdate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryClassificationUpdate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryClassificationDelete HistoryClassificationDelete = new _HistoryClassificationDelete("46e8a07d-44fb-450d-b7ef-524b8c628675");

    public static class _HistoryClassificationDelete
        extends _HistoryAbstractUpdate
    {

        protected _HistoryClassificationDelete(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryUpdate HistoryUpdate = new _HistoryUpdate("fdc408a6-c94c-425c-86fa-6d632281010d");

    public static class _HistoryUpdate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryUpdate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryRelatedUpdate HistoryRelatedUpdate = new _HistoryRelatedUpdate("8e4d8ac5-0e97-4276-88a9-3105c9416575");

    public static class _HistoryRelatedUpdate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryRelatedUpdate(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryDelete HistoryDelete = new _HistoryDelete("4c20af22-b272-45b3-9a19-bd03989b12f0");

    public static class _HistoryDelete
        extends _HistoryAbstractUpdate
    {

        protected _HistoryDelete(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryRelatedDelete HistoryRelatedDelete = new _HistoryRelatedDelete("da64b650-5ba8-4c87-b0c7-2802e4b27d40");

    public static class _HistoryRelatedDelete
        extends _HistoryAbstractUpdate
    {

        protected _HistoryRelatedDelete(final String _uuid)
        {
            super(_uuid);
        }
    }


    public static final _HistoryConnect HistoryConnect = new _HistoryConnect("14514bc0-b251-4df3-9955-473115e6b682");

    public static class _HistoryConnect
        extends _HistoryAbstractUpdate
    {

        protected _HistoryConnect(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryDisconnect HistoryDisconnect = new _HistoryDisconnect("5631c300-5814-4ce8-8cee-6119b9ff6ab3");

    public static class _HistoryDisconnect
        extends _HistoryAbstractUpdate
    {

        protected _HistoryDisconnect(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryAbstractLoginOut HistoryAbstractLoginOut = new _HistoryAbstractLoginOut("c0265955-04b9-4eb5-bbd4-1f3e4a4ba232");

    public static class _HistoryAbstractLoginOut
        extends _HistoryAbstract
    {
        protected _HistoryAbstractLoginOut(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryLogin HistoryLogin = new _HistoryLogin("ec564e40-81aa-42f7-a7c0-8f4bcc54dd2a");

    public static class _HistoryLogin
        extends _HistoryAbstractLoginOut
    {

        protected _HistoryLogin(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryLogout HistoryLogout = new _HistoryLogout("ee4bf603-a15e-4f7d-904f-585e0593b069");

    public static class _HistoryLogout
        extends _HistoryAbstractLoginOut
    {
        protected _HistoryLogout(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _HistoryEQL HistoryEQL = new _HistoryEQL("c96c63b5-2d4c-4bf9-9627-f335fd9c7a84");

    public static class _HistoryEQL
        extends CIType
    {

        protected _HistoryEQL(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Origin = new CIAttribute(this, "Origin");
        public final CIAttribute EQLStatement = new CIAttribute(this, "EQLStatement");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
    }
}
