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


package org.efaps.esjp.admin.common;

import java.util.List;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.util.EFapsException;


/**
 * Contains some funtionalities related to SystemConfigurations.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("aa441e81-fd9f-4462-99c2-2d27bd37f137")
@EFapsRevision("$Rev$")
public abstract class SystemConf_Base
{
    /**
     * Add an ObjectAttribute for the Instance to the SystemConfiguration
     * specified by the given UUID.
     * @param _sysConfUUID  UUID of the related SystemConfiguration
     * @param _instance     INstance the ObjectAttribute belongs to
     * @param _value        value to add
     * @throws EFapsException on error
     */
    public void addObjectAttribute(final UUID _sysConfUUID,
                                   final Instance _instance,
                                   final String _value)
        throws EFapsException
    {
        final SystemConfiguration config = SystemConfiguration.get(_sysConfUUID);
        if (config != null) {
            if (config.getObjectAttributeValue(_instance) == null) {
                final Insert insert = new Insert(CIAdminCommon.SystemConfigurationObjectAttribute);
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.AbstractLink, config.getId());
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.Key, _instance.getOid());
                insert.add(CIAdminCommon.SystemConfigurationObjectAttribute.Value, _value);
                insert.execute();
            }
        }
    }

    public Return dropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Field field = new Field() {

            @Override
            public StringBuilder getDropDownField(final Parameter _parameter,
                                                  final List<DropDownPosition> _values)
                throws EFapsException
            {
                final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
                for (final DropDownPosition dropPos : _values) {
                    if (fieldValue != null && fieldValue.getValue() != null
                                    && fieldValue.getValue().equals(dropPos.getValue())) {
                        dropPos.setSelected(true);
                        break;
                    }
                }
                _values.set(0, new DropDownPosition("0", "--"));
                return super.getDropDownField(_parameter, _values);
            }

        };

        return field.dropDownFieldValue(_parameter);
    }
}
