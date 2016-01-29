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
package org.efaps.esjp.admin.common.systemconfiguration;

import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.util.EFapsException;

/**
 * The listener interface for receiving ISysConf events. The class that is
 * interested in processing a ISysConf event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addISysConfListener</code> method. When the ISysConf event
 * occurs, that object's appropriate method is invoked.
 *
 * @see ISysConfEvent
 */
@EFapsUUID("55808114-0b50-4041-91d3-04b06eac874c")
@EFapsApplication("eFaps-Kernel")
public interface ISysConfListener
    extends IEsjpListener
{

    /**
     * Adds the attributes.
     *
     * @param _uuid2attr the uuid2attr
     * @throws EFapsException on error
     */
    void addAttributes(final Map<String, List<ISysConfAttribute>> _uuid2attr)
        throws EFapsException;

    /**
     * Adds the links.
     *
     * @param _uuid2link the uuid2link
     * @throws EFapsException on error
     */
    void addLinks(final Map<String, List<ISysConfLink>> _uuid2link)
        throws EFapsException;

}
