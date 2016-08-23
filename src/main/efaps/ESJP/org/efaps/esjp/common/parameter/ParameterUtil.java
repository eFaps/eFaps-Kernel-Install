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
 */


package org.efaps.esjp.common.parameter;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("395d1934-0fed-480b-ae66-8367ac72348a")
@EFapsApplication("eFaps-Kernel")
public class ParameterUtil
    extends ParameterUtil_Base
{
    /**
     * Clone the Parameter by making a new Parameter. This does not clone
     * all the values. It makes a clone for the map for PARAMETERS, PROPERTIES
     * @param _parameter Paramter as passed by the eFaps API
     * @param _tuplets tuplets of ParameterValues, Value
     * @return new Parameter instance with a copy
     */
    public static Parameter clone(final Parameter _parameter,
                                  final Object... _tuplets)
    {
        return ParameterUtil_Base.clone(_parameter, _tuplets);
    }

    /**
     * @param _parameter    Paramter as passed by the eFaps API
     * @param _key          Key of the property
     * @param _value        value of the property
     */
    public static void setProperty(final Parameter _parameter,
                                   final String _key,
                                   final String _value)
    {
        ParameterUtil_Base.setProperty(_parameter, _key, _value);
    }

    /**
     * Sets the parameter values.
     *
     * @param _parameter Paramter as passed by the eFaps API
     * @param _key      Key of the property
     * @param _values    value of the property
     */
    public static void setParameterValues(final Parameter _parameter,
                                          final String _key,
                                          final String... _values)
    {
        ParameterUtil_Base.setParameterValues(_parameter, _key, _values);
    }

    /**
     * Sets the parameter value.
     *
     * @param _parameter Paramter as passed by the eFaps API
     * @param _key      Key of the property
     * @param _idx the idx
     * @param _value the value
     */
    public static void setParameterValue(final Parameter _parameter,
                                          final String _key,
                                          final int _idx,
                                          final String _value)
    {
        ParameterUtil_Base.setParameterValue(_parameter, _key, _idx, _value);
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _keys  list of keys the value will be searched for sequential
     * @return value if found else null
     */
    public static String[] getParameterValues(final Parameter _parameter,
                                              final String... _keys)
    {
        return ParameterUtil_Base.getParameterValues(_parameter, _keys);
    }

    /**
     * @param _parameter Paramter as passed by the eFaps API
     * @param _keys  list of keys the value will be searched for sequential
     * @return value if found else null
     */
    public static String getParameterValue(final Parameter _parameter,
                                           final String... _keys)
    {
        return ParameterUtil_Base.getParameterValue(_parameter, _keys);
    }
}
