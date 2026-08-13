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
package org.efaps.tests;

import org.efaps.esjp.common.webhook.Webhook;

public class WebhookTest
{
    public static void main(String[] args)
    {
        final var webhook = new Webhook();
        final var key = "YTZpYWffyNfbFajpgID0NAgskUcvhiB8";
        final var msgId = "msg_YC8Iy3rsyM2tVdfga6DBhcm1cO6n";
        final var payload = "{\"type\":\"ping\",\"timestamp\":\"2026-08-13T00:16:27.50724223Z\",\"data\":{\"msg\":\"Hello World\"}}";
        webhook.sign(key, msgId, 1786580187, payload);
    }
}
