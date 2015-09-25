/*
 * Copyright 2003 - 2015 The eFaps Team
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
package org.efaps.esjp.common.dashboard;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Properties;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * The Class AbstractDashboardPanel_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("9a965d08-8d92-4718-a9d5-af2f93481daa")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractDashboardPanel_Base
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** The config. */
    private String config;

    /**
     * Instantiates a new abstract dashboard panel.
     */
    public AbstractDashboardPanel_Base()
    {
    }

    /**
     * Instantiates a new abstract dashboard panel_ base.
     *
     * @param _config the _config
     */
    public AbstractDashboardPanel_Base(final String _config)
    {
        this.config = _config;
    }

    /**
     * Gets the config.
     *
     * @return the config
     */
    public Properties getConfig()
    {
        final Properties ret = new Properties();
        if (this.config != null && !this.config.isEmpty()) {
            try {
                ret.load(new StringReader(this.config));
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void setConfig(final String _config)
    {
        this.config = _config;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    protected Integer getWidth()
    {
        return Integer.valueOf(getConfig().getProperty("Width", "600"));
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    protected Integer getHeight()
    {
        return Integer.valueOf(getConfig().getProperty("Height", "400"));
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    protected String getTitle()
    {
        return getConfig().getProperty("Title");
    }
}
