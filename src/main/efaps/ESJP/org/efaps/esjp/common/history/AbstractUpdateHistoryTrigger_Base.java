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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.admin.datamodel.attributetype.EnumType;
import org.efaps.admin.datamodel.attributetype.PasswordType;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AttributeValue;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

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
        _log.getInstance().getAttributes().addAll(getAttributes(_parameter, getHistoryInstance(_parameter)));
    }

    protected List<AttributeValue> getAttributes(final Parameter _parameter,
                                                 final Instance _instance)
        throws EFapsException
    {
        final List<AttributeValue> ret = new ArrayList<>();
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        final BidiMap<Integer, String> selectAttributes = new DualHashBidiMap<>(analyseProperty(_parameter,
                        "SelectAttribute"));
        final Map<Integer, String> selects = analyseProperty(_parameter, "Select");
        final BidiMap<Integer, String> phraseAttributes = new DualHashBidiMap<>(analyseProperty(_parameter,
                        "PhraseAttribute"));
        final Map<Integer, String> phrases = analyseProperty(_parameter, "Phrase");
        final Collection<String> ignore = analyseProperty(_parameter, "IgnoreAttribute").values();

        for (final Entry<?, ?> entry : values.entrySet()) {
            final Attribute attr = (Attribute) entry.getKey();
            if (!attr.getAttributeType().isAlwaysUpdate() && !attr.getAttributeType().isCreateUpdate()
                            && !ignore.contains(attr.getName())) {
                final AttributeValue attrValue = new AttributeValue();
                attrValue.setName(attr.getName());
                if (attr.getAttributeType().getDbAttrType() instanceof PasswordType) {
                    attrValue.setValue("****************");
                } else if (attr.getAttributeType().getDbAttrType() instanceof StatusType) {
                    final Object objArr = entry.getValue();
                    if (objArr instanceof Object[]) {
                        final Object obj = ((Object[]) objArr)[0];
                        final Long id;
                        if (obj instanceof String) {
                            id = Long.valueOf((String) obj);
                        } else {
                            id = (Long) obj;
                        }
                        attrValue.setValue(Status.get(id).getKey());
                    }
                } else if (attr.getAttributeType().getDbAttrType() instanceof BitEnumType) {
                    final Object objArr = entry.getValue();
                    if (objArr instanceof Object[]) {
                        final Object val = attr.getAttributeType().getDbAttrType()
                                        .readValue(attr, Arrays.asList((Object[]) objArr));
                        if (val == null) {
                            attrValue.setValue(val);
                        } else {
                            final StringBuilder strBldr = new StringBuilder();
                            boolean first = true;
                            for (final Object obj : (List<?>) val) {
                                if (first) {
                                    first = false;
                                } else {
                                    strBldr.append(", ");
                                }
                                if (obj instanceof List) {
                                    strBldr.append(((List<?>) obj).get(0));
                                } else {
                                    strBldr.append(obj);
                                }
                            }
                            attrValue.setValue(strBldr.toString());
                        }
                    }
                } else if (attr.getAttributeType().getDbAttrType() instanceof EnumType) {
                    final Object objArr = entry.getValue();
                    if (objArr instanceof Object[]) {
                        final Object val = attr.getAttributeType().getDbAttrType()
                                        .readValue(attr, Arrays.asList((Object[]) objArr));
                        if (val == null) {
                            attrValue.setValue(val);
                        } else {
                            attrValue.setValue(val.toString());
                        }
                    }
                } else {
                    // check is a select exists
                    if (selectAttributes.containsValue(attr.getName())) {
                        final String select = selects.get(selectAttributes.getKey(attr.getName()));
                        final PrintQuery print = new PrintQuery(_instance);
                        print.addSelect(select);
                        print.executeWithoutAccessCheck();
                        attrValue.setValue(print.getSelect(select));
                    } else if (phraseAttributes.containsValue(attr.getName())) {
                        final String phrase = phrases.get(phraseAttributes.getKey(attr.getName()));
                        final PrintQuery print = new PrintQuery(_instance);
                        print.addPhrase("SelectPhrase", phrase);
                        print.executeWithoutAccessCheck();
                        attrValue.setValue(print.getPhrase("SelectPhrase"));
                    } else {
                        final Object obj = entry.getValue();
                        if (obj instanceof Object[]) {
                            final Object tmpObj = ((Object[]) obj)[0];
                            if (tmpObj instanceof DateTime) {
                                attrValue.setValue(((DateTime) tmpObj).toString());
                            } else {
                                attrValue.setValue(tmpObj);
                            }
                        }
                    }
                }
                ret.add(attrValue);
            }
        }
        return ret;
    }
}
