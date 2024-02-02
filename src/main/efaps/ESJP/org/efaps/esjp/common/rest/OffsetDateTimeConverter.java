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
package org.efaps.esjp.common.rest;

import java.time.OffsetDateTime;

import javax.ws.rs.ext.ParamConverter;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

@EFapsUUID("faf05dc2-ce39-4cf9-9213-ea57c33959ce")
@EFapsApplication("eFaps-Kernel")
public class OffsetDateTimeConverter
    implements ParamConverter<OffsetDateTime>
{

    @Override
    public OffsetDateTime fromString(final String value)
    {
        if (value == null) {
            return null;
        }
        return OffsetDateTime.parse(value);
    }

    @Override
    public String toString(final OffsetDateTime value)
    {
        return value.toString();
    }
}
