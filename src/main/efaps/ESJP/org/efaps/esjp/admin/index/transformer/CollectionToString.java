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
package org.efaps.esjp.admin.index.transformer;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.efaps.admin.index.ITransformer;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.utils.StringUtils;

@EFapsUUID("91dd6956-e3f4-4bad-bb78-c79f3e87b52f")
@EFapsApplication("eFaps-Kernel")
public class CollectionToString
    implements ITransformer
{

    private static final Logger LOG = LoggerFactory.getLogger(CollectionToString.class);

    @Override
    public Object transform(final Object value)
        throws EFapsException
    {
        Object ret = "";
        if (value instanceof final Collection<?> collection) {
            final var stringValue = collection.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
            if (StringUtils.isNotBlank(stringValue)) {
                ret = stringValue;
            }
        } else if (value instanceof final String stringValue) {
            ret = stringValue;
        }
        LOG.debug("Got value: {} to be converted into: {} ", value, ret);
        return ret;
    }
}
