package org.efaps.esjp.common.webhook;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

@JsonDeserialize(builder = BaseDataDto.Builder.class)
@EFapsUUID("0e0fc001-18e5-4872-9aa5-033caf4391e8")
@EFapsApplication("eFaps-Kernel")
public class BaseDataDto
{

    private final String oid;

    @Generated("SparkTools")
    private BaseDataDto(Builder builder)
    {
        this.oid = builder.oid;
    }

    public String getOid()
    {
        return oid;
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

        private String oid;

        private Builder()
        {
        }

        public Builder withOid(String oid)
        {
            this.oid = oid;
            return this;
        }

        public BaseDataDto build()
        {
            return new BaseDataDto(this);
        }
    }
}
