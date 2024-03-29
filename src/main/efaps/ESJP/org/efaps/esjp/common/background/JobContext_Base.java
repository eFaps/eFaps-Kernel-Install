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
package org.efaps.esjp.common.background;

import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.background.IJobContext;

/**
 * The Class JobContext_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("7fb02ba5-6f9a-4eac-97e1-0424c0848a05")
@EFapsApplication("eFaps-Kernel")
public abstract class JobContext_Base
    implements IJobContext
{

    /** */
    private static final long serialVersionUID = 1L;

    /** The user name. */
    private String userName;

    /** The company uuid. */
    private UUID companyUUID;

    /** The locale. */
    private Locale locale;

    @Override
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Sets the user name.
     *
     * @param _userName the new user name
     * @return the job context
     */
    public JobContext setUserName(final String _userName)
    {
        this.userName = _userName;
        return (JobContext) this;
    }

    @Override
    public UUID getCompanyUUID()
    {
        return this.companyUUID;
    }

    /**
     * Sets the company uuid.
     *
     * @param _companyUUID the new company uuid
     * @return the job context
     */
    public JobContext setCompanyUUID(final UUID _companyUUID)
    {
        this.companyUUID = _companyUUID;
        return (JobContext) this;
    }

    @Override
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Sets the locale.
     *
     * @param _locale the new locale
     * @return the job context_ base
     */
    public JobContext setLocale(final Locale _locale)
    {
        this.locale = _locale;
        return (JobContext) this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
