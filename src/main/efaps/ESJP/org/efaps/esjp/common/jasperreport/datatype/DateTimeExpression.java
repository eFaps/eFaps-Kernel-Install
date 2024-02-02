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
package org.efaps.esjp.common.jasperreport.datatype;

import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.definition.ReportParameters;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.joda.time.DateTime;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b8a6ae9a-31c1-4cbd-ada4-c74f903ea23f")
@EFapsApplication("eFaps-Kernel")
public class DateTimeExpression
    extends AbstractSimpleExpression<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of this expression.
     */
    private DateTime value;

    /**
     * Teh datatype.
     */
    private AbstractDateTime dataType;

    /**
     * @param _value value
     * @param _dataType datatype
     */
    public DateTimeExpression(final DateTime _value,
                              final DateTimeDate _dataType)
    {
        setValue(_value);
        setDataType(_dataType);
    }

    @Override
    public String evaluate(final ReportParameters _reportParameters)
    {
        String ret = null;
        if (getDataType() != null) {
            ret = getDataType().valueToString(getValue(), _reportParameters.getLocale());
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public DateTime getValue()
    {
        return this.value;
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     * @return this for chaining
     */
    public DateTimeExpression setValue(final DateTime _value)
    {
        this.value = _value;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #dataType}.
     *
     * @return value of instance variable {@link #dataType}
     */
    public AbstractDateTime getDataType()
    {
        return this.dataType;
    }

    /**
     * Setter method for instance variable {@link #dataType}.
     *
     * @param _dataType value for instance variable {@link #dataType}
     * @return this for chaining
     */
    public DateTimeExpression setDataType(final AbstractDateTime _dataType)
    {
        this.dataType = _dataType;
        return this;
    }
}
