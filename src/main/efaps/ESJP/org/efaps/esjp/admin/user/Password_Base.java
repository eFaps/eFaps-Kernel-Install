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
package org.efaps.esjp.admin.user;

import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update.Status;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.jaas.SetPasswordHandler;
import org.efaps.jaas.efaps.UserLoginModule.UpdateException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Esjp responsible for update, verification of passwords.
 *
 * @author The eFaps Team
 */
@EFapsUUID("508bc197-d6ee-4a3c-9f93-60a0e52ebe93")
@EFapsApplication("eFaps-Kernel")
public abstract class Password_Base
{
    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Password.class);

    /**
     * Key used for the field for the old password.
     */
    private static String PWDOLD = "passwordold";

    /**
     * Key used for the field for the new password.
     */
    private static String PWDNEW = "passwordnew";

    /**
     * Key used for the field for the new password repetition.
     */
    private static String PWDNEWREPEAT = "passwordnew2";

    /**
     * Method is called to change the password of a user in the efpas database.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return changePwdUI(final Parameter _parameter)
        throws EFapsException
    {

        final Context context = Context.getThreadContext();
        final String passwordold = context.getParameter(Password_Base.PWDOLD);
        final String passwordnew = context.getParameter(Password_Base.PWDNEW);
        final Return ret = new Return();

        final SetPasswordHandler handler = new SetPasswordHandler(AppAccessHandler.getApplicationKey());
        Password_Base.LOG.debug("Applying SetPasswordHandler with ApplicationKey {}",
                        AppAccessHandler.getApplicationKey());
        try {
            if (handler.setPassword(context.getPerson().getName(), passwordnew, passwordold)) {
                ret.put(ReturnValues.TRUE, "true");
            }
        } catch (final LoginException e) {
            if (e instanceof UpdateException) {
                ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
            } else {
                ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.changePwdUI.checkPassword");
            }
        }
        return ret;
    }

    /**
     * Method is called from a command to validate the form. It checks if the
     * new password and the repetition are equal.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return with true if equal
     * @throws EFapsException on error
     */
    public Return validateFormUI(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Context context = Context.getThreadContext();
        final String passwordnew = context.getParameter(Password_Base.PWDNEW);
        final String passwordnew2 = context.getParameter(Password_Base.PWDNEWREPEAT);

        if (passwordnew.equals(passwordnew2)) {
            ret.put(ReturnValues.TRUE, "true");
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validateFormUI.unequal");
        }
        return ret;
    }

    /**
     * Executed on a validate event on the attribute.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return validatePwdValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object[] newPwd = (Object[]) _parameter.get(ParameterValues.NEW_VALUES);
        // Admin_User_PwdLengthMin
        if (newPwd[0].toString().length() > EFapsSystemConfiguration.get().getAttributeValueAsInteger(
                        KernelSettings.PWDLENGHT)) {
            ret.put(ReturnValues.TRUE, "true");
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
        }
        return ret;
    }

    /**
     * This method is used to validate password length when set a Password for a
     * User.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return validatePwdLengthUI(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        if (hasSetPwdAccess(_parameter)) {
            final String newPwd = Context.getThreadContext().getParameter("setpassword");
            // Admin_User_PwdLengthMin
            if (newPwd.length() > EFapsSystemConfiguration.get().getAttributeValueAsInteger(
                            KernelSettings.PWDLENGHT)) {
                ret.put(ReturnValues.TRUE, "true");
            } else {
                ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
            }
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PersonSetPwdForm/Password.setPwdValueUI.NoRight");
        }
        return ret;
    }

    /**
     * This method is used from Admins which have the Role Common_Main_PwdChg to
     * set a Password for a User.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return setPwdValueUI(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();

        if (hasSetPwdAccess(_parameter)) {
            final String pwd = Context.getThreadContext().getParameter("setpassword");
            final org.efaps.admin.user.Person pers = org.efaps.admin.user.Person.get(instance.getId());
            final Status status = pers.setPassword(pwd);
            if (status.isOk()) {
                ret.put(ReturnValues.TRUE, "true");
            } else {
                ret.put(ReturnValues.VALUES, status.getReturnValue());
            }
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PersonSetPwdForm/Password.setPwdValueUI.NoRight");
        }
        return ret;
    }

    /**
     * If a user is member of the roles 'Admin_User_ModifyUsers' or 'Administration'
     * he can set the password.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return true if has teh right to set password, else false
     * @throws EFapsException the e faps exception
     */
    protected boolean hasSetPwdAccess(final Parameter _parameter)
        throws EFapsException
    {
        // Admin_User_ModifyUsers
        final Role modUser = Role.get(UUID.fromString("2819571d-f5c4-4ff8-b7a3-383c195782be"));
        // Administration
        final Role admin = Role.get(UUID.fromString("1d89358d-165a-4689-8c78-fc625d37aacd"));

        return Context.getThreadContext().getPerson().isAssigned(modUser)
                        || Context.getThreadContext().getPerson().isAssigned(admin);
    }
}
