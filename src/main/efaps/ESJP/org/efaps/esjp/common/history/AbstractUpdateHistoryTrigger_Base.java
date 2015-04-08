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


package org.efaps.esjp.common.history;

import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.PasswordType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AttributeValue;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4cb893f9-141c-40dc-beab-c147a4b2096f")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractUpdateHistoryTrigger_Base
    extends AbstractHistoryTrigger
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void add2LogObject(final Parameter _parameter,
                                 final AbstractHistoryLog _log)
        throws EFapsException
    {
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        analyseProperty(_parameter, "");
        for (final Entry<?, ?> entry : values.entrySet()) {
            final Attribute attr = (Attribute) entry.getKey();
            if (!attr.getAttributeType().isAlwaysUpdate() && !attr.getAttributeType().isCreateUpdate()) {
                final AttributeValue attrValue = new AttributeValue();
                attrValue.setName(attr.getName());
                if (attr.getAttributeType().getDbAttrType() instanceof PasswordType) {
                    attrValue.setValue("****************");
                } else {
                    final String phrase = getProperty(_parameter, "Phrase4Attribute_" + attr.getName());
                    String phraseVal = null;
                    if (phrase != null) {
                        final PrintQuery print = new PrintQuery(getHistoryInstance(_parameter));
                        print.addPhrase("SelectPhrase", phrase);
                        print.executeWithoutAccessCheck();
                        phraseVal  = print.getPhrase("SelectPhrase");
                    }
                    if (phraseVal != null) {
                        attrValue.setValue(phraseVal);
                    } else {
                        final Object obj = entry.getValue();
                        if (obj instanceof Object[]) {
                            attrValue.setValue(((Object[]) obj)[0]);
                        }
                    }
                }
                _log.getInstance().getAttributes().add(attrValue);
            }
        }
    }
}
