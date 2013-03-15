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


package org.efaps.esjp.common;

import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * Class contains some generic methods used by its subclasses.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b374b7e1-2598-4841-b77c-542ed39c721e")
@EFapsRevision("$Rev$")
public abstract class AbstractCommon_Base
{

    /**
     * Search for the given Property and returns a tree map with the found values.<br/>
     * Properties like:<br/>
     * Name<br/>
     * Name01<br/>
     * Name02<br/>
     * Will return a map with:<br/>
     * 0 - Value for Name<br/>
     * 1 - Value for Name01<br/>
     * 2 - Value for Name02
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return map with properties
     * @throws EFapsException on error
     */
    protected Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                   final String _name)
        throws EFapsException
    {
        final Map<Integer, String> ret = new TreeMap<Integer, String>();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        // test for basic
        if (properties.containsKey(_name)) {
            ret.put(0, String.valueOf(properties.get(_name)));
        }
        for (int i = 1; i < 100; i++) {
            final String nameTmp = _name + String.format("%02d", i);
            if (properties.containsKey(nameTmp)) {
                ret.put(i, String.valueOf(properties.get(nameTmp)));
            } else {
                break;
            }
        }
        return ret;
    }

    /**
     * Get a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    protected String getProperty(final Parameter _parameter,
                                 final String _name)
        throws EFapsException
    {
        String ret = null;
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        // test for basic
        if (properties.containsKey(_name)) {
            ret = String.valueOf(properties.get(_name));
        }
        return ret;
    }

}
