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
package org.efaps.esjp.admin.index.transformer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.efaps.admin.index.ITransformer;
import org.efaps.util.EFapsException;

/**
 * The Class Name2Number.
 * Converts Strings to valid
 * @author The eFaps Team
 */
public class StringRight2Long
    implements ITransformer
{

    /**
     * Transform.
     *
     * @param _object the object
     * @return the object
     * @throws EFapsException on error
     */
    @Override
    public Object transform(final Object _object)
        throws EFapsException
    {
        Object ret = 0;
        if (_object instanceof String) {
            int i = 0;
            String subStr = "0";
            while (NumberUtils.isDigits(subStr)) {
                i++;
                subStr = StringUtils.right((String) _object, i);
            }
            if (i - 1 > 0) {
                ret = Long.parseLong(StringUtils.right((String) _object, i - 1));
            }
        } else {
            ret = _object;
        }
        return ret;
    }
}
