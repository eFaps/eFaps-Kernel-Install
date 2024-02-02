/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.common.properties;

import java.util.Map;
import java.util.Properties;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("ba63826a-7427-4c54-8859-2a834a760e72")
@EFapsApplication("eFaps-Kernel")
public class PropertiesUtil
    extends PropertiesUtil_Base
{

    /**
     * Get the properties map. Reads first the Map from
     * <code>ParameterValues.PROPERTIES</code> and than checks for overwrite by
     * a SystenConfiguration.
     *
     * @param _parameter the _parameter
     * @return the properties map internal
     * @throws EFapsException the e faps exception
     */
    public static Map<?, ?> getPropertiesMap(final Parameter _parameter)
        throws EFapsException
    {
        return PropertiesUtil_Base.getPropertiesMap(_parameter);
    }

    /**
     * Gets the properties.
     *
     * @param _parameter the parameter
     * @return the properties
     * @throws EFapsException the e faps exception
     */
    public static Properties getProperties(final Parameter _parameter)
        throws EFapsException
    {
        return PropertiesUtil_Base.getProperties(_parameter);
    }

    /**
     * Check for a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key of the Property
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    public static boolean containsProperty(final Parameter _parameter,
                                           final String _key)
        throws EFapsException
    {
        return PropertiesUtil_Base.containsProperty(_parameter, _key);
    }

    /**
     * Get a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key of the Property
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    public static String getProperty(final Parameter _parameter,
                                     final String _key)
        throws EFapsException
    {
        return PropertiesUtil_Base.getProperty(_parameter, _key);
    }

    /**
     * Get a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key of the Property
     * @param _defaultValue defaultvalue
     * @return value for the Property, if not found the defaultvalue
     * @throws EFapsException on error
     */
    public static String getProperty(final Parameter _parameter,
                                     final String _key,
                                     final String _defaultValue)
        throws EFapsException
    {
        return PropertiesUtil_Base.getProperty(_parameter, _key, _defaultValue);
    }

    /**
     * Analyse property.
     *
     * @param _parameter the parameter
     * @param _key the key
     * @param _offset the offset
     * @return the map
     * @throws EFapsException the e faps exception
     */
    public static Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                       final String _key,
                                                       final int _offset)
        throws EFapsException
    {

        return PropertiesUtil_Base.analyseProperty(_parameter, _key, _offset);
    }

    /**
     * Analyse property.
     *
     * @param _properties the properties
     * @param _key the key
     * @param _offset the offset
     * @return the map
     * @throws EFapsException the e faps exception
     */
    public static Map<Integer, String> analyseProperty(final Properties _properties,
                                                       final String _key,
                                                       final int _offset)
        throws EFapsException
    {
        return PropertiesUtil_Base.analyseProperty(_properties, _key, _offset);
    }

    /**
     * Gets the properties for a prefix.
     *
     * @param _properties the properties
     * @param _prefix the prefix
     * @return the properties4 prefix
     */
    public static Properties getProperties4Prefix(final Properties _properties,
                                                  final String _prefix)
    {
        return PropertiesUtil_Base.getProperties4Prefix(_properties, _prefix);
    }

    /**
     * Gets the properties4 prefix.
     *
     * @param _properties the properties
     * @param _prefix the prefix
     * @param _exclude exclude the property if it not starts with the prefix
     * @return the properties4 prefix
     */
    public static Properties getProperties4Prefix(final Properties _properties,
                                                  final String _prefix,
                                                  final boolean _exclude)
    {
        return PropertiesUtil_Base.getProperties4Prefix(_properties, _prefix, _exclude);
    }
}
