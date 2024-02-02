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
package org.efaps.esjp.common.history.xml;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import jakarta.xml.bind.annotation.XmlElementRef;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e38c61a9-1340-47b7-b402-78683b5abebc")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractRelatedUpdateLog
    extends AbstractUpdateLog
    implements IRelatedLog
{
    @XmlElementRef
    private RelatedInstObj relatedInstObj;

    @Override
    public RelatedInstObj getRelatedInstObj()
    {
        return this.relatedInstObj;
    }

    @Override
    public void setRelatedInstObj(final RelatedInstObj _relatedInstObj)
    {
        this.relatedInstObj = _relatedInstObj;
    }

    @Override
    protected AbstractInstObj getInstObj4Description()
    {
        return getRelatedInstObj();
    }
}
