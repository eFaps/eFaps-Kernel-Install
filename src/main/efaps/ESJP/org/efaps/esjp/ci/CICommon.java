/*
 * Copyright 2003 - 2016 The eFaps Team
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
import org.efaps.ci.CIStatus;
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


    public static final _BackgroundJobAbstract BackgroundJobAbstract = new _BackgroundJobAbstract("96a038fc-8d28-4a0d-b841-b6c6496d4bc7");

    public static class _BackgroundJobAbstract
        extends CIType
    {

        protected _BackgroundJobAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Company = new CIAttribute(this, "Company");
        public final CIAttribute StatusAbstract = new CIAttribute(this, "StatusAbstract");
        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Title = new CIAttribute(this, "Title");
        public final CIAttribute Progress = new CIAttribute(this, "Progress");
        public final CIAttribute Target = new CIAttribute(this, "Target");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _BackgroundJobStatus BackgroundJobStatus = new _BackgroundJobStatus("f43f45f4-cff2-4d7c-909c-ece88f26c412");
    public static class _BackgroundJobStatus extends org.efaps.esjp.ci.CIAdmin._DataModel_StatusAbstract
    {
        protected _BackgroundJobStatus(final String _uuid)
        {
            super(_uuid);
        }
        public final CIStatus Scheduled = new CIStatus(this, "Scheduled");
        public final CIStatus Ready = new CIStatus(this, "Ready");
        public final CIStatus Active = new CIStatus(this, "Active");
        public final CIStatus Finished = new CIStatus(this, "Finished");
        public final CIStatus Canceled = new CIStatus(this, "Canceled");
    }


    public static final _DashboardAbstract DashboardAbstract = new _DashboardAbstract("b2388ab1-f9c6-47f1-9893-bc2d72a71d19");

    public static class _DashboardAbstract
        extends CIType
    {

        protected _DashboardAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Title = new CIAttribute(this, "Title");
        public final CIAttribute Description = new CIAttribute(this, "Description");
        public final CIAttribute Weight = new CIAttribute(this, "Weight");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _DashboardDefault DashboardDefault = new _DashboardDefault("6bd550d1-4eac-4354-a984-431204f2bdb7");

    public static class _DashboardDefault
        extends _DashboardAbstract
    {

        protected _DashboardDefault(final String _uuid)
        {
            super(_uuid);
        }
    }


    public static final _DashboardAbstract2Object DashboardAbstract2Object = new _DashboardAbstract2Object("18dc5f79-e1aa-4e12-a8c1-6159b5f57fa6");

    public static class _DashboardAbstract2Object
        extends CIType
    {

        protected _DashboardAbstract2Object(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FromLinkAbstract = new CIAttribute(this, "FromLinkAbstract");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _DashboardDefault2Element DashboardDefault2Element = new _DashboardDefault2Element("f356aad6-093f-4313-a1ef-1c77f7e462dd");

    public static class _DashboardDefault2Element
        extends _DashboardAbstract2Object
    {

        protected _DashboardDefault2Element(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FromLink = new CIAttribute(this, "FromLink");
        public final CIAttribute ToLink = new CIAttribute(this, "ToLink");
        public final CIAttribute Field = new CIAttribute(this, "Field");
    }

    public static final _DashboardElement DashboardElement = new _DashboardElement("01dd4e3a-09f3-47cf-87c0-e3f3ab821732");

    public static class _DashboardElement
        extends CIType
    {

        protected _DashboardElement(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Description = new CIAttribute(this, "Description");
        public final CIAttribute Config = new CIAttribute(this, "Config");
        public final CIAttribute EsjpLink = new CIAttribute(this, "EsjpLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }


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

    public static final _HistoryAttributeSetCreate HistoryAttributeSetCreate = new _HistoryAttributeSetCreate("dc4e9fb1-34ac-490b-afe5-b46a52663a9d");

    public static class _HistoryAttributeSetCreate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryAttributeSetCreate(final String _uuid)
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

    public static final _HistoryAttributeSetUpdate HistoryAttributeSetUpdate = new _HistoryAttributeSetUpdate("8f4a5896-9504-4d4c-8ddc-c05f4871f095");

    public static class _HistoryAttributeSetUpdate
        extends _HistoryAbstractUpdate
    {

        protected _HistoryAttributeSetUpdate(final String _uuid)
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

    public static final _HistoryAttributeSetDelete HistoryAttributeSetDelete = new _HistoryAttributeSetDelete("aa856abd-00f4-4331-bd3d-26a92b70401e");

    public static class _HistoryAttributeSetDelete
        extends _HistoryAbstractUpdate
    {

        protected _HistoryAttributeSetDelete(final String _uuid)
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


    public static final _DBPropertiesBundle DBPropertiesBundle = new _DBPropertiesBundle("03c51ba4-00a6-47ac-b987-270308fe68d8");

    public static class _DBPropertiesBundle
        extends CIType
    {

        protected _DBPropertiesBundle(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute UUID = new CIAttribute(this, "UUID");
        public final CIAttribute Sequence = new CIAttribute(this, "Sequence");
        public final CIAttribute CacheOnStart = new CIAttribute(this, "CacheOnStart");
        public final CIAttribute RevisionLink = new CIAttribute(this, "RevisionLink");
    }
}
