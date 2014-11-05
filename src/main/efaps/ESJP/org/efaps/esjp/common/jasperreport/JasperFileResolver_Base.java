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

package org.efaps.esjp.common.jasperreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jasperreports.engine.util.FileResolver;

import org.apache.commons.io.IOUtils;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is used to load files from the eFaps Database into an JasperReport.
 * There are two different Types of files that can be loaded:
 * <ul>
 * <li>JasperReports used as subreports</li>
 * <li>Images (JasperImages)</li>
 * </ul>
 * To now if a image or a subreport is wanted a naming convention is used.
 * <ul>
 * <li>Images must start with "JasperImage."</li>
 * <li>JasperReports must start with "JasperReport."</li>
 * </ul>
 * <b>Caution: The dot is expected!</b>
 *
 * @author The eFaps Team
 * @version $Id: JasperFileResolver_Base.java 8885 2013-02-20 00:32:07Z
 *          jan@moxter.net $
 */
@EFapsUUID("4733dd43-2ef3-4572-a1e9-c820567e9a36")
@EFapsRevision("$Rev$")
public abstract class JasperFileResolver_Base
    implements FileResolver
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JasperFileResolver.class);

    /**
     * Pattern for the filename in case of jasper.
     */
    private static Pattern reportPattern = Pattern.compile("([A-Z,a-z,_,0-9])+(?=\\.)");

    /**
     * @see net.sf.jasperreports.engine.util.FileResolver#resolveFile(java.lang.String)
     * @param _jasperFileName name of the jasper report
     * @return File
     */
    @Override
    public File resolveFile(final String _jasperFileName)
    {
        File file = null;
        try {
            QueryBuilder queryBldr = null;
            String name = null;
            if (_jasperFileName.startsWith("JasperImage.")) {
                name = _jasperFileName.replace("JasperImage.", "");
                queryBldr = new QueryBuilder(CIAdminProgram.JasperImage);
            } else if (_jasperFileName.startsWith("JasperReport.")) {
                name = _jasperFileName.replace("JasperReport.", "");
                queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
            } else if (_jasperFileName.endsWith("jasper") || _jasperFileName.endsWith("jrxml")) {
                final Matcher matcher = reportPattern.matcher(_jasperFileName);
                if (matcher.find()) {
                    name = matcher.group();
                    queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
                }
            }
            if (queryBldr != null) {
                queryBldr.addWhereAttrEqValue("Name", name);
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                if (query.next()) {
                    final Checkout checkout = new Checkout(query.getCurrentValue());
                    final InputStream input = checkout.execute();
                    file = new FileUtil().getFile(checkout.getFileName(), "jasper");
                    final OutputStream out = new FileOutputStream(file);
                    IOUtils.copy(input, out);
                    input.close();
                    out.close();
                }
            }
        } catch (final EFapsException e) {
            JasperFileResolver_Base.LOG.error("error in JasperFileResolver.resolveFile", e);
        } catch (final IOException e) {
            JasperFileResolver_Base.LOG.error("error in JasperFileResolver.resolveFile", e);
        }
        return file;
    }
}
