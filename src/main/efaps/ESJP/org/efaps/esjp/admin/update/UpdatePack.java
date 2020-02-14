/*
 * Copyright 2003 - 2020 The eFaps Team
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

package org.efaps.esjp.admin.update;

import java.io.IOException;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 */
@EFapsUUID("ae1aac5a-5c81-44f3-9e8d-ca66f71fab1f")
@EFapsApplication("eFaps-Kernel")
public class UpdatePack
    extends AbstractUpdate
{

    /**
     * Check revisions.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     * @throws InstallationException on error
     * @throws IOException
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException, InstallationException, IOException
    {
        final Context context = Context.getThreadContext();
        final Context.FileParameter fileItem = context.getFileParameters().get("pack");
        final var files = getFiles(fileItem.getName(), fileItem.getInputStream());
        update(files);
        return new Return();
    }
}
