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
package org.efaps.esjp.admin.common.systemconfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.api.annotation.EFapsSysConfAttribute;
import org.efaps.api.annotation.EFapsSysConfLink;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.esjp.admin.index.Search;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class JmsResourceConfig.
 *
 * @author The eFaps Team
 */
@EFapsUUID("f12b6149-a855-419d-a090-ef46b71011e5")
@EFapsApplication("eFaps-Kernel")
public final class SysConfResourceConfig
{

    /** The attrs. */
    private static Set<ISysConfAttribute> ATTRS = new HashSet<>();
    static {
        ATTRS.add(KernelConfigurations.ACCESS4OBJECT);
        ATTRS.add(KernelConfigurations.ACTIVATEMOSKITO);
        ATTRS.add(KernelConfigurations.ACTIVATEPIVOT);
        ATTRS.add(KernelConfigurations.INDEXBASEFOLDER);
        ATTRS.add(KernelConfigurations.INDEXLANG);
        ATTRS.add(KernelConfigurations.INDEXQUERYBLDR);
        ATTRS.add(KernelConfigurations.PREFERENCES);
        ATTRS.add(KernelConfigurations.SSO_PERMITATTRIBUTEUPDATE);
        ATTRS.add(KernelConfigurations.SSO_PERMITCOMPANYUPDATE);
        ATTRS.add(KernelConfigurations.SSO_PERMITCREATEPERSON);
        ATTRS.add(KernelConfigurations.SSO_PERMITROLEUPDATE);
        ATTRS.add(KernelConfigurations.TABLEFILTERDEFAULT);
        ATTRS.add(KernelConfigurations.LAZYLOAD);
        ATTRS.add(KernelConfigurations.JASPER_DATEFORMAT);
    }

