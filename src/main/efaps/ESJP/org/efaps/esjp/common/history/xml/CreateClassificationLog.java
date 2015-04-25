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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.history.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
@EFapsUUID("23eba577-e7c0-45cc-a9c0-60e1d83d21c9")
@EFapsApplication("eFaps-Kernel")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "createClassification")
public class CreateClassificationLog
    extends AbstractUpdateLog
    implements IClassificationLog
{

    /**
     * The instance Object.
     */
    @XmlElementRef
    private ClassInstObj classInstObj;

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
        ret.append( CICommon.HistoryClassificationCreate.getType().getLabel());
        if (getClassInstObj() != null) {
            final Type type = Type.get(getClassInstObj().getTypeUUID());
            if (type != null) {
                ret.append(": ").append(type.getLabel());
            }
        }
        return ret.toString();
    }

    /**
     * Getter method for the instance variable {@link #classificationInstance}.
     *
     * @return value of instance variable {@link #classificationInstance}
     */
    @Override
    public ClassInstObj getClassInstObj()
    {
        return this.classInstObj;
    }

    /**
     * Setter method for instance variable {@link #classificationInstance}.
     *
     * @param _classificationInstance value for instance variable
     *            {@link #classificationInstance}
     */
    @Override
    public void setClassInstObj(final ClassInstObj _classificationInstance)
    {
        this.classInstObj = _classificationInstance;
    }
}
