/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.jasperreport;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("18031397-efce-4525-8f07-b39a7e1a86c9")
@EFapsApplication("eFaps-Kernel")
public class SubReportContainer
    extends SubReportContainer_Base
{
    /**
    * Needed for serialization.
    */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param _parameter        Parameters for an esjp
     * @param _dataSource       datasourclass if not the default must be used
     * @param _jrParameters map that contains the report parameters
     */
    public SubReportContainer(final Parameter _parameter,
                              final JRDataSource _dataSource,
                              final Map<String, Object> _jrParameters)
    {
        super(_parameter, _dataSource, _jrParameters);
    }
}
