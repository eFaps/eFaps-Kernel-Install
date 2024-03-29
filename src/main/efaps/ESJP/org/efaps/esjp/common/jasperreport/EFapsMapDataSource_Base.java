/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.common.jasperreport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;


/**
 * DataSource using a simple List of maps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("02c64d22-161b-4e99-9614-c34a23fbea77")
@EFapsApplication("eFaps-Kernel")
public abstract class EFapsMapDataSource_Base
    extends AbstractCommon
    implements IeFapsDataSource
{
    /**
     * List of map with values.
     */
    private final List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

    /**
     * Iterator used for the values.
     */
    private Iterator<Map<String, Object>> iter;

    /**
     * Current map with objects.
     */
    private Map<String, Object> current;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final JasperReport _jasperReport,
                     final Parameter _parameter,
                     final JRDataSource _parentSource,
                     final Map<String, Object> _jrParameters)
        throws EFapsException
    {

    }

    /**
     * Getter method for the instance variable {@link #iter}.
     *
     * @return value of instance variable {@link #iter}
     */
    public Iterator<Map<String, Object>> getIter()
    {
        return this.iter;
    }

    /**
     * Setter method for instance variable {@link #iter}.
     *
     * @param _iter value for instance variable {@link #iter}
     */

    public void setIter(final Iterator<Map<String, Object>> _iter)
    {
        this.iter = _iter;
    }

    /**
     * Getter method for the instance variable {@link #current}.
     *
     * @return value of instance variable {@link #current}
     */
    public Map<String, Object> getCurrent()
    {
        return this.current;
    }

    /**
     * Setter method for instance variable {@link #current}.
     *
     * @param _current value for instance variable {@link #current}
     */

    public void setCurrent(final Map<String, Object> _current)
    {
        this.current = _current;
    }

    /**
     * Getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     */
    public List<Map<String, Object>> getValues()
    {
        return this.values;
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     * @return true if a next value exist, else false
     * @throws JRException on error
     */
    @Override
    public boolean next()
        throws JRException
    {
        if (this.iter == null) {
            this.iter = this.values.iterator();
        }
        final boolean ret = this.iter.hasNext();
        if (ret) {
            this.current = this.iter.next();
        }
        return ret;
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     * @param _field JRField
     * @return value for the given field
     * @throws JRException on error
     */
    @Override
    public Object getFieldValue(final JRField _field)
        throws JRException
    {
        Object ret = null;
        final String key = _field.getPropertiesMap().getProperty("Key");
        if (key != null) {
            ret = this.current.get(key);
        }
        return ret;
    }
}
