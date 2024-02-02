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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f1174f25-8e7f-443d-96a7-73355ea7cf50")
@EFapsApplication("eFaps-Kernel")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "updateClassification")
public class UpdateClassificationLog
    extends AbstractClassificationUpdateLog
    implements IClassificationLog
{

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeColumnValue()
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append( CICommon.HistoryClassificationUpdate.getType().getLabel());
        if (getClassInstObj() != null) {
            final Type type = Type.get(getClassInstObj().getTypeUUID());
            if (type != null) {
                ret.append(": ").append(type.getLabel());
            }
        }
        return ret.toString();
    }

    @Override
    protected AbstractInstObj getInstObj4Description()
    {
        return getClassInstObj();
    }
}
