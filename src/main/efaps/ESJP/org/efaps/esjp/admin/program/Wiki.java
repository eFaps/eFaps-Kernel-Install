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

package org.efaps.esjp.admin.program;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.util.EFapsException;
import org.efaps.wikiutil.export.html.WEMHtml;
import org.efaps.wikiutil.parser.gwiki.GWikiParser;
import org.efaps.wikiutil.parser.gwiki.javacc.ParseException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3bff1883-e01d-4843-be12-d72bb8e920c7")
@EFapsApplication("eFaps-Kernel")
public class Wiki
{

    /**
     * Used to update the wiki including compilation.
     * @param _parameter Paarmeter as passed from the eFaps API
     * @return  new Return
     * @throws EFapsException on error
     */
    public Return updateWiki(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        final PrintQuery print = new PrintQuery(instance);
        print.addAttribute("FileName");
        print.addSelect("linkfrom[Admin_Program_WikiCompiled#ProgramLink].oid");
        print.addSelect("linkfrom[Admin_Program_WikiCompiled#ProgramLink].attribute[FileName]");
        print.execute();

        final String wiki = _parameter.getParameterValue("wiki");

        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(wiki.getBytes("UTF8"));
            final Checkin checkin = new Checkin(instance);
            checkin.execute(print.<String> getAttribute("FileName"), in, in.available());

            final WEMHtml wemhtml = new WEMHtml();
            GWikiParser.parse(wemhtml, new ByteArrayInputStream(wiki.getBytes("UTF8")), "UTF-8");
            final ByteArrayInputStream compiled = new ByteArrayInputStream(wemhtml.getHtml().getBytes("UTF8"));
            final String compiledOid = print.<String>getSelect("linkfrom[Admin_Program_WikiCompiled#ProgramLink].oid");
            final String compiledFileName =
                    print.<String>getSelect("linkfrom[Admin_Program_WikiCompiled#ProgramLink].attribute[FileName]");

            final Checkin compiledCheckin = new Checkin(compiledOid);
            compiledCheckin.execute(compiledFileName, compiled, compiled.available());

        } catch (final UnsupportedEncodingException e) {
            // TODO  add to DBProperties
            throw new EFapsException(Wiki.class, "updateWiki.UnsupportedEncodingException", e);
        } catch (final ParseException e) {
            // TODO add to DBProperties
            throw new EFapsException(Wiki.class, "updateWiki.ParseException", e);
        }
        return new Return();
    }

    /**
     * Used to set the value for the wiki in the edit form.
     * @param _parameter Parameter as passed from the eFaps API
     * @return  value for the wiki
     * @throws EFapsException on error
     */
    public Return getWikiFieldValue(final Parameter _parameter)
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
            throw new EFapsException(Wiki.class, "getWikiFieldValue.IOException", e);
        } finally {
            try {
                ins.close();
            } catch (final IOException e) {
                throw new EFapsException(Wiki.class, "getWikiFieldValue.IOException", e);
            }
        }
        ret.put(ReturnValues.VALUES, strb.toString());
        return ret;
    }
}
