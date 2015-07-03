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

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.annotation.EFapsSysConfAttribute;
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
     * Classes found by the scanner.
     */
    private final Map<String, List<ISysConfAttribute>> uuid2attr = new HashMap<>();

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
            this.uuid2attr.put(sysConfAn.value(), confAttrs);
            LOG.info("Found SystemConfiguration: {}", sysConfAn.value());
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
                }
            }
        }
    }

    /**
     * Gets the attributes.
     *
     * @param _uuid the _uuid
     * @return the attributes
     */
    public List<ISysConfAttribute> getAttributes(final String _uuid)
    {
        return this.uuid2attr.get(_uuid);
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
