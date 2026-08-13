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
package org.efaps.esjp.common.rest.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("e791f0cc-2c5a-4f3e-ae84-991d585a6929")
@EFapsApplication("eFaps-Kernel")
public class RestClientManager
{

    private static final Logger LOG = LoggerFactory.getLogger(RestClientManager.class);

    private static RestClientManager manager = new RestClientManager();

    private Client client;

    protected void initClient()
    {
        LOG.debug("Getting Client");
        final ClientConfig clientConfig = new ClientConfig();
        try {
            final Class<?> clazz = Class.forName("org.efaps.esjp.logback.jersey.JerseyLogFeature");
            if (clazz != null) {
                final Object filter = clazz.getDeclaredConstructor().newInstance();
                final Method method = clazz.getMethod("setLogger", Logger.class);
                method.invoke(filter, LOG);
                clientConfig.register(filter);
                // force load just in case
                Class.forName("org.efaps.esjp.logback.jersey.JerseyLogFilter");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("Catched", e);
        }
        client = ClientBuilder.newClient(clientConfig)
                        .register(JacksonFeature.class);
    }

    public static Client getClient()
    {
        if (manager.client == null) {
            manager.initClient();
        }
        return manager.client;
    }
}
