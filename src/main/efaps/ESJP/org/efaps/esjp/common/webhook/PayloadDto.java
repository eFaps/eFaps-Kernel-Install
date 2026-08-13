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

import java.time.OffsetDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

/**
 * https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md
 */
@EFapsUUID("72caf507-cdd2-4a16-8720-ef315e3eee68")
@EFapsApplication("eFaps-Kernel")
@JsonDeserialize(builder = PayloadDto.Builder.class)
public class PayloadDto
{

    private final String type;
    private final OffsetDateTime timestamp;
    private final Object data;

    @Generated("SparkTools")
    private PayloadDto(Builder builder)
    {
        this.type = builder.type;
        this.timestamp = builder.timestamp;
        this.data = builder.data;
    }

    public String getType()
    {
        return type;
    }

    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }

    public Object getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Generated("SparkTools")
    public static Builder builder()
    {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder
    {

        private String type;
        private OffsetDateTime timestamp;
        private Object data;

        private Builder()
        {
        }

        public Builder withType(String type)
        {
            this.type = type;
            return this;
        }

        public Builder withTimestamp(OffsetDateTime timestamp)
        {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withData(Object data)
        {
            this.data = data;
            return this;
        }

        public PayloadDto build()
        {
            return new PayloadDto(this);
        }
    }
}
