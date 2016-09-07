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

import java.io.Serializable;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("4f9623ac-8e92-4ed1-8b64-7c26ad0f2dce")
@EFapsApplication("eFaps-Kernel")
public interface ILoginAlert
    extends IEsjpListener, Serializable
{

    /**
     * Adds to the alert html.
     *
     * @param _html the html
     * @throws EFapsException on error
     */
    void add2Alert(final StringBuilder _html)
        throws EFapsException;

    /**
     * On close.
     */
    void onClose();
}
