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

import javax.xml.bind.annotation.XmlElementRef;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.history.IHistoryHtml;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d19eb79a-2420-4414-89dd-e0b5881e6dd0")
@EFapsRevision("$Rev$")
public abstract class AbstractConnectLog
    extends AbstractHistoryLog
    implements IHistoryHtml
{

    /**
     * The instance Object.
     */
    @XmlElementRef
    private ConnectInstObj connectInstance;

    /**
     * The instance Object.
     */
    @XmlElementRef
    private HistoryInstObj historyInstance;

    /**
     * Getter method for the instance variable {@link #connectInstance}.
     *
     * @return value of instance variable {@link #connectInstance}
     */
    public ConnectInstObj getConnectInstance()
    {
        return this.connectInstance;
    }

    /**
     * Setter method for instance variable {@link #connectInstance}.
     *
     * @param _connectInstance value for instance variable
     *            {@link #connectInstance}
     */
    public void setConnectInstance(final ConnectInstObj _connectInstance)
    {
        this.connectInstance = _connectInstance;
    }

    /**
     * Getter method for the instance variable {@link #historyInstance}.
     *
     * @return value of instance variable {@link #historyInstance}
     */
    public HistoryInstObj getHistoryInstance()
    {
        return this.historyInstance;
    }

    /**
     * Setter method for instance variable {@link #historyInstance}.
     *
     * @param _historyInstance value for instance variable
     *            {@link #historyInstance}
     */
    public void setHistoryInstance(final HistoryInstObj _historyInstance)
    {
        this.historyInstance = _historyInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder getDescriptionColumnValue()
        throws EFapsException
    {
        final AbstractInstObj conInst = getConnectInstance();
        final Type type = Type.get(conInst.getTypeUUID());
        final StringBuilder ret = new StringBuilder();
        ret.append(type.getLabel()).append("<br/>");
        boolean first = true;
        for (final AttributeValue attrVal : conInst.getAttributes()) {
            final Attribute attr = type.getAttribute(attrVal.getName());
            if (attr != null) {
                if (first) {
                    first = false;
                } else {
                    ret.append("<br/>");
                }
                ret.append(DBProperties.getProperty(attr.getLabelKey())).append(": ").append(attrVal.getValue());
            }
        }
        return ret;
    }
}