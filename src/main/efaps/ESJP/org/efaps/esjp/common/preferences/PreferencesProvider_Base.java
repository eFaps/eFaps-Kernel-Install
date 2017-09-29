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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IPreferencesProvider;
import org.efaps.db.Context;
import org.efaps.esjp.admin.common.systemconfiguration.KernelConfigurations;
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
     * The Enum SwitchPreference.
     */
    public enum SwitchPreference
    {
        /** The slidein. */
        SLIDEIN("org.efaps.webapp.ActivateSlideInMenu", "true", "false"),

        /** The TDT 4 content. */
        TDT4Content("org.efaps.webapp.TableDefaultType4Content", "Table", "GridX"),

        /** The TDT 4 content. */
        TDT4Tree("org.efaps.webapp.TableDefaultType4Tree", "Table", "GridX"),

        /** The TDT 4 content. */
        TDT4Search("org.efaps.webapp.TableDefaultType4Search", "Table", "GridX"),

        /** The TDT 4 content. */
        TDT4Form("org.efaps.webapp.TableDefaultType4Form", "Table", "GridX");

        /** The key. */
        private final String key;


        /** The on. */
        private final String on;

        /** The off. */
        private final String off;

        /**
         * Instantiates a new switch preference.
         *
         * @param _key the key
         * @param _on the on
         * @param _off the off
         */
        SwitchPreference(final String _key,
                         final String _on,
                         final String _off)
        {
            this.key = _key;
            this.on = _on;
            this.off = _off;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey()
        {
            return this.key;
        }
    }

    @Override
    public Map<String, String> getPreferences()
        throws EFapsException
    {
        final Map<String, String> ret = new HashMap<>();
        final List<String> prefs = KernelConfigurations.PREFERENCES.get();
        for (final SwitchPreference pref : SwitchPreference.values()) {
            if (prefs.contains(pref.key)) {
                String value = Context.getThreadContext().getUserAttribute(pref.key);
                if (value == null) {
                    value = SystemConfiguration.get(UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"))
                                    .getAttributeValue(pref.key);
                }
                ret.put(pref.key, value != null && value.equals(pref.on) ? "on" : "off");
            }
        }
        return ret;
    }

    @Override
    public void updatePreferences(final Map<String, String> _preferences)
        throws EFapsException
    {
        for (final SwitchPreference pref : SwitchPreference.values()) {
            if (_preferences.containsKey(pref.key)) {
                final String value = _preferences.get(pref.key);
                if ("off".equals(value)) {
                    Context.getThreadContext().setUserAttribute(pref.key, pref.off);
                } else if ("on".equals(value)) {
                    Context.getThreadContext().setUserAttribute(pref.key, pref.on);
                } else {
                    Context.getThreadContext().removeRequestAttribute(pref.key);
                }
            }
        }
    }
}
