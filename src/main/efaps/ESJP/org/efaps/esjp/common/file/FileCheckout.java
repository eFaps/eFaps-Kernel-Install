/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;

/**
 * Class is used as a trigger for field that perform a checkout
 * of an object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
@EFapsRevision("$Rev$")
public class FileCheckout
    implements EventExecution
{
    /**
     * {@inheritDoc}
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        File temp = null;
        try {
            final Checkout checkout = new Checkout(_parameter.getInstance());
            final InputStream input = checkout.execute();
            temp = new FileUtil().getFile(checkout.getFileName());
            final OutputStream out = new FileOutputStream(temp);
            IOUtils.copy(input, out);
            input.close();
            out.close();
        } catch (final IOException e) {
            throw new EFapsException(FileCheckout.class, "IOException", e);
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, temp);
        return ret;
    }
}
