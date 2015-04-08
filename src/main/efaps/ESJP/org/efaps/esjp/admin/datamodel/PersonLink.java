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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("cd9958cf-f8f4-4013-955e-f426e6139c65")
@EFapsApplication("eFaps-Kernel")
public class PersonLink
{

    public Return personValuePickerUI(final Parameter _parameter)
        throws EFapsException
    {
        final String input = (String) _parameter.get(ParameterValues.OTHERS);
        final TreeMap<String, String[]> map = new TreeMap<String, String[]>();

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person);
        queryBldr.addWhereAttrMatchValue(CIAdminUser.Person.Name, input + "*");
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminUser.Person.ID, CIAdminUser.Person.Name,
                           CIAdminUser.Person.FirstName, CIAdminUser.Person.LastName);
        multi.execute();
        while (multi.next()) {
            final String id = multi.<Long>getAttribute(CIAdminUser.Person.ID).toString();
            final String name =  multi.<String>getAttribute(CIAdminUser.Person.Name);
            final String first = multi.<String>getAttribute(CIAdminUser.Person.FirstName);
            final String last = multi.<String>getAttribute(CIAdminUser.Person.LastName);
            map.put(name, new String[] { id, name, last, first });
        }
        final List<String[]> ret = new ArrayList<String[]>();
        for (final Entry<String, String[]> entry : map.entrySet()) {
            ret.add(entry.getValue());
        }
        final Return retVal = new Return();
        retVal.put(ReturnValues.VALUES, ret);
        return retVal;
    }
}
