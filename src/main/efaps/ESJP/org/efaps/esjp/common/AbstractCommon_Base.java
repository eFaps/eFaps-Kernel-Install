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


package org.efaps.esjp.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class contains some generic methods used by its subclasses.
 *
 * @author The eFaps Team
 */
@EFapsUUID("b374b7e1-2598-4841-b77c-542ed39c721e")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractCommon_Base
{
    /**
     * Used to store a Key for caching mechanism in the RequestAttributes from
     * the Context.
     */
    public static final String REQUESTKEY4CACHING = AbstractCommon.class.getName() + ".UniqueKey4Request";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCommon.class);

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
        return analyseProperty(_parameter, _key, 0);
    }

    /**
     * Analyse property.
     *
     * @param _parameter the _parameter
     * @param _properties the _properties
     * @param _key the _key
     * @return the map
     * @throws EFapsException on error
     */
    protected Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                   final Properties _properties,
                                                   final String _key)
        throws EFapsException
    {
        return analyseProperty(_parameter, _properties, _key, 0);
    }

    /**
     * Analyse property.
     *
     * @param _parameter the _parameter
     * @param _properties the _properties
     * @param _key the _key
     * @param _offset the _offset
     * @return the map
     * @throws EFapsException on error
     */
    protected Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                   final Properties _properties,
                                                   final String _key,
                                                   final int _offset)
        throws EFapsException
    {
        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.PROPERTIES, _properties);
        return analyseProperty(parameter, _key, _offset);
    }

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
     * @param _offset offset to start to search for
     * @return map with properties
     * @throws EFapsException on error
     */
    protected Map<Integer, String> analyseProperty(final Parameter _parameter,
                                                   final String _key,
                                                   final int _offset)
        throws EFapsException
    {
        final Map<Integer, String> ret = new TreeMap<Integer, String>();
        final Map<?, ?> properties = getPropertiesMapInternal(_parameter);
        // test for basic
        final int start = _offset == 0 ? 1 : _offset;
        if (_offset == 0 && properties.containsKey(_key)) {
            ret.put(0, String.valueOf(properties.get(_key)));
        }
        String formatStr = "%02d";
        if (start > 99) {
            formatStr = "%03d";
        }
        for (int i = start; i < start + 100; i++) {
            final String nameTmp = _key + String.format(formatStr, i);
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
     * @param _defaultValue defaultvalue
     * @return value for the Property, if not found the defaultvalue
     * @throws EFapsException on error
     */
    protected String getProperty(final Parameter _parameter,
                                 final String _key,
                                 final String _defaultValue)
        throws EFapsException
    {
        final String ret;
        if (containsProperty(_parameter, _key)) {
            ret = getProperty(_parameter, _key);
        } else {
            ret  = _defaultValue;
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
        final Map<?, ?> properties = getPropertiesMapInternal(_parameter);
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
        final Map<?, ?> properties = getPropertiesMapInternal(_parameter);
        return properties.containsKey(_key);
    }

    /**
     * Get the properties map. Reads first the Map from
     * <code>ParameterValues.PROPERTIES</code> and than
     * checks for overwrite by a SystenConfiguration.
     *
     * @param _parameter the _parameter
     * @return the properties map internal
     * @throws EFapsException the e faps exception
     */
    @SuppressWarnings("unchecked")
    private Map<?, ?> getPropertiesMapInternal(final Parameter _parameter)
        throws EFapsException
    {
        Map<Object, Object> ret = (Map<Object, Object>) _parameter.get(ParameterValues.PROPERTIES);
        if (ret != null && ret.containsKey("PropertiesConfig")) {
            final String config = (String) ret.get("PropertiesConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
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
        return UUIDUtil.isUUID(_string);
    }

    /**
     * @return key used for Caching
     * @throws EFapsException on error
     */
    protected String getRequestKey()
        throws EFapsException
    {
        final String ret;
        if (Context.getThreadContext().containsRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING)) {
            ret = (String) Context.getThreadContext().getRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING);
        } else {
            ret = RandomStringUtils.random(16);
            Context.getThreadContext().setRequestAttribute(AbstractCommon_Base.REQUESTKEY4CACHING, ret);
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _props properties the QueryBuilder is wanted form
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected List<Status> getStatusListFromProperties(final Parameter _parameter,
                                                       final Properties _props)
        throws EFapsException
    {
        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.PROPERTIES, _props);
        return getStatusListFromProperties(parameter, 0);
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
     * <b>Negate a Status:</b><br/>
     * To work with exclusion the value for the property StatusNN must be prefixed with a "!" e.g.<br>
     * &lt;property name=&quot;StatusNN&quot;&gt;!Open&lt;/property&gt;<br/><br/>
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return List of Status, empty List if not found
     * @throws EFapsException on error
     */
    protected List<Status> getStatusListFromProperties(final Parameter _parameter)
        throws EFapsException
    {
        return getStatusListFromProperties(_parameter, 0);
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
     * <b>Negate a Status:</b><br/>
     * To work with exclusion the value for the property StatusNN must be prefixed with a "!" e.g.<br>
     * &lt;property name=&quot;StatusNN&quot;&gt;!Open&lt;/property&gt;<br/><br/>
     * <b>Include all Status from a StatusGroup:</b><br/>
     * To include all Status from a StatusGroup the value for the property StatusNN must be "*" e.g.<br>
     * &lt;property name=&quot;StatusGroupNN&quot;&gt;StatusGrp&lt;/property&gt;<br/>
     * &lt;property name=&quot;StatusNN&quot;&gt;*&lt;/property&gt;<br/><br/>
     * @param _parameter Parameter as passed by the eFaps API
     * @return List of Status, empty List if not found
     * @param _offset offset to start to search for
     * @throws EFapsException on error
     */
    protected List<Status> getStatusListFromProperties(final Parameter _parameter,
                                                       final int _offset)
        throws EFapsException
    {
        final Set<Status> ret = new HashSet<Status>();
        final Set<Status> negateList = new HashSet<Status>();
        final Map<Integer, String> statusGroupMap = analyseProperty(_parameter, "StatusGroup", _offset);
        // no statusGroup was set, try to get it from the instance
        if (statusGroupMap.isEmpty()) {
            final Instance inst = _parameter.getInstance();
            if (inst != null && inst.isValid() &&  inst.getType().isCheckStatus()) {
                statusGroupMap.put(_offset, inst.getType().getStatusAttribute().getLink().getUUID().toString());
            }
        }

        if (!statusGroupMap.isEmpty()) {
            final String defaultStaturGrp = statusGroupMap.values().iterator().next();
            final Map<Integer, String> statusMap = analyseProperty(_parameter, "Status", _offset);
            for (final Entry<Integer, String> entry : statusMap.entrySet()) {
                final String stGrpStr = statusGroupMap.containsKey(entry.getKey()) ? statusGroupMap.get(entry.getKey())
                                : defaultStaturGrp;

                String statusStr = entry.getValue();
                if (statusStr.equals("*")) {
                    final StatusGroup stGrp;
                    if (isUUID(stGrpStr)) {
                        stGrp = Status.get(UUID.fromString(stGrpStr));
                    } else {
                        stGrp = Status.get(stGrpStr);
                    }
                    ret.addAll(stGrp.values());
                } else {
                    boolean negate = false;
                    if (statusStr.startsWith("!")) {
                        statusStr = statusStr.substring(1);
                        negate = true;
                    }
                    final Status status;
                    if (isUUID(stGrpStr)) {
                        status = Status.find(UUID.fromString(stGrpStr), statusStr);
                    } else {
                        status = Status.find(stGrpStr, statusStr);
                    }
                    if (status != null) {
                        if (negate) {
                            negateList.add(status);
                            ret.addAll(status.getStatusGroup().values());
                        } else {
                            ret.add(status);
                        }
                    } else {
                        final AbstractUserInterfaceObject command = (AbstractUserInterfaceObject) _parameter
                                        .get(ParameterValues.UIOBJECT);
                        AbstractCommon_Base.LOG.error("Status Definition invalid. Command: {}, Index: {}",
                                        command == null ? "UNKNOWN" : command.getName(), entry.getKey());
                        throw new EFapsException(getClass(), "Status", entry);
                    }
                }
            }
        }
        ret.removeAll(negateList);
        return new ArrayList<Status>(ret);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _props properties the QueryBuilder is wanted form
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromProperties(final Parameter _parameter,
                                                      final Properties _props)
        throws EFapsException
    {
        return getQueryBldrFromProperties(_parameter, _props, null);
    }

    /**
     * Gets the query bldr from properties.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _props properties the QueryBuilder is wanted form
     * @param _prefix the prefix
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromProperties(final Parameter _parameter,
                                                      final Properties _props,
                                                      final String _prefix)
        throws EFapsException
    {
        return getQueryBldrFromProperties(_parameter, _props, _prefix, 0);
    }

    /**
     * Gets the querybldr from properties.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _props properties the QueryBuilder is wanted form
     * @param _prefix the prefix
     * @param _offSet the offset
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromProperties(final Parameter _parameter,
                                                      final Properties _props,
                                                      final String _prefix,
                                                      final int _offSet)
        throws EFapsException
    {
        final Properties props = getProperties4Prefix(_props, _prefix, true);
        final Map<Object, Object> propsMap = props.entrySet().stream().collect(
                        Collectors.toMap(e -> e.getKey().toString(),
                                         e -> e.getValue().toString()));
        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.PROPERTIES, propsMap);
        return getQueryBldrFromPropertiesInternal(parameter, _offSet);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromProperties(final Parameter _parameter)
        throws EFapsException
    {
        return getQueryBldrFromProperties(_parameter, 0);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _offset offset to start to search for
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromProperties(final Parameter _parameter,
                                                      final int _offset)
        throws EFapsException
    {
        final QueryBuilder ret;
        if (containsProperty(_parameter, "QueryBldrConfig")) {
            final String config = getProperty(_parameter, "QueryBldrConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            if (sysConf != null) {
                final Properties props = sysConf.getAttributeValueAsProperties(getProperty(_parameter,
                                "QueryBldrConfigAttribute"));
                ret = getQueryBldrFromProperties(_parameter, props, null, _offset);
            } else {
                ret = getQueryBldrFromPropertiesInternal(_parameter, _offset);
            }
        } else {
            ret = getQueryBldrFromPropertiesInternal(_parameter, _offset);
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _offset offset to start to search for
     * @return QueryBuilder
     * @throws EFapsException on error
     */
    protected QueryBuilder getQueryBldrFromPropertiesInternal(final Parameter _parameter,
                                                              final int _offset)
        throws EFapsException
    {
        QueryBuilder ret = null;
        final Map<Integer, String> types = analyseProperty(_parameter, "Type", _offset);
        final Map<Integer, String> linkFroms = analyseProperty(_parameter, "LinkFrom", _offset);
        final Map<Integer, String> classif = analyseProperty(_parameter, "Classification", _offset);
        final Map<Integer, String> expands = analyseProperty(_parameter, "ExpandChildTypes", _offset);
        boolean first = true;
        boolean multiple = false;
        final List<Type> excludes = new ArrayList<Type>();
        for (final Entry<Integer, String> typeEntry : types.entrySet()) {
            final Type type;
            String typeStr = typeEntry.getValue();
            boolean negate = false;
            if (typeStr.startsWith("!")) {
                typeStr = typeStr.substring(1);
                negate = true;
            }

            if (isUUID(typeEntry.getValue())) {
                type = Type.get(UUID.fromString(typeStr));
            } else {
                type = Type.get(typeStr);
            }
            if (type == null) {
                final AbstractUserInterfaceObject command = (AbstractUserInterfaceObject) _parameter
                                .get(ParameterValues.UIOBJECT);
                AbstractCommon_Base.LOG.error("Type Definition invalid. Object: {}, Index: {}",
                                command == null ? "UNKNOWN" : command.getName(), typeEntry.getKey());
            } else {
                if (negate) {
                    excludes.add(type);
                } else {
                    if (first) {
                        ret = new QueryBuilder(type);
                        if (linkFroms.size() == 1 && linkFroms.containsKey(typeEntry.getKey())) {
                            ret.addWhereAttrEqValue(linkFroms.get(typeEntry.getKey()),
                                            getInstance4LinkFrom(_parameter));
                        }
                        // in case of a simple query set the includechilds here
                        if (types.size() == 1 && expands.size() == 1) {
                            ret.setIncludeChildTypes(!"false".equalsIgnoreCase(expands.get(typeEntry.getKey())));
                        } else if (expands.size() > 1 && "true".equalsIgnoreCase(expands.get(typeEntry.getKey()))) {
                            final Set<Type> typeList = getTypeList(_parameter, type);
                            ret.addType(typeList.toArray(new Type[typeList.size()]));
                        }
                        first = false;
                    } else {
                        ret.addType(type);
                        if (expands.size() > 1 && "true".equalsIgnoreCase(expands.get(typeEntry.getKey()))) {
                            final Set<Type> typeList = getTypeList(_parameter, type);
                            ret.addType(typeList.toArray(new Type[typeList.size()]));
                        }
                        multiple = true;
                    }
                }
            }
        }

        if (ret != null) {
            if (!excludes.isEmpty()) {
                final List<Long> typeIds = new ArrayList<Long>();
                for (final Type type : excludes) {
                    for (final Type exType :  getTypeList(_parameter, type)) {
                        typeIds.add(exType.getId());
                    }
                }
                ret.addWhereAttrNotEqValue(ret.getType().getTypeAttribute(), typeIds.toArray());
            }
            final Set<Classification> clazzes = new HashSet<>();
            for (final String classStr : classif.values()) {
                final Classification clazz;
                if (isUUID(classStr)) {
                    clazz = Classification.get(UUID.fromString(classStr));
                } else {
                    clazz = Classification.get(classStr);
                }
                if (clazz == null) {
                    final AbstractUserInterfaceObject command = (AbstractUserInterfaceObject) _parameter
                                    .get(ParameterValues.UIOBJECT);
                    AbstractCommon_Base.LOG.error("Classification Definition invalid. Object: {}, Value: {}",
                                    command == null ? "UNKNOWN" : command.getName(), classStr);
                } else {
                    clazzes.add(clazz);
                }
            }
            if (!clazzes.isEmpty()) {
                ret.addWhereClassification(clazzes.toArray(new Classification[clazzes.size()]));
            }

            final List<Status> statusList = getStatusListFromProperties(_parameter, _offset);
            if (!statusList.isEmpty()) {
                Type tempType = ret.getType();
                while (!tempType.isCheckStatus() && tempType.getParentType() != null) {
                    tempType = tempType.getParentType();
                }
                ret.addWhereAttrEqValue(tempType.getStatusAttribute(), statusList.toArray());
            }
            // in case of multiple, the linkfrom must be evaluated
            if (multiple && linkFroms.size() > 1) {
                final QueryBuilder attrQueryBldr = new QueryBuilder(ret.getType());
                attrQueryBldr.setOr(true);
                final Set<List<String>> added = new HashSet<List<String>>();
                for (final Entry<Integer, String> entry : linkFroms.entrySet()) {
                    if (types.containsKey(entry.getKey())) {
                        final String typeStr = types.get(entry.getKey());
                        final Type type;
                        if (isUUID(typeStr)) {
                            type = Type.get(UUID.fromString(typeStr));
                        } else {
                            type = Type.get(typeStr);
                        }
                        if (type != null) {
                            final List<String> colNames = type.getAttribute(entry.getValue()).getSqlColNames();
                            for (final Attribute attr : ret.getType().getAttributes().values()) {
                                if (CollectionUtils.isEqualCollection(colNames, attr.getSqlColNames())) {
                                    if (!added.contains(colNames)) {
                                        attrQueryBldr.addWhereAttrEqValue(attr, getInstance4LinkFrom(_parameter));
                                        added.add(colNames);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!added.isEmpty()) {
                    ret.addWhereAttrInQuery("ID", attrQueryBldr.getAttributeQuery("ID"));
                }
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Instance use for the whre on Linkfroms
     * @throws EFapsException on error
     */
    protected Instance getInstance4LinkFrom(final Parameter _parameter)
        throws EFapsException
    {
        return _parameter.getInstance();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return List of instances, if not found empty list
     * @throws EFapsException on error
     */
    protected List<Instance> getSelectedInstances(final Parameter _parameter)
        throws EFapsException
    {
        return getInstances(_parameter, "selectedRow", true);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key the values are
     * @return List of instances, if not found empty list
     * @throws EFapsException on error
     */
    protected List<Instance> getInstances(final Parameter _parameter,
                                          final String _key)
        throws EFapsException
    {

        return getInstances(_parameter, _key, false);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key the values are
     * @param _evalOthers consider to get the StringArry from OTHERS
     * @return List of instances, if not found empty list
     * @throws EFapsException on error
     */
    protected List<Instance> getInstances(final Parameter _parameter,
                                          final String _key,
                                          final boolean _evalOthers)
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<>();
        String[] oids = _parameter.getParameterValues(_key);
        if (oids == null && _evalOthers) {
            final Object tmp = _parameter.get(ParameterValues.OTHERS);
            if (tmp != null && tmp instanceof String[]) {
                oids = (String[]) tmp;
            }
        }
        if (oids != null) {
            for (final String oid : oids) {
                final Instance instance = Instance.get(oid);
                if (instance.isValid()) {
                    ret.add(instance);
                }
            }
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return map with statuses
     * @throws EFapsException on error
     */
    public Map<Status, Status> getStatusMapping(final Parameter _parameter)
        throws EFapsException
    {
        final Map<Status, Status> ret = new HashMap<>();
        final Instance docInst = _parameter.getInstance();
        if (docInst != null && docInst.isValid()) {
            final Map<Integer, String> keys = analyseProperty(_parameter, "StatusMapKey");
            final Map<Integer, String> values = analyseProperty(_parameter, "StatusMapValue");
            for (final Entry<Integer, String> entry : keys.entrySet()) {
                final Status keyStatus = Status.find(docInst.getType().getStatusAttribute().getLink().getUUID(),
                                entry.getValue());
                if (keyStatus != null && values.containsKey(entry.getKey())) {
                    final Status valueStatus = Status.find(docInst.getType().getStatusAttribute().getLink().getUUID(),
                                    values.get(entry.getKey()));
                    if (valueStatus != null) {
                        ret.put(keyStatus, valueStatus);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param _key key the label is wanted for
     * @return class dependend Property
     */
    public String getDBProperty(final String _key)
    {
        return DBProperties.getProperty(this.getClass().getName() + "." + _key);
    }

    /**
     * Gets the formated db property.
     *
     * @param _key key the label is wanted for
     * @param _args the _args
     * @return class dependend Property
     */
    public String getFormatedDBProperty(final String _key,
                                        final Object... _args)
    {
        return DBProperties.getFormatedDBProperty(this.getClass().getName() + "." + _key,  _args);
    }

    /**
     * Gets the query bldr from properties.
     *
     * @param _properties the _properties
     * @return the query bldr from properties
     * @throws EFapsException on error
     */
    protected static QueryBuilder getQueryBldrFromProperties(final Properties _properties)
        throws EFapsException
    {
        final Parameter parameter = new Parameter();
        final AbstractCommon cm = new AbstractCommon()
        {
        };
        return cm.getQueryBldrFromProperties(parameter, _properties);
    }

    /**
     * Gets the properties4 prefix.
     *
     * @param _properties the properties
     * @param _prefix the prefix
     * @return the properties4 prefix
     */
    protected static Properties getProperties4Prefix(final Properties _properties,
                                                     final String _prefix){
        return getProperties4Prefix(_properties, _prefix, false);
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
                                                     final boolean _exclude){
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
