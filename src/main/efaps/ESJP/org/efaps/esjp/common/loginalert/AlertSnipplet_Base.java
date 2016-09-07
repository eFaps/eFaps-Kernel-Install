/*
 * Copyright 2003 - 2016 The eFaps Team
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
package org.efaps.esjp.common.loginalert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.util.EFapsBaseException;

/**
 * The Class AlertSnipplet_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("bc5ec035-3d42-4f58-a141-413ba9df82cb")
@EFapsApplication("eFaps-Kernel")
public abstract class AlertSnipplet_Base
    implements IEsjpSnipplet
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The init. */
    private boolean init;

    /** The html snipplet. */
    private CharSequence htmlSnipplet;

    /** The alerts. */
    private final List<ILoginAlert> alerts;

    /**
     * Instantiates a new alert snipplet.
     *
     * @param _alerts the alerts
     */
    public AlertSnipplet_Base(final List<ILoginAlert> _alerts)
    {
        this.alerts = _alerts;
    }

    @Override
    public CharSequence getHtmlSnipplet()
        throws EFapsBaseException
    {
        if (!this.init) {
            this.init = true;
            final StringBuilder bldr = new StringBuilder();
            for (final ILoginAlert listener : this.alerts) {
                listener.add2Alert(bldr);
            }
            this.htmlSnipplet = bldr;
        }
        return this.htmlSnipplet;
    }

    @Override
    public boolean isVisible()
        throws EFapsBaseException
    {
        return StringUtils.isNotEmpty(getHtmlSnipplet());
    }

    @Override
    public String getIdentifier()
        throws EFapsBaseException
    {
        return AlertSnipplet.class.getName();
    }
}
