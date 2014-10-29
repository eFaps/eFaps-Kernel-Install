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


package org.efaps.esjp.common.eql;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.eql.IEsjpSelect;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("15ac15cc-f6ef-4768-82b0-1f37bd26b902")
@EFapsRevision("$Rev$")
public abstract class AbstractSelect_Base
    implements IEsjpSelect
{

    /**
     * Key of this select.
     */
    private String key;

    /**
     * Mapping for the values.
     */
    private final Map<Instance, Object> values = new HashMap<>();

    /**
     * @param _key key for this IEsjpSelect
     */
    @Override
    public void setKey(final String _key)
    {
        this.key = _key;
    }

    /**
     * @return the key for this IEsjpSelect
     */
    @Override
    public String getKey()
    {
        return this.key;
    }

    /**
     * @param _instance Instance of the current object
     * @return the value for the given instance
     */
    @Override
    public Object getValue(final Instance _instance)
    {
        return this.values.get(_instance);
    }

    /**
     * Getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     */
    public Map<Instance, Object> getValues()
    {
        return this.values;
    }
}
