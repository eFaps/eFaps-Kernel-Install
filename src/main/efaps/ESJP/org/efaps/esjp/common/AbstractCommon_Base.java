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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.efaps.admin.datamodel.Status;
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
        throws EFapsException
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


    /**
     * To have always the same form of defining Status in esjp properties use this method.<br/>
     * Example for config: (StatusGrp can be the name or the UUID of a StatusGroup)<br/>
     * <ul><li>
     * <b>Only one:</b><br/>
     * &lt;property name=&quot;StatusGroup&quot;&gt;Accounting_PurchaseRecordStatus&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status&quot;&gt;Open&lt;/property&gt;
     * </li><li>
     * <b>Various in different StatusGroups:</b><br/>
     * &lt;property name=&quot;StatusGroup01&quot;&gt;StatusGrp1&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status01&quot;&gt;Open&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusGroup02&quot;&gt;StatusGrp2&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status02&quot;&gt;Closed&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusGroup03&quot;&gt;StatusGrp3&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status03&quot;&gt;Open&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusGroupNN&quot;&gt;StatusGrpNN&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusNN&quot;&gt;Open&lt;/property&gt;
     * </li><li>
     * <b>Various in the same StatusGroups:</b><br/>
     * &lt;property name=&quot;StatusGroup&quot;&gt;StatusGrp&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status01&quot;&gt;Closed&lt;/property&gt;<br/>
     * &lt;property name=&quot;Status02&quot;&gt;Open&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusNN&quot;&gt;Open&lt;/property&gt;
     * </li></ul>
     * @param _parameter Parameter as passed by the eFaps API
     * @return List of Status, empty List if not found
     * @throws EFapsException on error
     */
    protected List<Status> getStatusListFromProperties(final Parameter _parameter)
        throws EFapsException
    {
        final List<Status> ret = new ArrayList<Status>();
        final Map<Integer, String> statusGroupMap = analyseProperty(_parameter, "StatusGroup");
        if (!statusGroupMap.isEmpty()) {
            final String defaultStaturGrp = statusGroupMap.values().iterator().next();
            final Map<Integer, String> statusMap = analyseProperty(_parameter, "Status");
            for (final Entry<Integer, String> entry : statusMap.entrySet()) {
                final String stGrpStr = statusGroupMap.containsKey(entry.getKey()) ? statusGroupMap.get(entry.getKey())
                                : defaultStaturGrp;
                final Status status;
                if (isUUID(stGrpStr)) {
                    status = Status.find(UUID.fromString(stGrpStr), entry.getValue());
                } else {
                    status = Status.find(stGrpStr, entry.getValue());
                }
                if (status != null) {
                    ret.add(status);
                }
            }
        }
        return ret;
    }
}
