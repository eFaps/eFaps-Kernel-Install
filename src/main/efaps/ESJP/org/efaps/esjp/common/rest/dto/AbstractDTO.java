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
package org.efaps.esjp.common.rest.dto;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

@EFapsUUID("979f1b8b-79e2-44ab-a37b-4897aa4a43bc")
@EFapsApplication("eFaps-Kernel")
public class AbstractDTO
{

    private final String oid;

    protected AbstractDTO(final Builder<?, ?> _builder)
    {
        this.oid = _builder.oid;
    }

    public String getOid()
    {
        return this.oid;
    }

    public static abstract class Builder<S extends Builder<S, T>, T extends AbstractDTO>
    {

        private String oid;

        @SuppressWarnings("unchecked")
        public S withOid(final String _oid)
        {
            this.oid = _oid;
            return (S) this;
        }

        public abstract T build();
    }
}
