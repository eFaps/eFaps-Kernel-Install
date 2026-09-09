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

import java.io.File;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.esjp.common.listener.ISignedUrl;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("a9e3cc6c-23fd-45d9-9d85-fef9b88f345c")
@EFapsApplication("eFaps-Kernel")
public class SignedUrl

{

    private static final Logger LOG = LoggerFactory.getLogger(SignedUrl.class);

    public Object onUpload(final File file,
                           final String reference)
        throws EFapsException
    {
        LOG.info("OnUpload for: {}, ref: {}", file, reference);
        Object ret = null;
        for (final var listener : Listener.get().<ISignedUrl>invoke(ISignedUrl.class)) {
            if (listener.applies(reference)) {
                ret = listener.onUpload(file, reference);
            }
        }
        return ret;
    }
}
