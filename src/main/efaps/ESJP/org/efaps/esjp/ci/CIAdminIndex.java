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
 *
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("79b051da-f4c5-4ff5-9420-635ca91c7e49")
// CHECKSTYLE:OFF
public class CIAdminIndex
    extends org.efaps.ci.CIAdminIndex
{

    public static final _IndexSearchAbstract IndexSearchAbstract = new _IndexSearchAbstract("75b5c0ad-9251-4d07-bbf2-08ea13b2071f");

    public static class _IndexSearchAbstract
        extends CIType
    {

        protected _IndexSearchAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Config = new CIAttribute(this, "Config");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _IndexSearch IndexSearch = new _IndexSearch("8b1b6f35-8065-4e99-af2a-2084fc5199e3");

    public static class _IndexSearch
        extends _IndexSearchAbstract
    {

        protected _IndexSearch(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _IndexSearchDetailAbstract IndexSearchDetailAbstract = new _IndexSearchDetailAbstract("cc0aa10b-8694-4016-b046-69092d063e14");

    public static class _IndexSearchDetailAbstract
        extends CIType
    {

        protected _IndexSearchDetailAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Key = new CIAttribute(this, "Key");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _IndexSearchResultField IndexSearchResultField = new _IndexSearchResultField("55d55201-357b-429e-8069-0fcc4e7c108b");

    public static class _IndexSearchResultField
        extends _IndexSearchDetailAbstract
    {

        protected _IndexSearchResultField(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute SearchLink = new CIAttribute(this, "SearchLink");
        public final CIAttribute Label = new CIAttribute(this, "Label");
        public final CIAttribute Priority = new CIAttribute(this, "Priority");
    }

    public static final _IndexAttributeAbstract IndexAttributeAbstract = new _IndexAttributeAbstract("080cb40d-f023-4c8f-a2cf-3b304ea6a6a5");

    public static class _IndexAttributeAbstract
        extends _IndexSearchDetailAbstract
    {

        protected _IndexAttributeAbstract(final String _uuid)
        {
            super(_uuid);
        }
    }


    public static final _IndexSearchResultFieldKey IndexSearchResultFieldKey = new _IndexSearchResultFieldKey("78424fe4-ccc0-4039-a227-96c870743124");

    public static class _IndexSearchResultFieldKey
        extends _IndexAttributeAbstract
    {

        protected _IndexSearchResultFieldKey(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute ResultFieldLink = new CIAttribute(this, "ResultFieldLink");
    }



}
