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
package org.efaps.esjp.admin.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("88c3831c-c306-46e4-8797-fbd9def54221")
@EFapsApplication("eFaps-Kernel")
public class Extension
    extends AbstractUpdate
{

    private static final Logger LOG = LoggerFactory.getLogger(Extension.class);

    public Return execute(final Parameter _parameter)
        throws EFapsException, InstallationException
    {
        final String[] oids = _parameter.getParameterValues("selectedRow");
        for (final String oid : oids) {
            try {
                final var instance = Instance.get(oid);
                final var checkout = new Checkout(instance);
                final var inputStream = checkout.execute();
                final var files = getFiles(checkout.getFileName(), inputStream);
                final var installFiles = getInstallFiles(files);
                final List<InstallFile> installFileList = new ArrayList<>(installFiles.values());
                Collections.sort(installFileList, (_installFile0,
                                                   _installFile1) -> _installFile0.getName().compareTo(_installFile1
                                                                   .getName()));
                install(installFileList);
            } catch (IOException | URISyntaxException e) {
                LOG.error("Catched", e);
            }
        }
        return new Return();
    }
}
