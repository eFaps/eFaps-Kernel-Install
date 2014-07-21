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

package org.efaps.esjp.common.jasperreport;

import java.util.Collection;
import java.util.Iterator;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("233813a6-fa36-4c0c-a591-fd4e3ec774de")
@EFapsRevision("$Rev$")
public abstract class AbstractBeanCollectionDataSource_Base
    extends JRAbstractBeanDataSource
    implements IeFapsDataSource
{

    /**
     * Data.
     */
    private Collection<?> data;

    /**
     * Iterator.
     */
    private Iterator<?> iterator;

    /**
     * CurrentBean.
     */
    private Object currentBean;

    /**
     * @param _isUseFieldDescription
     */
    public AbstractBeanCollectionDataSource_Base()
    {
        super(false);
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     * @return true if there is a next record, false otherwise
     * @throws JRException if any error occurs while trying to move to the next element
     */
    @Override
    public boolean next()
        throws JRException
    {
        boolean hasNext = false;

        if (this.iterator != null) {
            hasNext = this.iterator.hasNext();
            if (hasNext) {
                this.currentBean = this.iterator.next();
            }
        }
        return hasNext;
    }

    /**
     * Gets the field value for the current position.
     * @param _field Field the value will be retrieved for
     * @return an object containing the field value. The object type must be the field object type.
     * @throws JRException if any error occurs while trying to move to the next element
     */
    @Override
    public Object getFieldValue(final JRField _field)
        throws JRException
    {
        return getFieldValue(this.currentBean, _field);
    }

    /**
    *
    */
    @Override
    public void moveFirst()
    {
        if (this.data != null) {
            this.iterator = this.data.iterator();
        }
    }

    /**
     * Returns the underlying bean collection used by this data source.
     *
     * @return the underlying bean collection
     */
    public Collection<?> getData()
    {
        return this.data;
    }

    /**
     * Setter method for instance variable {@link #data}.
     *
     * @param _data value for instance variable {@link #data}
     */
    public void setData(final Collection<?> _data)
    {
        this.data = _data;
        if (this.data != null) {
            this.iterator = this.data.iterator();
        }
    }
}
