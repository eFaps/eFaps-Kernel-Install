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

package org.efaps.esjp.admin;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsListener;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * The listener interface for receiving runLevel events.
 * The class that is interested in processing a runLevel
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addRunLevelListener</code> method. When
 * the runLevel event occurs, that object's appropriate
 * method is invoked.
 *
 * @see RunLevelEvent
 */
@EFapsUUID("66680d2f-ab29-4b13-a682-964cdfc406ee")
@EFapsApplication("eFaps-Kernel")
@EFapsListener
public class RunLevelListener
    extends RunLevelListener_Base
{
}
