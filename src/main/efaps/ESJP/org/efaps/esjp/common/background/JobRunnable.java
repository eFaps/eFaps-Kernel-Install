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

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJob;

/**
 * This class must be replaced for customization, therefore it is left mainly
 * empty.
 * Functional description can be found in the related "<code>_Base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("0394760c-bfbb-45ea-a894-39fe1fa9efd9")
@EFapsApplication("eFaps-Kernel")
public class JobRunnable
    extends JobRunnable_Base
{
    /**
     * Instantiates a new job runnable.
     *
     * @param _job the job
     * @param _bridge the bridge
     */
    public JobRunnable(final IJob _job,
                       final IExecutionBridge _bridge)
    {
        super(_job, _bridge);
    }
}
