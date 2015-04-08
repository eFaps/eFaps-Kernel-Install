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
 * Revision:        $Rev: 10208 $
 * Last Changed:    $Date: 2013-09-16 16:16:22 -0500 (lun, 16 sep 2013) $
 * Last Changed By: $Author: jan@moxter.net $
 */

package org.efaps.esjp.common.quartz;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.quartz.JobExecutionContext;

/**
 * Interface an esjp must implement to be executed as part of an EventDefinition.
 *
 * @author The eFaps Team
 * @version $Id: Project_Base.java 11050 2013-11-20 22:25:09Z
 *          jorge.cueva@moxter.net $
 */
@EFapsUUID("e55a454e-494b-4ef1-95ab-7bdf7a73b390")
@EFapsApplication("eFaps-Kernel")
public interface IEventDefinition
{
    /**
     * Execute the related esjp.
     * @param _definitionInstance Instance of the definition (must not be null)
     * @param _jobExec JobExecutionContext as passed from a Quartztrigger (can be null)
     * @throws EFapsException on error
     */
    void execute(final Instance _definitionInstance,
                 final JobExecutionContext _jobExec)
        throws EFapsException;
}
