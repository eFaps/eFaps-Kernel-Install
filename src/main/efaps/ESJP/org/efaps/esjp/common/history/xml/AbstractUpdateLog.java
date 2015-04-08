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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.history.IHistoryHtml;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f23d81b3-8083-450b-876d-78e207890fe6")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractUpdateLog
    extends AbstractHistoryLog
    implements IHistoryHtml
{
    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder getDescriptionColumnValue()
        throws EFapsException
    {
        final AbstractInstObj inst = getInstance();
        final Type type = Type.get(inst.getTypeUUID());
        final StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (final AttributeValue attrVal : inst.getAttributes()) {
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
