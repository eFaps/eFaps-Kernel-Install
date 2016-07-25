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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.history;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("8f61fc90-229c-4cc6-a793-206202548155")
public interface IHistoryHtml
{

    /**
     * @return the value for the type column
     * @throws EFapsException on error
     */
    String getTypeColumnValue()
        throws EFapsException;

    /**
     * @return value for the description column
     * @throws EFapsException on error
     */
    StringBuilder getDescriptionColumnValue()
        throws EFapsException;
}
