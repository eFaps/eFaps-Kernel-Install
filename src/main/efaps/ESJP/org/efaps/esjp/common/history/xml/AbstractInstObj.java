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


package org.efaps.esjp.common.history.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1bff1f0b-888b-4d92-86b9-28fb81eeb150")
@EFapsRevision("$Rev$")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractInstObj
{
    /**
     * OID of the Object.
     */
    @XmlAttribute(name = "oid")
    private String oid;

    /**
     * UUID of the Type .
     */
    @XmlAttribute(name = "typeUUID")
    private UUID typeUUID;

    /**
     * Attributes belonging to the Object that executed the event to create this
     * instance of history.
     */
    @XmlElement(name = "attribute")
    @XmlElementWrapper(name = "attributes")
    private List<AttributeValue> attributes;

    /**
     * Getter method for the instance variable {@link #attributes}.
     *
     * @return value of instance variable {@link #attributes}
     */
    public List<AttributeValue> getAttributes()
    {
        if (this.attributes == null) {
            this.attributes = new ArrayList<AttributeValue>();
        }
        return this.attributes;
    }

    /**
     * Setter method for instance variable {@link #attributes}.
     *
     * @param _attributes value for instance variable {@link #attributes}
     */
    public void setAttributes(final List<AttributeValue> _attributes)
    {
        this.attributes = _attributes;
    }


    /**
     * Getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid()
    {
        return this.oid;
    }


    /**
     * Setter method for instance variable {@link #oid}.
     *
     * @param _oid value for instance variable {@link #oid}
     */
    public void setOid(final String _oid)
    {
        this.oid = _oid;
    }


    /**
     * Getter method for the instance variable {@link #typeUUID}.
     *
     * @return value of instance variable {@link #typeUUID}
     */
    public UUID getTypeUUID()
    {
        return this.typeUUID;
    }


    /**
     * Setter method for instance variable {@link #typeUUID}.
     *
     * @param _typeUUID value for instance variable {@link #typeUUID}
     */
    public void setTypeUUID(final UUID _typeUUID)
    {
        this.typeUUID = _typeUUID;
    }
}
