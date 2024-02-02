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
package org.efaps.esjp.common.eql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c138e2dd-4361-4f7c-8da0-f859ce901c9a")
@EFapsApplication("eFaps-Kernel")
public abstract class ClassSelect_Base
    extends AbstractSelect
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClassSelect.class);

    /**
     * SelectBuilder for this select.
     */
    private SelectBuilder selectBuilder = SelectBuilder.get().clazz().type();

    /**
     * Level to be returned as value.
     */
    private int level = -1;

    /**
     * Initialize this IEsjpSelect.
     *
     * @param _instances list of instances
     * @param _parameters parameters
     * @throws EFapsException on error
     */
    @Override
    public void initialize(final List<Instance> _instances,
                           final String... _parameters)
        throws EFapsException
    {
        if (_parameters != null) {
            setLevel(Integer.parseInt(_parameters[0]));
        }
        final MultiPrintQuery multi = new MultiPrintQuery(_instances);
        final SelectBuilder sel = getSelectBuilder();
        multi.addSelect(sel);
        multi.execute();
        while (multi.next()) {
            final List<Classification> clazzes = multi.getSelect(sel);
            if (clazzes == null || clazzes.isEmpty()) {
                getValues().put(multi.getCurrentInstance(), "");
            } else {
                getValues().put(multi.getCurrentInstance(), evalValue(clazzes));
            }
        }
    }

    /**
     * @param _clazzes list of classifications
     * @return object for the select
     * @throws EFapsException on error
     */
    public Object evalValue(final List<Classification> _clazzes)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        for (final Classification clazz : _clazzes) {
            if (getLevel() < 0) {
                if (ret.length() > 0) {
                    ret.append(" - ");
                }
                ret.append(clazz.getLabel());
            } else {
                final List<String> labels = new ArrayList<>();
                Classification current = clazz;
                while (!current.isRoot()) {
                    labels.add(current.getLabel());
                    current = current.getParentClassification();
                }
                labels.add(current.getLabel());
                Collections.reverse(labels);
                if (ret.length() > 0) {
                    ret.append(" - ");
                }
                ret.append(labels.size() > getLevel() ? labels.get(getLevel()) : labels.get(labels.size() - 1));
            }
        }
        LOG.debug("Evaluated value: '{}", ret);
        return ret.toString();
    }

    /**
     * Getter method for the instance variable {@link #selectBuilder}.
     *
     * @return value of instance variable {@link #selectBuilder}
     */
    protected SelectBuilder getSelectBuilder()
    {
        return this.selectBuilder;
    }

    /**
     * Setter method for instance variable {@link #selectBuilder}.
     *
     * @param _selectBuilder value for instance variable {@link #selectBuilder}
     */
    protected void setSelectBuilder(final SelectBuilder _selectBuilder)
    {
        this.selectBuilder = _selectBuilder;
    }

    /**
     * Getter method for the instance variable {@link #level}.
     *
     * @return value of instance variable {@link #level}
     */
    protected int getLevel()
    {
        return this.level;
    }

    /**
     * Setter method for instance variable {@link #level}.
     *
     * @param _level value for instance variable {@link #level}
     */
    protected void setLevel(final int _level)
    {
        this.level = _level;
    }
}
