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
@EFapsUUID("92655a65-c84e-4c4d-b8d6-7fc485f0e0c9")
public interface IAttributeSetLog
{

    /**
     * Getter method for the instance variable {@link #classificationInstance}.
     *
     * @return value of instance variable {@link #classificationInstance}
     */
    AttributeSetInstObj getAttributSetInstObj();

    /**
     * Setter method for instance variable {@link #classificationInstance}.
     *
     * @param _attributSetInstObj the new attribut set inst obj
     */
    void setAttributSetInstObj(final AttributeSetInstObj _attributSetInstObj);

}
