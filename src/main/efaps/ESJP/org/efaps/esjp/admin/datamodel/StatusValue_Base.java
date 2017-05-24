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

package org.efaps.esjp.admin.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.api.ui.IOption;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.Update;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uiform.Edit;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Status from the Database.<br>
 *
 * @author The eFaps Team
 */
@EFapsUUID("c0e0547a-a39f-4741-ae3d-c196e8cb2f60")
@EFapsApplication("eFaps-Kernel")
public abstract class StatusValue_Base
    extends AbstractCommon
    implements EventExecution
{

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter parameter as defined by the efaps api
     * @return map with value and keys
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final UIValue uiValue = (UIValue) _parameter.get(ParameterValues.UIOBJECT);

        final List<IOption> values = new ArrayList<>();
        if (uiValue.getTargetMode().equals(TargetMode.VIEW) || uiValue.getTargetMode().equals(TargetMode.PRINT)
                        || uiValue.getTargetMode().equals(TargetMode.UNKNOWN)) {
            final Object object = uiValue.getDbValue();
            if (object instanceof Long) {
                final Status status = Status.get((Long) object);
                values.add(getOption(_parameter)
                                .setLabel(status.getLabel())
                                .setValue(status.getId())
                                .setSelected(true));
            }
        } else if (uiValue.getTargetMode().equals(TargetMode.CREATE)) {
            final Type type = uiValue.getAttribute().getLink();
            final StatusGroup group = Status.get(type.getUUID());
            for (final Status status : group.values()) {
                values.add(getOption(_parameter)
                                .setLabel(status.getLabel())
                                .setValue(status.getId()));
            }
        } else {
            if (uiValue.getInstance().getType().isCheckStatus()) {
                final Type type = uiValue.getInstance().getType().getStatusAttribute().getLink();
                final StatusGroup group = Status.get(type.getName());
                for (final Status status : group.values()) {
                    values.add(getOption(_parameter)
                                    .setLabel(status.getLabel())
                                    .setValue(status.getId())
                                    .setSelected(Long.valueOf(status.getId()).equals(uiValue.getDbValue())));
                }
            }
        }
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * Method to set the Status for an Instance.<br>
     *
     * <pre>
     * <code>
     *  &lt;execute program=&quot;org.efaps.esjp.admin.datamodel.StatusValue&quot; method=&quot;setStatus&quot;&gt;
     *      &lt;property name=&quot;Status&quot;&gt;Open&lt;/property&gt;
     *  &lt;/execute&gt;
     *  <br>
     * </code>
     * </pre>
     *
     * @param _parameter parameter as defined by the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return setStatus(final Parameter _parameter)
        throws EFapsException
    {
        final String statusName = getProperty(_parameter, "Status");
        final boolean updateInstance = "true".equalsIgnoreCase(getProperty(_parameter, "UpdateInstance"));
        final boolean evalOID = "true".equalsIgnoreCase(getProperty(_parameter, "EvalOID"));
        final Collection<String> noUpdateStatus = analyseProperty(_parameter, "NoUpdateStatus").values();

        final Set<Instance> instancesTmp = new HashSet<>();
        if (evalOID) {
            final String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
            for (final String oid : oids) {
                final Instance inst = Instance.get(oid);
                if (inst.isValid()) {
                    instancesTmp.add(inst);
                }
            }
        } else {
            instancesTmp.add(_parameter.getInstance());
        }
        final Set<Instance> instances;
        if (containsProperty(_parameter, "Select4Instance")) {
            instances = new HashSet<>();
            for (final Instance inst : instancesTmp) {
                final PrintQuery print = new PrintQuery(inst);
                print.addSelect(getProperty(_parameter, "Select4Instance"));
                print.execute();
                final Instance instTmp = print.getSelect(getProperty(_parameter, "Select4Instance"));
                if (InstanceUtils.isValid(instTmp)) {
                    instances.add(instTmp);
                }
            }
        } else {
            instances = instancesTmp;
        }
        for (final Instance inst : instances) {
            final Status status = Status.find(inst.getType().getStatusAttribute().getLink().getName(), statusName);
            if (status != null) {
                boolean doUpdate = true;
                if (!noUpdateStatus.isEmpty()) {
                    final PrintQuery print = new PrintQuery(inst);
                    print.addAttribute(inst.getType().getStatusAttribute());
                    print.execute();
                    final Long currStatusId = print.<Long>getAttribute(inst.getType().getStatusAttribute());
                    for (final String noUpdate : noUpdateStatus) {
                        final Status noUp = Status.find(inst.getType().getStatusAttribute().getLink().getName(),
                                        noUpdate);
                        if (noUp != null && currStatusId == noUp.getId()) {
                            doUpdate = false;
                            break;
                        }
                    }
                }
                if (doUpdate) {
                    // the instance must be updated first, because after
                    // changing the status the
                    // user might not have the right to update any more
                    if (updateInstance) {
                        _parameter.put(ParameterValues.INSTANCE, inst);
                        updateInstance(_parameter);
                    }
                    final Update update = new Update(inst);
                    update.add(inst.getType().getStatusAttribute(), ((Long) status.getId()).toString());
                    update.execute();
                }
                // let others participate
                for (final ISetStatusListener listener : Listener.get().<ISetStatusListener>invoke(
                                ISetStatusListener.class)) {
                    listener.afterSetStatus(_parameter, inst, status);
                }
            }
        }
        final Return ret = new Return();
        return ret;
    }

    /**
     * @param _parameter parameter as defined by the eFaps API
     * @throws EFapsException on error
     */
    protected void updateInstance(final Parameter _parameter)
        throws EFapsException
    {
        new Edit().execute(_parameter);
    }

    /**
     * Gets the option.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the option
     */
    protected StatusValueOption getOption(final Parameter _parameter)
    {
        return new StatusValueOption();
    }


    /**
     * The Class StatusValueOption.
     */
    public static class StatusValueOption
        implements IOption
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The label. */
        private String label;

        /** The object. */
        private Object value;

        /** The selected. */
        private boolean selected;

        @Override
        public String getLabel()
        {
            return this.label;
        }

        @Override
        public Object getValue()
        {
            return this.value;
        }

        @Override
        public boolean isSelected()
        {
            return this.selected;
        }

        /**
         * Sets the label.
         *
         * @param _label the label
         * @return the range value option
         */
        public StatusValueOption setLabel(final String _label)
        {
            this.label = _label;
            return this;
        }

        /**
         * Sets the selected.
         *
         * @param _selected the selected
         * @return the range value option
         */
        public StatusValueOption setSelected(final boolean _selected)
        {
            this.selected = _selected;
            return this;
        }

        /**
         * Sets the value.
         *
         * @param _value the value
         * @return the range value option
         */
        public StatusValueOption setValue(final Object _value)
        {
            this.value = _value;
            return this;
        }
    }
}
