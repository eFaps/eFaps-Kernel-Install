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
package org.efaps.esjp.common.loginalert;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.api.ui.ILoginAlertProvider;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AlertProvider_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("13b359fc-f4b1-4f44-9263-d35f53319d94")
@EFapsApplication("eFaps-Kernel")
public abstract class AlertProvider_Base
    implements ILoginAlertProvider
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AlertProvider.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The login alerts. */
    private List<ILoginAlert> loginAlerts;

    /**
     * Gets the login alerts.
     *
     * @return the login alerts
     */
    public List<ILoginAlert> getLoginAlerts()
    {
        if (CollectionUtils.isEmpty(this.loginAlerts)) {
            try {
                this.loginAlerts = Listener.get().<ILoginAlert>invoke(ILoginAlert.class);
            } catch (final EFapsException e) {
                LOG.error("Catched error", e);
            }
        }
        return this.loginAlerts;
    }

    @Override
    public IEsjpSnipplet getEsjpSnipplet(final String _key)
    {
        return new AlertSnipplet(getLoginAlerts());
    }

    @Override
    public void onClose()
    {
        for (final ILoginAlert listener : this.getLoginAlerts()) {
            listener.onClose();
        }
    }
}
