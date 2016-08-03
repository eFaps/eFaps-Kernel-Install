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

package org.efaps.esjp.common.properties;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;

/**
 * Util class for Properties management.
 *
 * @author The eFaps Team
 */
@EFapsUUID("3e4ee06b-6f4a-4806-a736-e7f48d438b74")
@EFapsApplication("eFaps-Kernel")
public abstract class PropertiesUtil_Base
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
    @SuppressWarnings("unchecked")
    protected static Map<?, ?> getPropertiesMap(final Parameter _parameter)
        throws EFapsException
    {
        Map<Object, Object> ret = (Map<Object, Object>) _parameter.get(ParameterValues.PROPERTIES);
        if (ret != null && ret.containsKey("PropertiesConfig")) {
            final String config = (String) ret.get("PropertiesConfig");
            final SystemConfiguration sysConf;
            if (UUIDUtil.isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            if (sysConf != null) {
                final Properties props = sysConf
                                .getAttributeValueAsProperties((String) ret.get("PropertiesConfigAttribute"));
                ret = (Map<Object, Object>) _parameter.get(ParameterValues.PROPERTIES);
                ret.putAll(props);
            }
        }
        return ret;
    }

    /**
     * Get the properties map. Reads first the Map from
     * <code>ParameterValues.PROPERTIES</code> and than checks for overwrite by
     * a SystenConfiguration.
     *
     * @param _parameter the _parameter
     * @return the properties map internal
     * @throws EFapsException the e faps exception
     */
    protected static Properties getProperties(final Parameter _parameter)
        throws EFapsException
    {
        final Properties ret = new Properties();
        final Map<?, ?> map = PropertiesUtil.getPropertiesMap(_parameter);
        ret.putAll(map);
        return ret;
    }

    /**
     * Check for a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key of the Property
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    protected static boolean containsProperty(final Parameter _parameter,
                                              final String _key)
        throws EFapsException
    {
        final Map<?, ?> propertiesMap = PropertiesUtil.getPropertiesMap(_parameter);
        return propertiesMap.containsKey(_key);
    }

    /**
     * Get a Property form the ParameterValues.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key of the Property
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    protected static String getProperty(final Parameter _parameter,
                                        final String _key)
        throws EFapsException
    {
        return PropertiesUtil.getProperty(_parameter, _key, null);
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
    protected static String getProperty(final Parameter _parameter,
                                        final String _key,
                                        final String _defaultValue)
        throws EFapsException
    {
        final String ret;
        if (containsProperty(_parameter, _key)) {
            final Map<?, ?> propertiesMap = PropertiesUtil.getPropertiesMap(_parameter);
            ret = String.valueOf(propertiesMap.get(_key));
        } else {
            ret = _defaultValue;
        }
        return ret;
    }

    /**
     * Search for the given Property and returns a tree map with the found
     * values.<br/>
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
     * @param _key key of the Property
     * @param _offset offset to start to search for
     * @return map with properties
     * @throws EFapsException on error
     */
    protected static Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                          final String _key,
                                                          final int _offset)
        throws EFapsException
    {
        return PropertiesUtil.analyseProperty(PropertiesUtil.getProperties(_parameter), _key, _offset);
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
    protected static Map<Integer, String> analyseProperty(final Properties _properties,
                                                          final String _key,
                                                          final int _offset)
        throws EFapsException
    {
        final Map<Integer, String> ret = new TreeMap<>();
        // test for basic
        final int start = _offset == 0 ? 1 : _offset;
        if (_offset == 0 && _properties.containsKey(_key)) {
            ret.put(0, String.valueOf(_properties.get(_key)));
        }
        String formatStr = "%02d";
        if (start > 99) {
            formatStr = "%03d";
        }
        for (int i = start; i < start + 100; i++) {
            final String nameTmp = _key + String.format(formatStr, i);
            if (_properties.containsKey(nameTmp)) {
                ret.put(i, String.valueOf(_properties.get(nameTmp)));
            } else {
                break;
            }
        }
        return ret;
    }

    /**
     * Gets the properties4 prefix.
     *
     * @param _properties the properties
     * @param _prefix the prefix
     * @return the properties4 prefix
     */
    protected static Properties getProperties4Prefix(final Properties _properties,
                                                     final String _prefix)
    {
        return PropertiesUtil.getProperties4Prefix(_properties, _prefix, false);
    }

    /**
     * Gets the properties4 prefix.
     *
     * @param _properties the properties
     * @param _prefix the prefix
     * @param _exclude exclude the property if it not starts with the prefix
     * @return the properties4 prefix
     */
    protected static Properties getProperties4Prefix(final Properties _properties,
                                                     final String _prefix,
                                                     final boolean _exclude)
    {
        final Properties ret = new Properties();
        final String key = _prefix == null ? null : _prefix + ".";
        for (final Entry<Object, Object> entry : _properties.entrySet()) {
            if (_prefix != null) {
                if (entry.getKey().toString().startsWith(key)) {
                    ret.put(entry.getKey().toString().replace(key, ""), entry.getValue());
                } else if (!_exclude) {
                    ret.put(entry.getKey(), entry.getValue());
                }
            } else {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret;
    }
}
