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
package org.efaps.esjp.common.webhook;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsListener;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.eql.EQL;
import org.efaps.esjp.admin.common.IReloadCacheListener;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.rest.client.RestClientManager;
import org.efaps.esjp.common.serialization.SerializationUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.ContextualSupplier;
import jakarta.transaction.Synchronization;

@EFapsUUID("55cb620a-5ed4-4862-abaa-b336df87cda7")
@EFapsApplication("eFaps-Kernel")
@EFapsListener
public class Webhook
    implements IReloadCacheListener
{

    private static final Logger LOG = LoggerFactory.getLogger(Webhook.class);

    private static MultiValuedMap<String, Entry> WEBHOOKS = null;

    private static final String HMAC_SHA256 = "HmacSHA256";

    private void init()
        throws EFapsException
    {
        // ensure RestClientManager is loaded
        RestClientManager.getClient();
        WEBHOOKS = MultiMapUtils.newListValuedHashMap();
        final var eval = EQL.builder().print().query(CICommon.Webhook)
                        .where().attribute(CICommon.Webhook.Status).eq(CICommon.WebhookStatus.Active)
                        .select().attribute(CICommon.Webhook.Name, CICommon.Webhook.EventTypes,
                                        CICommon.Webhook.SignKeys, CICommon.Webhook.URL)
                        .evaluate();
        while (eval.next()) {
            final String eventTypesStr = eval.get(CICommon.Webhook.EventTypes);
            final String name = eval.get(CICommon.Webhook.Name);
            final String signKeys = eval.get(CICommon.Webhook.SignKeys);
            final String url = eval.get(CICommon.Webhook.URL);
            Arrays.stream(eventTypesStr.split("\n")).forEach(eventType -> {
                final var entry = new Entry();
                entry.name = name;
                entry.signKeys = signKeys;
                entry.url = url;
                WEBHOOKS.put(eventType, entry);
            });
        }
    }

    private void send(final String eventType,
                      final Entry entry,
                      final String payloadStr)
    {
        try {
            final var msgId = getMsgId();

            LOG.info("Calling webhook with content: {}", payloadStr);

            final var client = RestClientManager.getClient();
            LOG.info("client {}", client);

            final var retryPolicy = RetryPolicy.<Boolean>builder()
                            .withMaxAttempts(10)
                            .withBackoff(Duration.ofSeconds(1), Duration.ofSeconds(600))
                            .handleResultIf(e -> !e)
                            .build();
            Failsafe.with(retryPolicy).getAsync((ContextualSupplier<Boolean, Boolean>) context -> {
                var result = false;
                try {
                    final var count = context.getAttemptCount();
                    LOG.info("intent {}", count);

                    final var request = client.target(entry.url)
                                    .request();
                    request.header("webhook-id", msgId);
                    final var timeStamp = Instant.now().getEpochSecond();
                    request.header("webhook-timestamp", timeStamp);
                    request.header("webhook-signature", sign(entry.signKeys, msgId, timeStamp, payloadStr));

                    LOG.info("request {}", request);

                    final var response = request.buildPost(Entity.entity(payloadStr, MediaType.APPLICATION_JSON))
                                    .invoke();

                    if (Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                        result = true;
                    }
                } catch (final Exception e) {
                    LOG.error("Catched", e);
                }
                return result;
            });
        } catch (final Exception e) {
            LOG.error("Catched error on send", e);
        }
    }

    protected final String sign(final String signKeys,
                                final String msgId,
                                final long timestamp,
                                final String payload)
    {
        LOG.info("signKeys {}", signKeys);
        final StringBuilder strBldr = new StringBuilder();
        Arrays.stream(signKeys.split("\n")).forEach(keyStr -> {
            try {
                final var key = Base64.getDecoder().decode(keyStr);
                final String toSign = String.format("%s.%s.%s", msgId, timestamp, payload);
                LOG.info("signing: {}", toSign);
                final Mac sha512Hmac = Mac.getInstance(HMAC_SHA256);
                final SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_SHA256);
                sha512Hmac.init(keySpec);
                final byte[] macData = sha512Hmac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));

                final String signature = Base64.getEncoder().encodeToString(macData);
                if (!strBldr.isEmpty()) {
                    strBldr.append(" ");
                }
                strBldr.append(String.format("v1,%s", signature));
                LOG.info("sign: {}", strBldr);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException e) {
                LOG.error("Catched", e);
            }
        });
        return strBldr.toString();
    }

    private String getMsgId()
    {
        final var random = RandomStringUtils.insecure().nextAlphanumeric(28);
        return "msg_" + random;
    }

    public void trigger(final String eventType,
                        final Object data)
        throws EFapsException
    {
        if (WEBHOOKS == null) {
            init();
        }
        LOG.debug("Trigger request for eventType: {}, data: {}", eventType, data);
        if (WEBHOOKS.containsKey(eventType)) {
            for (final var entry : WEBHOOKS.get(eventType)) {
                register(eventType, entry, data);
            }
        } else {
            LOG.debug("no register required");
        }
    }

    public Return ping(final Parameter parameter)
        throws EFapsException, JsonProcessingException
    {
        LOG.info("Sending ping to: {}", parameter.getInstance());
        // ensure RestClientManager is loaded
        RestClientManager.getClient();
        final var eval = EQL.builder().print(parameter.getInstance())
                        .attribute(CICommon.Webhook.Name, CICommon.Webhook.EventTypes,
                                        CICommon.Webhook.SignKeys, CICommon.Webhook.URL)
                        .evaluate();
        if (eval.next()) {
            final String name = eval.get(CICommon.Webhook.Name);
            final String signKeys = eval.get(CICommon.Webhook.SignKeys);
            final String url = eval.get(CICommon.Webhook.URL);
            final var entry = new Entry();
            entry.name = name;
            entry.signKeys = signKeys;
            entry.url = url;

            LOG.info("entry: {}", entry);
            register("ping", entry, PingDto.builder().withMsg("Hello World").build());
        }
        return new Return();
    }

    public void register(final String eventType,
                         final Entry entry,
                         final Object data)
        throws EFapsException
    {
        final var payload = PayloadDto.builder()
                        .withType(eventType)
                        .withTimestamp(OffsetDateTime.now())
                        .withData(data)
                        .build();
        LOG.debug("Register for entry: {}, payload: {}", entry, payload);
        final var objectMapper = SerializationUtil.getObjectMapper();

        try {
            final var payloadStr = objectMapper.writeValueAsString(payload);

            final var syncReg = Context.getThreadContext().getSynchronizationRegistry();
            syncReg.registerInterposedSynchronization(new Synchronization()
            {

                @Override
                public void beforeCompletion()
                {
                    // not used here
                }

                @Override
                public void afterCompletion(int status)
                {
                    if (jakarta.transaction.Status.STATUS_COMMITTED == status) {
                        send(eventType, entry, payloadStr);
                    } else {
                        LOG.warn("After completion with status: {}", status);
                    }
                }
            });
        } catch (final JsonProcessingException e) {
            LOG.error("Catched error on sending webhook", e);
        }

    }

    public static class Entry
    {

        String name;
        String signKeys;
        String url;

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    @Override
    public int getWeight()
    {
        return 0;
    }

    @Override
    public void onReloadSystemConfig(Parameter parameter)
        throws EFapsException
    {
        // Nothing to do here
    }

    @Override
    public void onReloadCache(Parameter parameter)
        throws EFapsException
    {
        WEBHOOKS = null;
    }
}
