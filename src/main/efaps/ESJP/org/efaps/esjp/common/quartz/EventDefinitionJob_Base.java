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
 * Revision:        $Rev: 10208 $
 * Last Changed:    $Date: 2013-09-16 16:16:22 -0500 (lun, 16 sep 2013) $
 * Last Changed By: $Author: jan@moxter.net $
 */

package org.efaps.esjp.common.quartz;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.AttributeQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TODO Comment.
 *
 * @author The eFaps Team
 * @version $Id: EventDefinitionJob_Base.java 10208 2013-09-16 21:16:22Z jan@moxter.net $
 */
@EFapsUUID("9b423589-e341-45c0-b59f-098d6dd8006c")
@EFapsRevision("$Rev: 10208 $")
public abstract class EventDefinitionJob_Base
    implements Job
{
    @Override
    public void execute(final JobExecutionContext _jobExec)
        throws JobExecutionException
    {
        final String quartzDefName = _jobExec.getTrigger().getKey().getName();
        try {
            final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminCommon.QuartzTriggerAbstract);
            attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.QuartzTriggerAbstract.Name, quartzDefName);
            final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery(CIAdminProgram.Java.ID);

            // ERP_EventDefinitionAbstract
            final QueryBuilder attrQueryBldr2 = new QueryBuilder(Type
                            .get(UUID.fromString("847cf68b-c23a-4826-a154-dc38ff7f76fd")));
            attrQueryBldr2.addWhereAttrInQuery("QuartzDefinitionLink", attrQuery);
            final AttributeQuery attrQuery2 = attrQueryBldr2.getAttributeQuery("ID");
            attrQuery2.setCompanyDepended(false);

            // ERP_EventDefinition2JavaProgram
            final QueryBuilder queryBldr = new QueryBuilder(Type
                            .get(UUID.fromString("b8405fb3-2dd1-440d-88bd-a64491fc27df")));
            queryBldr.addWhereAttrInQuery("EventDefinitionLink", attrQuery2);
            final MultiPrintQuery multi = queryBldr.getPrint();
            final SelectBuilder selProgName = new SelectBuilder().linkto("JavaProgramLink")
                            .attribute(CIAdminProgram.Java.Name);
            final SelectBuilder selProperties = new SelectBuilder().linkto("EventDefinitionLink")
                            .attribute("Properties");
            multi.addSelect(selProgName, selProperties);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final String clazzName = multi.<String>getSelect(selProgName);
                final String properties = multi.<String>getSelect(selProperties);
                final Properties props = new Properties();
                props.load(new  StringReader(properties));
                final Class<?> clazz = Class.forName(clazzName);
                final Object ins = clazz.newInstance();
                if (ins instanceof IEventDefinition) {
                    ((IEventDefinition)ins).execute(props, _jobExec);
                }
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  catch (final ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
