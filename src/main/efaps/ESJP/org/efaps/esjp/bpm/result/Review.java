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


package org.efaps.esjp.bpm.result;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Review
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Status of the Review.
     */
    private boolean approved;

    /**
     * Getter method for the instance variable {@link #approved}.
     *
     * @return value of instance variable {@link #approved}
     */
    public boolean isApproved()
    {
        return this.approved;
    }


    /**
     * Setter method for instance variable {@link #approved}.
     *
     * @param approved value for instance variable {@link #approved}
     */
    public void setApproved(final boolean _approved)
    {
        this.approved = _approved;
    }


    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
