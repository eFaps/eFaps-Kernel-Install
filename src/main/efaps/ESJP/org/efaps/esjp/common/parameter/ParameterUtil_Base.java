/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.esjp.common.parameter;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("628e963d-33a3-4a0f-ac22-ed71ca76d48a")
@EFapsRevision("$Rev$")
public abstract class ParameterUtil_Base
{

    /**
     * Clone the Parameter by making a new Parameter. This does not clone
     * all the values. It makes a clone for the map for PARAMETERS, PROPERTIES
     * @param _parameter Paramter as passed by the eFaps API
     * @param _tuplets tuplets of ParameterValues, Value
     * @return new Parameter instance with a copy
     */
    protected static Parameter clone(final Parameter _parameter,
                                     final Object... _tuplets)
    {
        final Parameter ret = new Parameter();
        for (final Parameter.ParameterValues parVal : Parameter.ParameterValues.values()) {
            final Object val = _parameter.get(parVal);
            if (val != null) {
                if (parVal.equals(ParameterValues.PROPERTIES)
                                || parVal.equals(ParameterValues.PARAMETERS)) {
                    ret.put(parVal, new HashMap<Object, Object>((Map<?, ?>) val));
                } else {
                    ret.put(parVal, val);
                }
            }
        }
        // check if some are set, the tuples must be an even number
        if (_tuplets != null && (_tuplets.length & 1) == 0) {
            int i = 0;
            while (i < _tuplets.length) {
                ret.put((ParameterValues) _tuplets[i], _tuplets[i + 1]);
                i = i + 2;
            }
        }
        return ret;
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _key  Key of the property
     * @param _value    value of the property
     */
    protected static void setProperty(final Parameter _parameter,
                                      final String _key,
                                      final String _value)
    {
        @SuppressWarnings("unchecked")
        Map<Object, Object> properties = (Map<Object, Object>) _parameter.get(ParameterValues.PROPERTIES);
        if (properties == null) {
            properties = new HashMap<Object, Object>();
        }
        properties.put(_key, _value);
        _parameter.put(ParameterValues.PROPERTIES, properties);
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _key      Key of the property
     * @param _values    value of the property
     */
    protected static void setParmeterValue(final Parameter _parameter,
                                           final String _key,
                                           final String... _values)
    {
        Map<String, String[]> map = _parameter.getParameters();
        if (map == null) {
            map = new HashMap<String, String[]>();
            _parameter.put(ParameterValues.PARAMETERS, map);
        }
        map.put(_key, _values);
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _keys  list of keys the value will be searched for sequential
     * @return value if found else null
     */
    protected static String getParameterValue(final Parameter _parameter,
                                              final String... _keys)
    {
        String ret = null;
        for (final String key : _keys) {
            ret = _parameter.getParameterValue(key);
            if (ret != null) {
                break;
            }
        }
        return ret;
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _keys  list of keys the value will be searched for sequential
     * @return value if found else null
     */
    protected static String[] getParameterValues(final Parameter _parameter,
                                                 final String... _keys)
    {
        String[] ret = null;
        for (final String key : _keys) {
            ret = _parameter.getParameterValues(key);
            if (ret != null) {
                break;
            }
        }
        return ret;
    }
}
