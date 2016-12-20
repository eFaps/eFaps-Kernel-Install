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

package org.efaps.esjp.common.tag;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("fb11e24d-2aa0-4f3f-a3f3-d3d1361711a4")
@EFapsApplication("eFaps-Kernel")
public class Tag
    extends Tag_Base
{

    /**
     * Tag object.
     *
     * @param _parameter the parameter
     * @param _objectInst the object inst
     * @param _tagType the tag type
     * @return the instance
     * @throws EFapsException the eFaps exception
     */
    public static Instance tagObject(final Parameter _parameter,
                                     final Instance _objectInst,
                                     final Type _tagType)
        throws EFapsException
    {
        return Tag_Base.tagObject(_parameter, _objectInst, _tagType);
    }

    /**
     * Untag object.
     *
     * @param _parameter the parameter
     * @param _objectInst the object inst
     * @param _tagType the tag type
     * @return the instance
     * @throws EFapsException the e faps exception
     */
    public static Instance untagObject(final Parameter _parameter,
                                          final Instance _objectInst,
                                          final Type _tagType)
        throws EFapsException
    {
        return Tag_Base.untagObject(_parameter, _objectInst, _tagType);
    }
}


