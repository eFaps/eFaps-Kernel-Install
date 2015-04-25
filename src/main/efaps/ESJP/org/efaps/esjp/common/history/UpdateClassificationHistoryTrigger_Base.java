/*
 * Copyright 2003 - 2015 The eFaps Team
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

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.common.history.xml.AbstractHistoryLog;
import org.efaps.esjp.common.history.xml.ClassInstObj;
import org.efaps.esjp.common.history.xml.IClassificationLog;
import org.efaps.esjp.common.history.xml.UpdateClassificationLog;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3f9ce0bc-10d1-4c56-8231-6cdb8248ce2e")
@EFapsApplication("eFaps-Kernel")
public abstract class UpdateClassificationHistoryTrigger_Base
    extends AbstractUpdateHistoryTrigger
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void add2LogObject(final Parameter _parameter,
                                 final AbstractHistoryLog _log)
        throws EFapsException
    {
        super.add2LogObject(_parameter, _log);
        final ClassInstObj instObj = new ClassInstObj();
        instObj.setOid(_parameter.getInstance().getOid());
        instObj.setTypeUUID(_parameter.getInstance().getType().getUUID());
        ((IClassificationLog) _log).setClassInstObj(instObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getHistoryType()
    {
        return CICommon.HistoryClassificationUpdate.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractHistoryLog getLogObject(final Parameter _parameter)
    {
        return new UpdateClassificationLog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Instance getHistoryInstance(final Parameter _parameter)
        throws EFapsException
    {
        final Instance clazzInst = _parameter.getInstance();
        final String linkAttr = ((Classification) clazzInst.getType()).getLinkAttributeName();
        final PrintQuery print = new PrintQuery(clazzInst);
        final SelectBuilder selInst = SelectBuilder.get().linkto(linkAttr).instance();
        print.addSelect(selInst);
        print.executeWithoutAccessCheck();
        return print.getSelect(selInst);
    }
}
