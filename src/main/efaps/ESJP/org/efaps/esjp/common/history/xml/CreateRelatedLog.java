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


package org.efaps.esjp.common.history.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("aecad2e9-b378-447a-a56d-4b29c3622af9")
@EFapsApplication("eFaps-Kernel")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "createRelated")
public class CreateRelatedLog
    extends AbstractRelatedUpdateLog
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
        ret.append( CICommon.HistoryRelatedCreate.getType().getLabel());
        if (getRelatedInstObj() != null) {
            final Type type = Type.get(getRelatedInstObj().getTypeUUID());
            if (type != null) {
                ret.append(": ").append(type.getLabel());
            }
        }
        return ret.toString();
    }
}
