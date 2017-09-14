/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.esjp.common.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IPreferencesProvider;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The Class DashboardProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("125c5aab-47e0-4017-9f16-1414e929beb8")
@EFapsApplication("eFaps-Kernel")
public abstract class PreferencesProvider_Base
    implements IPreferencesProvider
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the preferences keys.
     *
     * @return the preferences keys
     */
    public Set<String> getPreferencesKeys()
    {
        final Set<String> ret = new HashSet<>();
        ret.add("org.efaps.webapp.ActivateSlideInMenu");
        return ret;
    }

    @Override
    public Map<String, String> getPreferences()
        throws EFapsException
    {
        final Map<String, String> ret = new HashMap<>();
        for (final String key : getPreferencesKeys()) {
            ret.put(key, Context.getThreadContext().getUserAttribute(key));
        }
        return ret;
    }

    @Override
    public void updatePreferences(final Map<String, String> _preferences)
        throws EFapsException
    {
        for (final String key : getPreferencesKeys()) {
            Context.getThreadContext().setUserAttribute(key, _preferences.get(key));
        }
    }
}
