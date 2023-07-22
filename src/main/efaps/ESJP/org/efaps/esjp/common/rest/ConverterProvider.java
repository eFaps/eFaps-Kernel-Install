/*
 * Copyright 2003 - 2023 The eFaps Team
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
@EFapsUUID("153a12e5-719b-43c7-9aa6-efb1ee6ff3bc")
@EFapsApplication("eFaps-Kernel")
public class ConverterProvider implements ParamConverterProvider
{

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
            final Annotation[] annotations) {
        if (rawType.equals(OffsetDateTime.class)) {
            return (ParamConverter<T>) new OffsetDateTimeConverter();
        } else if (rawType.equals(LocalDate.class)) {
            return (ParamConverter<T>) new LocalDateConverter();
        }
        return null;
    }

}
