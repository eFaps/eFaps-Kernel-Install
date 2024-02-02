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

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.update.schema.program.esjp.ESJPCompiler;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler;
import org.efaps.update.schema.program.staticsource.CSSCompiler;
import org.efaps.update.schema.program.staticsource.JavaScriptCompiler;
import org.efaps.update.schema.program.staticsource.WikiCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Compile.
 *
 * @author The eFaps Team
 */
@EFapsUUID("db8f9db6-322c-48e7-993c-8df339780dad")
@EFapsApplication("eFaps-Kernel")
public class Compile
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Compile.class);

    /**
     * Execute.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     * @throws InstallationException the installation exception
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException, InstallationException
    {
        final String[] types = _parameter.getParameterValues("type");
        LOG.info("===Starting Compiler via UI===");
        if (ArrayUtils.contains(types, "java")) {
            LOG.info("==Compiling Java==");
            new ESJPCompiler(org.efaps.rest.Compile.getClassPathElements()).compile(null, false);
        }
        if (ArrayUtils.contains(types, "css")) {
            LOG.info("==Compiling CSS==");
            new CSSCompiler().compile();
        }
        if (ArrayUtils.contains(types, "js")) {
            LOG.info("==Compiling Javascript==");
            new JavaScriptCompiler().compile();
        }
        if (ArrayUtils.contains(types, "wiki")) {
            LOG.info("==Compiling Wiki==");
            new WikiCompiler().compile();
        }
        if (ArrayUtils.contains(types, "jasper")) {
            LOG.info("==Compiling JasperReports==");
            new JasperReportCompiler(org.efaps.rest.Compile.getClassPathElements()).compile();
        }
        LOG.info("===Ending Compiler via UI===");
        return new Return();
    }
}
