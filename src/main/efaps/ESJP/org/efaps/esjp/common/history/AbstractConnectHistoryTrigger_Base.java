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

package org.efaps.esjp.common.history;

import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.history.xml.AbstractConnectLog;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.AttributeValue;
import org.efaps.esjp.common.history.xml.ConnectInstObj;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractConnectHistoryTrigger_Base.java 9780 2013-07-08
 *          17:04:43Z jan@moxter.net $
 */
@EFapsUUID("28b038f6-3577-47d8-b611-b7c6552f218e")
@EFapsApplication("eFaps-Kernel")
public abstract class AbstractConnectHistoryTrigger_Base
    extends AbstractHistoryTrigger
{

    /**
     * Select Statement to the connected instance.
     */
    public static final String CONTYPEKEY = "Select4ConnectInstance";

    /**
     * Select Statement to the connected instance.
     */
    public static final String ATTRSEL = "Select4ConnectAttribute";

    /**
     * Select Statement to the connected instance.
     */
    public static final String HISEL = "Select4HistoryInstance";

    private Instance historyInstance;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void add2LogObject(final Parameter _parameter,
                                 final AbstractHistoryLog _log)
        throws EFapsException
    {
        final AbstractConnectLog log = (AbstractConnectLog) _log;

        final String selConTypeInst = getProperty(_parameter, AbstractConnectHistoryTrigger_Base.CONTYPEKEY);
        final Map<Integer, String> selects = analyseProperty(_parameter, AbstractConnectHistoryTrigger_Base.ATTRSEL);

        final PrintQuery print = new PrintQuery(_parameter.getInstance());
        print.addSelect(selConTypeInst);
        for (final String select : selects.values()) {
            print.addSelect(select);
        }
        print.executeWithoutAccessCheck();

        final Instance conInst = print.<Instance>getSelect(selConTypeInst);
        final ConnectInstObj conInstObj = new ConnectInstObj();
        conInstObj.setTypeUUID(conInst.getType().getUUID());
        conInstObj.setOid(conInst.getOid());
        log.setConnectInstance(conInstObj);

        for (final String select : selects.values()) {
            final Object value = print.getSelect(select);
            final Attribute attr = print.getAttribute4Select(select);
            final AttributeValue attrVal = new AttributeValue();
            attrVal.setValue(value);
            attrVal.setName(attr.getName());
            conInstObj.getAttributes().add(attrVal);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Instance getHistoryInstance(final Parameter _parameter)
        throws EFapsException
    {
        if (this.historyInstance == null) {
            final String selHistoryInst = getProperty(_parameter, AbstractConnectHistoryTrigger_Base.HISEL);
            final PrintQuery print = new PrintQuery(_parameter.getInstance());
            print.addSelect(selHistoryInst);
            print.executeWithoutAccessCheck();
            this.historyInstance = print.<Instance>getSelect(selHistoryInst);
        }
        return this.historyInstance;
    }
}
