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

package org.efaps.esjp.common.jasperreport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.Resource;
import net.sf.jasperreports.repo.StreamRepositoryService;

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
 */
@EFapsUUID("4733dd43-2ef3-4572-a1e9-c820567e9a36")
@EFapsApplication("eFaps-Kernel")
public abstract class JasperFileResolver_Base
    implements StreamRepositoryService
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JasperFileResolver.class);

    /**
     * Pattern for the filename in case of jasper.
     */
    private static Pattern reportPattern = Pattern.compile("([A-Z,a-z,_,0-9])+(?=\\.)");

    @Override
    public InputStream getInputStream(final String _uri)
    {
        InputStream input = null;
        QueryBuilder queryBldr = null;
        String name = null;
        if (_uri.startsWith("JasperImage.")) {
            name = _uri.replace("JasperImage.", "");
            queryBldr = new QueryBuilder(CIAdminProgram.JasperImage);
        } else if (_uri.startsWith("JasperReport.")) {
            name = _uri.replace("JasperReport.", "");
            queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
        } else if (_uri.endsWith("jasper") || _uri.endsWith("jrxml")) {
            final Matcher matcher = reportPattern.matcher(_uri);
            if (matcher.find()) {
                name = matcher.group();
                queryBldr = new QueryBuilder(CIAdminProgram.JasperReportCompiled);
            }
        }
        if (queryBldr != null) {
            try {
                queryBldr.addWhereAttrEqValue("Name", name);
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                if (query.next()) {
                    final Checkout checkout = new Checkout(query.getCurrentValue());
                    input = checkout.execute();
                }
            } catch (final EFapsException e) {
                JasperFileResolver_Base.LOG.error("error in JasperFileResolver.getInputStream", e);
            }
        }
        return input;
    }

    @Override
    public OutputStream getOutputStream(final String uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Resource getResource(final String uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveResource(final String uri, final Resource resource)
    {
        // TODO Auto-generated method stub
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K extends Resource> K getResource(final String _uri, final Class<K> _resourceType)
    {
        Resource ret = null;
        if (_resourceType.isAssignableFrom(InputStreamResource.class)) {
            final InputStream inputStream = getInputStream(_uri);
            if (inputStream != null) {
                ret = new InputStreamResource();
                ((InputStreamResource) ret).setInputStream(inputStream);
            }
        }
        return (K) ret;
    }
}
