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
package org.efaps.esjp.common.uisearch;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.esjp.common.uisearch.Connect_Base.ConnectType;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("40d509f9-f63e-4180-b523-8ede500e7479")
@EFapsApplication("eFaps-Kernel")
public interface IConnectListener
    extends IEsjpListener
{

    /**
     * Eval relationship.
     *
     * @param _parameter the _parameter
     * @param _connectType the _connect type
     * @param _idx the _idx
     * @throws EFapsException the e faps exception
     */
    void evalRelationship(final Parameter _parameter,
                          final ConnectType _connectType,
                          final int _idx)
        throws EFapsException;

}
