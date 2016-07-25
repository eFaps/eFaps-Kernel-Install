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
 */

package org.efaps.esjp.common.history.xml;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsApplication("eFaps-Kernel")
@EFapsUUID("af4764d0-36e5-4ae5-99d4-96f63af069ac")
public interface IClassificationLog
{

    /**
     * Getter method for the instance variable {@link #classificationInstance}.
     *
     * @return value of instance variable {@link #classificationInstance}
     */
    ClassInstObj getClassInstObj();

    /**
     * Setter method for instance variable {@link #classificationInstance}.
     *
     * @param _classificationInstance value for instance variable
     *            {@link #classificationInstance}
     */
    void setClassInstObj(final ClassInstObj _classificationInstance);

}
