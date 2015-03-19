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


package org.efaps.esjp.admin.datamodel;

import java.io.StringReader;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.PrintQuery;
import org.efaps.util.EFapsException;


/**
 * A Range Value esjp that is used to get the map for Person.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("a4eb532a-6503-4c20-90d5-7279f2633060")
@EFapsRevision("$Rev$")
public abstract class PersonRangeValue_Base
    extends RangesValue
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSelectedValue(final Parameter _parameter,
                                    final Map<?,?> _valueMap)
        throws EFapsException
    {
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (_parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
            final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final String valueDef = (String) properties.get("Value");
            String value;
            if (valueDef.contains("$<")) {
                ValueList list = null;
                final ValueParser parser = new ValueParser(new StringReader(valueDef));
                try {
                    list = parser.ExpressionString();
                } catch (final ParseException e) {
                    throw new EFapsException(PersonRangeValue_Base.class.toString(), e);
                }
                final PrintQuery print = new PrintQuery(CIAdminUser.Person.getType(),
                                Context.getThreadContext().getPersonId());
                list.makeSelect(print);
                print.execute();
                value = list.makeString(null, print, TargetMode.CREATE);
            } else {
                value = Context.getThreadContext().getPerson().getName();
            }
            fieldValue.setValue(value);
        }
    }
}
