/*
 * Copyright 2003 - 2022 The eFaps Team
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
package org.efaps.esjp.common.rest;

import java.time.LocalDate;

import javax.ws.rs.ext.ParamConverter;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

@EFapsUUID("d9e487bc-2d56-46f7-9032-0a58797b81da")
@EFapsApplication("eFaps-Kernel")
public class LocalDateConverter implements ParamConverter<LocalDate> {

    @Override
    public LocalDate fromString(final String value) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value);
    }

    @Override
    public String toString(final LocalDate value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}