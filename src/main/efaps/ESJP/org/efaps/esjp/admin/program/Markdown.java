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

package org.efaps.esjp.admin.program;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.util.EFapsException;

/**
 * The Class Markdown.
 *
 * @author The eFaps Team
 */
@EFapsUUID("5ecd72e2-f654-4c7e-9cf3-6f391fc22a1e")
@EFapsApplication("eFaps-Kernel")
public class Markdown
{

    /**
     * Gets the markdown field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the markdown field value
     * @throws EFapsException on error
     */
    public Return getMarkdownFieldValue(final Parameter _parameter)
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
            throw new EFapsException(CSS.class, "getMarkdownFieldValue.IOException", e);
        } finally {
            try {
                ins.close();
            } catch (final IOException e) {
                throw new EFapsException(Markdown.class, "getMarkdownFieldValue.IOException", e);
            }
        }
        ret.put(ReturnValues.VALUES, strb.toString());
        return ret;
    }

    /**
     * Gets the markdown field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the markdown field value
     * @throws EFapsException on error
     */
    public Return getHtmlFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getCallInstance();

        ret.put(ReturnValues.VALUES, renderHtml(instance).toString());
        return ret;
    }

    /**
     * Used to update the wiki including compilation.
     *
     * @param _parameter Paarmeter as passed from the eFaps API
     * @return new Return
     * @throws EFapsException on error
     */
    public Return updateMarkdown(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        final PrintQuery print = new PrintQuery(instance);
        print.addAttribute(CIAdminProgram.Abstract.Name);
        print.execute();

        final String markdown = _parameter.getParameterValue("markdown");
        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(markdown.getBytes("UTF8"));
            final Checkin checkin = new Checkin(instance);
            checkin.execute(print.<String>getAttribute(CIAdminProgram.Abstract.Name), in, in.available());
        } catch (final UnsupportedEncodingException e) {
            throw new EFapsException(Markdown.class, "updateMarkdown.UnsupportedEncodingException", e);
        }
        return new Return();
    }

    /**
     * Render html.
     *
     * @param _instance the instance
     * @return the char sequence
     * @throws EFapsException on error
     */
    public CharSequence renderHtml(final Instance _instance)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        try {
            final List<Extension> extensions = Arrays.asList(TablesExtension.create());
            final Parser parser = Parser.builder().extensions(extensions).build();

            final Checkout checkout = new Checkout(_instance);
            final InputStream ins = checkout.execute();
            final StringBuilder strb = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strb.append(line).append("\n");
            }
            final Node document = parser.parse(strb.toString());
            final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
            ret.append(renderer.render(document));
        } catch (final IOException e) {
            throw new EFapsException(Markdown.class, "updateMarkdown.IOException", e);
        }
        return ret;
    }
}
