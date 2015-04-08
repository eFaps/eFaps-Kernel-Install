/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;

/**
 * Class is used as a trigger for field that perform a checkout
 * of an object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
@EFapsApplication("eFaps-Kernel")
public class FileCheckout
    extends AbstractCommon
    implements EventExecution
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
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

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return return combing
     * @throws EFapsException on error
     */
    public Return multiple(final Parameter _parameter)
        throws EFapsException
    {
        File retFile = null;
        try {
            final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
            if (oids != null) {
                final List<Instance> instances = new ArrayList<>();
                for (final String oid : oids) {
                    final Instance instance = Instance.get(oid);
                    if (instance.isValid()) {
                        instances.add(instance);
                    }
                }
                final List<File> files = new ArrayList<>();
                for (final Instance instance : instances) {
                    final Checkout checkout = new Checkout(instance);
                    final InputStream input = checkout.execute();
                    final File temp = new FileUtil().getFile(checkout.getFileName());
                    final OutputStream out = new FileOutputStream(temp);
                    IOUtils.copy(input, out);
                    input.close();
                    out.close();
                    files.add(temp);
                }
                retFile = new FileUtil().combinePdfs(files, getProperty(_parameter, "FileName", "CheckOut"), false);
            }
        } catch (final IOException e) {
            throw new EFapsException(FileCheckout.class, "IOException", e);
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, retFile);
        return ret;
    }
}