    /**
     * Singleton instance.
     */
    private static SysConfResourceConfig CONFIG;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SysConfResourceConfig.class);

    /**
     * Attributes found by the scanner.
     */
    private final Map<String, List<ISysConfAttribute>> uuid2attr = new HashMap<>();

    /**
     * Links  found by the scanner.
     */
    private final Map<String, List<ISysConfLink>> uuid2link = new HashMap<>();

    /**
     * Constructor.
     */
    private SysConfResourceConfig()
    {
        // Singelton
    }

    /**
     * Initialize and scan for root resource and provider classes using a scanner.
     *
     * @throws EFapsException on error
     */
    protected void init()
        throws EFapsException
    {
        addKernelSysConf();
        for (final Class<?> clazz : new EsjpScanner().scan(EFapsSystemConfiguration.class)) {
            final EFapsSystemConfiguration sysConfAn = clazz.getAnnotation(EFapsSystemConfiguration.class);
            final List<ISysConfAttribute> confAttrs = new ArrayList<>();
            final List<ISysConfLink> confLinks = new ArrayList<>();
            uuid2attr.put(sysConfAn.value(), confAttrs);
            uuid2link.put(sysConfAn.value(), confLinks);
            LOG.debug("Found Annotation in class {}", clazz);
            final SystemConfiguration sysConfig = SystemConfiguration.get(UUID.fromString(sysConfAn.value()));
            if (sysConfig == null) {
                LOG.warn("No SystemConfiguratio found for : {}", sysConfAn.value());
            } else {
                LOG.info("Found SystemConfiguration: {} - {}", sysConfig.getName(), sysConfAn.value());
            }
            for (final Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(EFapsSysConfAttribute.class)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            final ISysConfAttribute attr = (ISysConfAttribute) field.get(null);
                            confAttrs.add(attr);
                            LOG.info("    Found Attribute: {}", attr);
                        } catch (final IllegalArgumentException | IllegalAccessException e) {
                            LOG.error("Catched error", e);
                        }
                    }
                } else if (field.isAnnotationPresent(EFapsSysConfLink.class)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            final ISysConfLink link = (ISysConfLink) field.get(null);
                            confLinks.add(link);
                            LOG.info("    Found Link     : {}", link);
                        } catch (final IllegalArgumentException | IllegalAccessException e) {
                            LOG.error("Catched error", e);
                        }
                    }
                }
            }
        }
        for (final ISysConfListener listener : Listener.get().<ISysConfListener>invoke(
                        ISysConfListener.class)) {
            listener.addAttributes(uuid2attr);
            listener.addLinks(uuid2link);
        }
    }

    /**
     * Adds the kernel system configuration manually.
     *
     * @throws CacheReloadException the cache reload exception
     */
    private void addKernelSysConf()
        throws CacheReloadException
    {
        LOG.info("Adding SystemConfiguration: {} - {}",
                        org.efaps.admin.EFapsSystemConfiguration.get().getName(),
                        org.efaps.admin.EFapsSystemConfiguration.get().getUUID());

        final List<ISysConfAttribute> attrs = new ArrayList<>();
        uuid2attr.put(org.efaps.admin.EFapsSystemConfiguration.get().getUUID().toString(), attrs);

        AbstractSysConfAttribute<?, ?> attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.ACTIVATE_BPM)
                        .description("Activate the BPM process mechanism");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.BPM_COMPILERLEVEL)
                        .description("The Java version to be used to compile the bpmn process files");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.ACTIVATE_GROUPS)
                        .description("Activate the Groups Access Mechanism");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.SHOW_DBPROPERTIES_KEY)
                        .description("Show the Keys for the DBProperties");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.REQUIRE_PERSON_UUID)
                        .description("Force the use of UUId for Persons");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.USERUI_DISPLAYPERSON)
                        .defaultValue("${LASTNAME}, ${FIRSTNAME}");
        LOG.info("    Add Attribute: {}", attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.LOGIN_MAX_TRIES)
                        .description(" Maximum number of tries to login with the wrong Password into\n"
                                        + " eFaps, before the User is going to be deactivated. To deactivate this\n"
                                        + " mechanism set the value to 0.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.LOGIN_TIME_RETRY)
                        .description(" This attribute defines the time in minutes which must elapse\n"
                                        + " after trying n-times to login with the wrong password, before the user\n"
                                        + " has again the possibility to try to login. To deactivate this mechanism\n"
                                        + " set the value to 0.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.DEACTIVATE_ACCESSCACHE)
                        .description("Deactivate the Caching mechanism for Access");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.DEACTIVATE_QUERYCACHE)
                        .description("Deactivate the Caching mechanism for Access");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.JMS_TIMEOOUT)
                        .description("TimeOut for JMS sessions. Default: 0");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.UPDATE_DEACTIVATEJSCOMP)
                        .description("Deactivate the Javascript Compression mechanism");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.UPDATE_ACTIVATEJSCOMPWAR)
                        .description("Activate the Javascript Compiler Warning mechanism");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.MSGTRIGGERACTIVE)
                        .description("Activate the Javascript Compiler Warning mechanism");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new PropertiesSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.QUARTZPROPS)
                        .description("Properties for the Quartz Scheduler");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.MSGTRIGGERINTERVAL)
                        .description("Interval for the SystemMessage Trigge");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.DBTIMEZONE)
                        .description("TimeZoneId from Java Definition");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new PropertiesSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.PWDSTORE)
                        .description("PasswordStore Digester Configuration");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.PWDTH)
                        .description(" The Threshold of how many passwords hashs will be stored to\n"
                                        + " compare it with a new given password.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new IntegerSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.PWDLENGHT)
                        .description("The Minimum length of a new Password");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new PropertiesSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.DEFAULTMENU)
                        .description(" This Attribute defines the Menu which is added as a DefaultMenu to all\n"
                                        + " Menubars. ");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.CLASSPATHS)
                        .description(" The path to the libaries the webapp is build on. Needed for\n"
                                        + " compilation of java and Jasperreports. e.g.\n"
                                        + " \"/tmp/Jetty_0_0_0_0_8060_efaps.war__efaps__.bo28gn/webapp/WEB-INF/lib/");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new PropertiesSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.PROFILES4UPDATE)
                        .concatenate(true)
                        .description("   Profiles to be applied on update if not specified explicitly.\n"
                                        + " Properties. Can be Concatenated.\n"
                                        + " e.g. for Archives:\n"
                                        + " eFapsApp-Sales=Role.AsList=ubicaciones;products");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new BooleanSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.INDEXACTIVATE)
                        .description(" Activate the general index mechanism.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.INDEXANALYZERPROVCLASS)
                        .description("ClassName of the class used for getting the Analyzer. Must implement\n"
                                        + " org.efaps.admin.index.IAnalyzerProvider");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.INDEXDIRECTORYPROVCLASS)
                        .description("ClassName of the class used for getting the Directory. Must implement\n"
                                        + " org.efaps.admin.index.IDirectoryProvider");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        /** See description. */
        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.INDEXSEARCHCLASS)
                        .defaultValue(Search.class.getName())
                        .description("ClassName of the class used for getting the Search. Must implement\n"
                                        + " org.efaps.admin.index.ISearch.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        /** See description. */
        attr = new StringSysConfAttribute()
                        .sysConfUUID(org.efaps.admin.EFapsSystemConfiguration.get().getUUID())
                        .key(KernelSettings.INDEXDEFAULTOP)
                        .defaultValue("AND")
                        .description("The default operator for the QueryParser Can be 'AND' or 'OR'. Default is set\n"
                                        + " to 'AND' for eFaps.");
        LOG.info("    Add Attribute: {}", attr);
        attrs.add(attr);

        for (final ISysConfAttribute attrTmp : ATTRS) {
            LOG.info("    Add Attribute: {}", attrTmp);
            attrs.add(attrTmp);
        }
    }

    /**
     * Gets the links.
     *
     * @param _uuid the _uuid
     * @return the links
     */
    public List<ISysConfLink> getLinks(final String _uuid)
    {
        final List<ISysConfLink> ret;
        if (uuid2link.containsKey(_uuid)) {
            ret = uuid2link.get(_uuid);
        } else {
            ret = new ArrayList<>();
        }
        return ret;
    }

    /**
     * Gets the link.
     *
     * @param _uuid the _uuid
     * @param _key the _key
     * @return the link
     */
    public ISysConfLink getLink(final String _uuid,
                                     final String _key)
    {
        ISysConfLink ret = null;
        for (final ISysConfLink link : getLinks(_uuid)) {
            if (link.getKey().equals(_key)) {
                ret = link;
                break;
            }
        }
        return ret;
    }

    /**
     * Gets the attributes.
     *
     * @param _uuid the _uuid
     * @param _key the _key
     * @return the attributes
     */
    public ISysConfAttribute getAttribute(final String _uuid,
                                          final String _key)
    {
        ISysConfAttribute ret = null;
        for (final ISysConfAttribute attr : getAttributes(_uuid)) {
            if (attr.getKey().equals(_key)) {
                ret = attr;
                break;
            }
        }
        return ret;
    }

    /**
     * Gets the attributes.
     *
     * @param _uuid the _uuid
     * @return the attributes
     */
    public List<ISysConfAttribute> getAttributes(final String _uuid)
    {
        final List<ISysConfAttribute> ret;
        if (uuid2attr.containsKey(_uuid)) {
            ret = uuid2attr.get(_uuid);
        } else {
            ret = new ArrayList<>();
        }
        return ret;
    }

    /**
     * Gets the resource config.
     *
     * @return the singleton JmsResourceConfig instance
     * @throws EFapsException on error
     */
    public static SysConfResourceConfig getResourceConfig()
        throws EFapsException
    {
        if (SysConfResourceConfig.CONFIG == null) {
            SysConfResourceConfig.CONFIG = new SysConfResourceConfig();
            CONFIG.init();
        }
        return SysConfResourceConfig.CONFIG;
    }
}
