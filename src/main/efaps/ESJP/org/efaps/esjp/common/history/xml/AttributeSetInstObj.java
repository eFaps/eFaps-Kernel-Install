/*
 * Copyright 2003 - 2015 The eFaps Team
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
 */

package org.efaps.esjp.common.history.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("af6d7cc0-6ccf-40ac-a29c-d7391f0c6183")
@EFapsApplication("eFaps-Kernel")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "attributeSetInstance")
public class AttributeSetInstObj
    extends AbstractInstObj
{
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}