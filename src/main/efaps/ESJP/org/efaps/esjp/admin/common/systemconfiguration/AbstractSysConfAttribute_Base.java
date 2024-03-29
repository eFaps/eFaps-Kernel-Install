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
package org.efaps.esjp.admin.common.systemconfiguration;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * The Class AbstractSysConfAttribute_Base.
 *
 * @author The eFaps Team
 * @param <T> type class returned by chaining
 * @param <V> value type returned
 */
@EFapsUUID("cb109497-8f43-4c14-a5d4-07c57a266d0b")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractSysConfAttribute_Base<T extends AbstractSysConfAttribute<T, V>, V>
    implements ISysConfAttribute
{

    /** The sys conf uuid. */
    private UUID sysConfUUID;

    /** The description. */
    private String description;

    /** The key. */
    private String key;

    /** The default value. */
    private V defaultValue;

    /**
     * Gets the this.
     *
     * @return the this
     */
    protected abstract T getThis();

    /**
     * Gets the this.
     *
     * @return the this
     * @throws EFapsException on error
     */
    public abstract V get()
        throws EFapsException;

    /**
     * Gets the SystemConfiguration UUID.
     *
     * @return the SystemConfiguration UUID
     */
    protected UUID getSysConfUUID()
    {
        return this.sysConfUUID;
    }

    /**
     * SystemConfiguration UUID.
     *
     * @param _sysConfUUID the SystemConfiguration UUID.
     * @return the t
     */
    public T sysConfUUID(final UUID _sysConfUUID)
    {
        this.sysConfUUID = _sysConfUUID;
        return getThis();
    }

    /**
     * Description.
     *
     * @param _description the _description
     * @return the t
     */
    public T description(final String _description)
    {
        this.description = _description;
        return getThis();
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public V getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param _defaultValue the new default value
     * @return the t
     */
    public T defaultValue(final V _defaultValue)
    {
        this.defaultValue = _defaultValue;
        return getThis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return this.key;
    }

    /**
     * Key.
     *
     * @param _key the _key
     * @return the t
     */
    public T key(final String _key)
    {
        this.key = _key;
        return getThis();
    }

    /**
     * Check if the Attribute is set explecitely.
     *
     * @return true, if successful
     * @throws EFapsException on error
     */
    public boolean exists()
        throws EFapsException
    {
        return SystemConfiguration.get(getSysConfUUID()).containsAttributeValue(getKey());
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append(getKey()).append(" - ").append(this.getClass().getSimpleName()).toString();
    }
}
