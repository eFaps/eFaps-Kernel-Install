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


import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

import jakarta.xml.bind.annotation.XmlElementRef;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("bb10c22c-df7d-49be-ae9b-b10efc5cbbf5")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractAttributeSetUpdateLog
    extends AbstractUpdateLog
    implements IAttributeSetLog
{

    /** The attr set ist obj. */
    @XmlElementRef
    private AttributeSetInstObj attrSetIstObj;


    @Override
    public AttributeSetInstObj getAttributSetInstObj()
    {
        return this.attrSetIstObj;
    }

    @Override
    public void setAttributSetInstObj(final AttributeSetInstObj _attributSetInstObj)
    {
        this.attrSetIstObj = _attributSetInstObj;
    }

    @Override
    protected AbstractInstObj getInstObj4Description()
    {
        return getAttributSetInstObj();
    }

    @Override
    public String getTypeColumnValue()
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        if (getAttributSetInstObj() != null) {
            final AttributeSet attrSet = (AttributeSet) Type.get(getAttributSetInstObj().getTypeUUID());
            if (attrSet != null) {
                final Type parentType = attrSet.getAttribute(attrSet.getAttributeName()).getLink();
                final String attrLabel = DBProperties.getProperty(
                                parentType.getName() + "/" + attrSet.getAttributeName() + ".Label");
                ret.append(": ").append(attrLabel);
                if (parentType instanceof Classification) {
                    ret.append(" in Class: ").append(parentType.getLabel());
                }
            }
        }
        return ret.toString();
    }
}
