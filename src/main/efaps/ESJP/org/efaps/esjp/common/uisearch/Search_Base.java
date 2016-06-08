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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.common.uisearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.StringType;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id:QuerySearch.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("a036b0e7-20ce-47c7-83a0-f7644af80fd1")
@EFapsApplication("eFaps-Kernel")
public abstract class Search_Base
    extends AbstractCommon
    implements EventExecution
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @throws EFapsException on error
     * @return List of instances
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, getInstances(_parameter));
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API.
     * @return list of instances
     * @throws EFapsException  on error
     */
    protected List<Instance> getInstances(final Parameter _parameter)
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<Instance>();
        final QueryBuilder queryBldr = getQueryBldr(_parameter);
        add2QueryBuilder(_parameter, queryBldr);
        ret.addAll(queryBldr.getQuery().execute());
        return ret;
    }

    /**
     * Method for obtains a List of the query.
     *
     * @param _parameter Parameter as passed by the eFaps API.
     * @param _queryBldr QueryBuilder to add to
     * @throws EFapsException on error.
     */
    protected void add2QueryBuilder(final Parameter _parameter,
                                    final QueryBuilder _queryBldr)
        throws EFapsException
    {
        // to be used by implenting classes
    }

    /**
     * Method for obtains a QueryBuilder.
     *
     * @param _parameter Parameter as passed from the eFaps API.
     * @return queryBldr QueryBuilder with values for search.
     * @throws EFapsException on error.
     */
    protected QueryBuilder getQueryBldr(final Parameter _parameter)
        throws EFapsException
    {
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final QueryBuilder ret = getQueryBldrFromProperties(_parameter);

        final Collection<String> ignFields = analyseProperty(_parameter, "IgnoreCase4Field").values();
        for (final Field field : command.getTargetForm().getFields()) {
            final String value = Context.getThreadContext().getParameter(field.getName());
            if (value != null && value.length() > 0 && !value.equals("*")) {
                if (ret.getType().getAttribute(field.getAttribute()) != null) {
                    final Attribute attribute = ret.getType().getAttribute(field.getAttribute());
                    if (attribute.getAttributeType().getDbAttrType() instanceof StringType) {
                        ret.addWhereAttrMatchValue(field.getAttribute(), value + "*")
                                        .setIgnoreCase(ignFields.contains(field.getName()));
                    } else {
                        ret.addWhereAttrEqValue(field.getAttribute(), value);
                    }
                } else {
                    if (field.getAttribute() != null) {
                        ret.addWhereAttrMatchValue(field.getAttribute(), value)
                                        .setIgnoreCase(ignFields.contains(field.getName()));
                    } else if (field.getSelect() != null) {
                        if (field.getUIProvider() instanceof StringUI) {
                            ret.addWhereSelectMatchValue(field.getSelect(), value + "*")
                                .setIgnoreCase(ignFields.contains(field.getName()));
                        } else if (field.getUIProvider() instanceof NumberUI) {
                            ret.addWhereAttrEqValue(field.getSelect(), value);
                        } else {
                            ret.addWhereAttrMatchValue(field.getSelect(), value)
                                .setIgnoreCase(ignFields.contains(field.getName()));
                        }
                    }
                }
            }
        }
        return ret;
    }



    /**
     * Render a dropdown with the types.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @return Return containing HTML snipplet
     * @throws EFapsException on error
     */
    public Return typeFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        String typeStr = getProperty(_parameter, "Types");
        if (typeStr != null) {
            Search_Base.LOG.warn("Field '{}' uses deprecated API", fieldValue.getField().getName());
        } else {
            typeStr = getProperty(_parameter, "Type");
        }
        final Type type = Type.get(typeStr);

        final Map<String, Long> values = new TreeMap<String, Long>();
        final Set<Type> types = getTypeList(_parameter, type);
        for (final Type atype : types) {
            if (!atype.isAbstract()) {
                values.put(atype.getLabel(), atype.getId());
            }
        }

        final StringBuilder html = new StringBuilder();
        html.append("<select size=\"1\" name=\"").append(fieldValue.getField().getName()).append("\">");
        html.append("<option value=\"*\">*</option>");
        for (final Entry<String, Long> value : values.entrySet()) {
            html.append("<option value=\"").append(value.getValue()).append("\">").append(value.getKey())
                            .append("</option>");
        }
        html.append("</select>");
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }
}
