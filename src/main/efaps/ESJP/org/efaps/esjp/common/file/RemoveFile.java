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
package org.efaps.esjp.common.file;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.store.Resource;
import org.efaps.db.store.Store;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;

@EFapsUUID("889d5892-6280-4798-a11b-6be31f7d11a8")
@EFapsApplication("eFaps-Kernel")
public class RemoveFile
    implements EventExecution
{

    @Override
    public Return execute(final Parameter parameter)
        throws EFapsException
    {
        final var instance = parameter.getInstance();
        if (InstanceUtils.isValid(instance)) {
            final Resource resource = Store.get(instance.getType().getStoreId())
                        .getResource(instance);
            if (resource.exists()) {
                resource.clean();
            }
        }
        return null;
    }

}
