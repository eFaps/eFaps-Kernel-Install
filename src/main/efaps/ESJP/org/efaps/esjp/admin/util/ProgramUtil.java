/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.esjp.admin.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminEvent;
import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("5cd048cd-1ee0-47cd-8c8e-1480d18b5dae")
@EFapsApplication("eFaps-Kernel")
public class ProgramUtil
    extends AbstractUtil
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProgramUtil.class);


    /**
     * Delete all events related to Attributes, Commands and Menus.
     * @param _parameter Parameter as passed by the eFasp API
     * @throws EFapsException on error
     */
    public void removeProgram(final Parameter _parameter)
        throws EFapsException
    {
        if (checkAccess()) {
            // Admin_Program_Java
            final String name = _parameter.getParameterValue("valueField");
            ProgramUtil.LOG.info("Searching for esjp: '{}'", name);
            final QueryBuilder queryBldr = new QueryBuilder(UUID.fromString("11043a35-f73c-481c-8c77-00306dbce824"));
            queryBldr.addWhereAttrEqValue("Name", name);
            final InstanceQuery query = queryBldr.getQuery();
            query.execute();
            if (query.next()) {
                final Instance prgInst = query.getCurrentValue();
                ProgramUtil.LOG.info("Found esjp: '{}'", name);

                // Admin_Program_JavaClass
             // Admin_Program_Javaclass
                final QueryBuilder classQueryBldr = new QueryBuilder(
                                UUID.fromString("9118e1e3-ed4c-425d-8578-8d1f1d385110"));
                classQueryBldr.addWhereAttrEqValue("ProgramLink", prgInst);
                final InstanceQuery classQuery = classQueryBldr.getQuery();
                classQuery.execute();
                while (classQuery.next()) {
                    ProgramUtil.LOG.info("Removing compiled Classes: '{}'", classQuery.getCurrentValue());
                    final Delete del = new Delete(classQuery.getCurrentValue());
                    del.execute();
                }

                final QueryBuilder classQueryBldr2 = new QueryBuilder(
                                UUID.fromString("12c9da04-8085-4d74-a2a8-e211d655e29a"));
                classQueryBldr2.addWhereAttrEqValue("ProgramLink", prgInst);
                final InstanceQuery classQuery2 = classQueryBldr2.getQuery();
                classQuery2.execute();
                while (classQuery2.next()) {
                    ProgramUtil.LOG.info("Removing compiled Classes: '{}'", classQuery2.getCurrentValue());
                    final Delete del = new Delete(classQuery2.getCurrentValue());
                    del.execute();
                }

                final QueryBuilder eventQueryBldr = new QueryBuilder(CIAdminEvent.Definition);
                eventQueryBldr.addWhereAttrEqValue("JavaProg", prgInst);
                final InstanceQuery eventQuery = eventQueryBldr.getQuery();
                eventQuery.execute();
                while (eventQuery.next()) {
                    ProgramUtil.LOG.info("Removing related EventDefintion: '{}'", eventQuery.getCurrentValue());
                    final Delete del = new Delete(eventQuery.getCurrentValue());
                    del.execute();
                }

                final Delete del = new Delete(prgInst);
                del.execute();
                ProgramUtil.LOG.info("Removed esjp sucessfully");
            }
        }
    }

    /**
     * Validate methods.
     *
     * @param _parameter the _parameter
     * @throws EFapsException the e faps exception
     */
    public void validateMethods(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, Boolean>> map = new HashMap<>();

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminEvent.Definition);
        final MultiPrintQuery multi = queryBldr.getPrint();
        final SelectBuilder selProgName = SelectBuilder.get().linkto(CIAdminEvent.Definition.JavaProg)
                        .attribute(CIAdminProgram.Java.Name);
        final SelectBuilder selAbstractName = SelectBuilder.get().linkto(CIAdminEvent.Definition.Abstract)
                        .attribute(CIAdminProgram.Java.Name);
        final SelectBuilder selAbstractInst = SelectBuilder.get().linkto(CIAdminEvent.Definition.Abstract).instance();
        multi.addSelect(selProgName, selAbstractName, selAbstractInst);
        multi.addAttribute(CIAdminEvent.Definition.Method);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            final String progName = multi.getSelect(selProgName);
            final String methodName = multi.getAttribute(CIAdminEvent.Definition.Method);

            Map<String, Boolean> methodMap;
            if (map.containsKey(progName)) {
                methodMap = map.get(progName);
            } else {
                methodMap = new HashMap<>();
                map.put(progName, methodMap);
            }
            boolean found = false;
            if (methodMap.containsKey(methodName)) {
                found = methodMap.get(methodName);
            } else {
                try {
                    final Class<?> clazz = Class.forName(progName);
                    final Method method = clazz.getMethod(methodName, Parameter.class);
                    if (method != null) {
                        found = true;
                        methodMap.put(methodName, true);
                    }
                } catch (final ClassNotFoundException e) {
                    LOG.debug("ESJP {} not found", progName);
                } catch (final NoSuchMethodException e) {
                    LOG.debug("Method {} in ESJP {} not found", methodName, progName);
                } catch (final SecurityException e) {
                    LOG.error("SecurityException", e);
                }
            }
            if (!found) {
                final Instance abstractInst = multi.getSelect(selAbstractInst);
                final String abstractName = multi.getSelect(selAbstractName);
                if (abstractInst.getType().isCIType(CIAdminUserInterface.Field)) {
                    final PrintQuery print = new PrintQuery(abstractInst);
                    final SelectBuilder selColName = SelectBuilder.get().linkto(CIAdminUserInterface.Field.Collection)
                                    .attribute(CIAdminProgram.Java.Name);
                    final SelectBuilder selColInst = SelectBuilder.get().linkto(CIAdminUserInterface.Field.Collection)
                                    .instance();
                    print.addSelect(selColName, selColInst);
                    print.executeWithoutAccessCheck();

                    final Instance colInst = print.getSelect(selColInst);
                    final String colName = print.getSelect(selColName);
                    LOG.warn("Could not find method '{}' in ESJP '{}' realted a {} '{}' in {} '{}'", methodName,
                                    progName, abstractInst.getType().getName(), abstractName,
                                    colInst.getType().getName(), colName);
                } else {
                    LOG.warn("Could not find method '{}' in ESJP '{}' realted a {} '{}'", methodName, progName,
                                    abstractInst.getType().getName(), abstractName);
                }
            }
        }
    }
}
