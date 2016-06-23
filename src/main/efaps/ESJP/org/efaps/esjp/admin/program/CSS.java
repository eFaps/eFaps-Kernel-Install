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
package org.efaps.esjp.admin.program;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FileUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.bundle.TempFileBundle;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.update.schema.program.staticsource.CSSCompiler;
import org.efaps.util.EFapsException;

/**
 * The Class CSS.
 *
 * @author The eFaps Team
 */
@EFapsUUID("686f64ac-f273-4efc-86b9-87d4b304ceb9")
@EFapsApplication("eFaps-Kernel")
public class CSS
{

    /**
     * Compile.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return compile(final Parameter _parameter)
        throws EFapsException
    {
        new CSSCompiler().compile();
        try {
            FileUtils.cleanDirectory(TempFileBundle.getTempFolder());
        } catch (final IOException e) {
            throw new EFapsException(CSS.class, "compile", e);
        }
        return new Return();
    }

    /**
     * Used to update the wiki including compilation.
     *
     * @param _parameter Paarmeter as passed from the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return updateCSS(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        final PrintQuery print = new PrintQuery(instance);
        print.addAttribute(CIAdminProgram.Abstract.Name);
        print.execute();

        final String css = _parameter.getParameterValue("css");
        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(css.getBytes("UTF8"));
            final Checkin checkin = new Checkin(instance);
            checkin.execute(print.<String>getAttribute(CIAdminProgram.Abstract.Name), in, in.available());
        } catch (final UnsupportedEncodingException e) {
            throw new EFapsException(CSS.class, "updateCSS.UnsupportedEncodingException", e);
        }
        return new Return();
    }

    /**
     * Used to set the value for the wiki in the edit form.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @return value for the wiki
     * @throws EFapsException on error
     */
    public Return getCSSFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getCallInstance();
        final Checkout checkout = new Checkout(instance);
        final InputStream ins = checkout.execute();
        final StringBuilder strb = new StringBuilder();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strb.append(line).append("\n");
            }
        } catch (final IOException e) {
            throw new EFapsException(CSS.class, "getCSSFieldValue.IOException", e);
        } finally {
            try {
                ins.close();
            } catch (final IOException e) {
                throw new EFapsException(CSS.class, "getCSSFieldValue.IOException", e);
            }
        }
        ret.put(ReturnValues.VALUES, strb.toString());
        return ret;
    }
}
