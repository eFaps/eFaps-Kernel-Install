/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.esjp.admin.common.systemconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.annotation.EFapsSysConfAttribute;
import org.efaps.api.annotation.EFapsSysConfLink;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.rest.EFapsResourceConfig;
import org.efaps.rest.EFapsResourceConfig.EFapsResourceFinder;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
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
        @SuppressWarnings("unchecked")
        final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(
                        EFapsClassLoader.getInstance(),
                        EFapsSystemConfiguration.class);
        final EFapsResourceFinder resourceFinder = new EFapsResourceConfig.EFapsResourceFinder();
        while (resourceFinder.hasNext()) {
            final String next = resourceFinder.next();
            if (asl.accept(next)) {
                final InputStream in = resourceFinder.open();
                try {
                    SysConfResourceConfig.LOG.debug("Scanning '{}' for annotations.", next);
                    asl.process(next, in);
                } catch (final IOException e) {
                    SysConfResourceConfig.LOG.warn("Cannot process '{}'", next);
                } finally {
                    try {
                        in.close();
                    } catch (final IOException ex) {
                        SysConfResourceConfig.LOG.trace("Error closing resource stream.", ex);
                    }
                }
            }
        }
        for (final Class<?> clazz : asl.getAnnotatedClasses()) {
            final EFapsSystemConfiguration sysConfAn = clazz.getAnnotation(EFapsSystemConfiguration.class);
            final List<ISysConfAttribute> confAttrs = new ArrayList<>();
            final List<ISysConfLink> confLinks = new ArrayList<>();
            this.uuid2attr.put(sysConfAn.value(), confAttrs);
            this.uuid2link.put(sysConfAn.value(), confLinks);
            LOG.info("Found SystemConfiguration: {} - {}",
                            SystemConfiguration.get(UUID.fromString(sysConfAn.value())).getName(), sysConfAn.value());
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
                            LOG.info("    Found Link: {}", link);
                        } catch (final IllegalArgumentException | IllegalAccessException e) {
                            LOG.error("Catched error", e);
                        }
                    }
                }
            }
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
        List<ISysConfLink> ret;
        if (this.uuid2link.containsKey(_uuid)) {
            ret = this.uuid2link.get(_uuid);
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
        List<ISysConfAttribute> ret;
        if (this.uuid2attr.containsKey(_uuid)) {
            ret = this.uuid2attr.get(_uuid);
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
