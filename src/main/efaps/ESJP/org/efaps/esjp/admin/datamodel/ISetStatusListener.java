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
package org.efaps.esjp.admin.datamodel;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("d7adc54d-5b6a-47be-bb53-8863b61ef5ee")
public interface ISetStatusListener
    extends IEsjpListener
{

    /**
     * After set status.
     *
     * @param _parameter the parameter
     * @param _instance the instance
     * @param _status the status
     * @throws EFapsException the e faps exception
     */
    void afterSetStatus(final Parameter _parameter,
                        final Instance _instance,
                        final Status _status)
        throws EFapsException;
}
