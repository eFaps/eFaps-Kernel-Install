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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.HistoryXML.AttrValue;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * @todo description
 */
@EFapsUUID("11b5668b-de34-4cb5-985c-b3f10686e72c")
@EFapsRevision("$Rev$")
public class HistoryTrigger
{

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return insertTrigger(final Parameter _parameter)
        throws EFapsException
    {
        execute(_parameter, CICommon.HistoryCreate.getType(), "create");
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return updateTrigger(final Parameter _parameter)
        throws EFapsException
    {
        execute(_parameter, CICommon.HistoryUpdate.getType(), "update");
        return new Return();
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _type         type to be created
     * @param _event        event name
     * @throws EFapsException on error
     */
    protected void execute(final Parameter _parameter,
                           final Type _type,
                           final String _event)
        throws EFapsException
    {
        final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        // analyze the attributes
        final Set<AttrValue> attrValues = new HashSet<AttrValue>();
        for (final Entry<?, ?> entry : values.entrySet()) {
            final Attribute attr = ((Attribute) entry.getKey());
            if (properties.containsKey("Select4_" + attr.getName())) {
                final String sel = (String) properties.get("Select4_" + attr.getName());
                final AttrValue attrValue = new AttrValue(attr.getName(), DBProperties.getProperty(attr.getLabelKey(),
                                "en"), sel);
                attrValues.add(attrValue);
            } else if (attr.getAttributeType().getDbAttrType() instanceof StatusType) {
                final Long statusID = (Long) ((Object[]) entry.getValue())[0];
                final Status status = Status.get(statusID);
                attrValues.add(new AttrValue(attr.getName(), DBProperties.getProperty(attr.getLabelKey(), "en"),
                                DBProperties.getProperty(status.getLabelKey(), "en"), null));
            } else {
                final Object val = entry.getValue();
                final String value;
                if (val instanceof Object[]) {
                    value = ((Object[]) val)[0] == null ? null : ((Object[]) val)[0].toString();
                } else {
                    value = val == null ? null : val.toString();
                }
                attrValues.add(new AttrValue(attr.getName(), DBProperties.getProperty(attr.getLabelKey(), "en"), value,
                                null));
            }
        }
        final PrintQuery print = new PrintQuery(instance);
        boolean exec = false;
        for (final AttrValue attrValue : attrValues) {
            if (attrValue.getSelect() != null) {
                print.addSelect(attrValue.getSelect());
                print.addSelect(new SelectBuilder().linkto(attrValue.getName()).oid());
                exec = true;
            }
        }
        if (exec && print.executeWithoutAccessCheck()) {
            for (final AttrValue attrValue : attrValues) {
                if (attrValue.getSelect() != null) {
                    final String linkOid = print.<String>getSelect(new SelectBuilder().linkto(attrValue.getName())
                                    .oid());
                    attrValue.setLinkOid(linkOid);
                    final Object val = print.getSelect(attrValue.getSelect());
                    if (val != null) {
                        attrValue.setValue(val.toString());
                    }
                }
            }
        }
        // create the actual xml
        final HistoryXML xml = new HistoryXML(instance, _event);
        xml.addAttributes(attrValues);
        final String xmlStr = xml.getXML();

        if (instance.isValid() && instance.getType().isGeneralInstance()) {
            final Insert insert = new Insert(_type);
            insert.add(CICommon.HistoryAbstract.GeneralInstanceLink, instance.getGeneralId());
            insert.add(CICommon.HistoryAbstract.Value, xmlStr);
            insert.executeWithoutAccessCheck();
        }
    }
}
