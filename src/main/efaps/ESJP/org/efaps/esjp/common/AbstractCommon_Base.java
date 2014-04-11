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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.RandomStringUtils;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;


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
     * Regex for testing a UUID for valid.
     */
    //CHECKSTYLE:OFF
    public static final String UUID_REGEX = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}";
    //CHECKSTYLE:ON

    /**
     * Used to store a Key for caching mechanism in the RequestAttributes from
     * the Context.
     */
    public static final String REQUESTKEY4CACHING = AbstractCommon.class.getName() + ".UniqueKey4Request";

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
     * @param _key key of the Property
     * @return map with properties
     * @throws EFapsException on error
     */
    protected Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                   final String _key)
        throws EFapsException
    {
        final Map<Integer, String> ret = new TreeMap<Integer, String>();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        // test for basic
        if (properties.containsKey(_key)) {
            ret.put(0, String.valueOf(properties.get(_key)));
        }
        for (int i = 1; i < 100; i++) {
            final String nameTmp = _key + String.format("%02d", i);
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
     * @param _key key of the Property
     * @return value for the Property, null if not found
     * @throws EFapsException on error
     */
    protected String getProperty(final Parameter _parameter,
                                 final String _key)
        throws EFapsException
    {
        String ret = null;
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        // test for basic
        if (properties.containsKey(_key)) {
            ret = String.valueOf(properties.get(_key));
        }
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
    protected boolean containsProperty(final Parameter _parameter,
                                       final String _key)
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        return properties.containsKey(_key);
    }

    /**
     * Recursive method to get a Type with his children and children children as
     * a simple set.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @param _type Type type
     * @return set of types
     * @throws CacheReloadException on error
     */
    protected Set<Type> getTypeList(final Parameter _parameter,
                                    final Type _type)
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<Type>();
        ret.add(_type);
        for (final Type child : _type.getChildTypes()) {
            ret.addAll(getTypeList(_parameter, child));
        }
        return ret;
    }

    /**
     * @param _string string to validate
     * @return true if valid UUID else false.
     */
    protected boolean isUUID(final String _string)
    {
        return _string.matches(AbstractCommon_Base.UUID_REGEX);
    }

    /**
     * @return key used for Caching
     * @throws EFapsException on error
     */
    protected String getRequestKey()
        throws EFapsException
    {
        String ret;
        if (Context.getThreadContext().containsRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING)) {
            ret = (String) Context.getThreadContext().getRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING);
        } else {
            ret = RandomStringUtils.random(16);
            Context.getThreadContext().setRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING, ret);
        }
        return ret;
    }
}
