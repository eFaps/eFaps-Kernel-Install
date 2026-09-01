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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

@EFapsUUID("7710c8cf-2ee2-4cdf-947e-491627bc6775")
@EFapsApplication("eFaps-Kernel")
@JsonDeserialize(builder = PingDto.Builder.class)
public class PingDto
{

    private final String msg;

    @Generated("SparkTools")
    private PingDto(Builder builder)
    {
        this.msg = builder.msg;
    }

    public String getMsg()
    {
        return msg;
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

        private String msg;

        private Builder()
        {
        }

        public Builder withMsg(String msg)
        {
            this.msg = msg;
            return this;
        }

        public PingDto build()
        {
            return new PingDto(this);
        }
    }
}
