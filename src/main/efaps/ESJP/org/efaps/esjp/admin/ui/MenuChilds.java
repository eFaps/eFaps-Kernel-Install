/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.admin.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * ESJP used to move the children of a command on e position up or down and to
 * evaluate the values for the sorting.
 * 
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f03932cf-7227-46c9-bb92-546245b1f49b")
@EFapsRevision("$Rev$")
public class MenuChilds {

	/**
	 * Method is called from a command to move the child of a menu on position
	 * up in the UI menus.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @return empty Return to follow interface
	 * @throws EFapsException
	 *             on error
	 * @see #move(Parameter, boolean)
	 */
	public Return moveChildUpUI(final Parameter _parameter) throws EFapsException {
		moveOfUI(_parameter, true);
		return new Return();
	}
	
	/**
	 * Method is called from a command to move the child of a menu on position
	 * up in the help menus.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @return empty Return to follow interface
	 * @throws EFapsException
	 *             on error
	 * @see #move(Parameter, boolean)
	 */
	public Return moveChildUpHelp(final Parameter _parameter) throws EFapsException {
		moveOfHelp(_parameter, true);
		return new Return();
	}

	/**
	 * Method is called from a command to move the child of a menu on position
	 * down in the UI menus.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @return empty Return to follow interface
	 * @throws EFapsException
	 *             on error
	 * @see #move(Parameter, boolean)
	 */
	public Return moveChildDownUI(final Parameter _parameter)
			throws EFapsException {
		moveOfUI(_parameter, false);
		return new Return();
	}
	
	/**
	 * Method is called from a command to move the child of a menu on position
	 * down in the help menus.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @return empty Return to follow interface
	 * @throws EFapsException
	 *             on error
	 * @see #move(Parameter, boolean)
	 */
	public Return moveChildDownHelp(final Parameter _parameter)
			throws EFapsException {
		moveOfHelp(_parameter, false);
		return new Return();
	}

	/**
	 * Method that moves a child up or down for the section of UI menu.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @param _up
	 *            move the child up
	 * @throws EFapsException
	 *             on error
	 */
	private void moveOfUI(final Parameter _parameter, final boolean _up)
			throws EFapsException {
		final Instance instance = _parameter.getInstance();
		final String[] oidRows = (String[]) _parameter
				.get(ParameterValues.OTHERS);
		if (oidRows != null && oidRows.length > 0) {

			final Instance selected = Instance.get(oidRows[0]);
			final SearchQuery query2 = new SearchQuery();
			query2.setQueryTypes("Admin_UI_Menu2Command");
			query2.addWhereExprEqValue("FromMenu", instance.getId());
			query2.addSelect("ID");
			query2.addSelect("ToCommand");
			query2.execute();
			final List<Long> ids = new ArrayList<Long>();
			final Map<Long, Long> id2cmd = new HashMap<Long, Long>();
			while (query2.next()) {
				final Long id = (Long) query2.get("ID");
				final Long cmdId = (Long) query2.get("ToCommand");
				ids.add(id);
				id2cmd.put(id, cmdId);
			}
			Collections.sort(ids);
			for (int i = 0; i < ids.size(); i++) {
				final Long actId = ids.get(i);
				final Long actCmd = id2cmd.get(actId);

				if (actId.equals(selected.getId())) {
					Long exId = null;
					// if move upwards and the selected is not the first
					if (_up && i > 0) {
						exId = ids.get(i - 1);
					} else if (!_up && i < ids.size() - 1) {
						exId = ids.get(i + 1);
					}
					if (exId != null) {
						// update the exChange
						final Update update = new Update(
								"Admin_UI_Menu2Command", exId.toString());
						update.add("ToCommand", actCmd);
						update.execute();
						update.close();

						// update the actual
						final Update updateAct = new Update(
								"Admin_UI_Menu2Command", actId.toString());
						updateAct.add("ToCommand", id2cmd.get(exId));
						updateAct.execute();
						updateAct.close();
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Method that moves a child up or down for the section of help menu.
	 * 
	 * @param _parameter
	 *            Parameter as provided to the esjp from eFaps
	 * @param _up
	 *            move the child up
	 * @throws EFapsException
	 *             on error
	 */
	private void moveOfHelp(final Parameter _parameter, final boolean _up)
			throws EFapsException {
		final Instance instance = _parameter.getInstance();
		final String[] oidRows = (String[]) _parameter
				.get(ParameterValues.OTHERS);
		if (oidRows != null && oidRows.length > 0) {

			final Instance selected = Instance.get(oidRows[0]);
			final SearchQuery query2 = new SearchQuery();
			query2.setQueryTypes("Admin_Help_Menu2Menu");
			query2.addWhereExprEqValue("FromLink", instance.getId());
			query2.addSelect("ID");
			query2.addSelect("ToLink");
			query2.execute();
			final List<Long> ids = new ArrayList<Long>();
			final Map<Long, Long> id2cmd = new HashMap<Long, Long>();
			while (query2.next()) {
				final Long id = (Long) query2.get("ID");
				final Long cmdId = (Long) query2.get("ToLink");
				ids.add(id);
				id2cmd.put(id, cmdId);
			}
			Collections.sort(ids);
			for (int i = 0; i < ids.size(); i++) {
				final Long actId = ids.get(i);
				final Long actCmd = id2cmd.get(actId);

				if (actId.equals(selected.getId())) {
					Long exId = null;
					// if move upwards and the selected is not the first
					if (_up && i > 0) {
						exId = ids.get(i - 1);
					} else if (!_up && i < ids.size() - 1) {
						exId = ids.get(i + 1);
					}
					if (exId != null) {
						// update the exChange
						final Update update = new Update(
								"Admin_Help_Menu2Menu", exId.toString());
						update.add("ToLink", actCmd);
						update.execute();
						update.close();

						// update the actual
						final Update updateAct = new Update(
								"Admin_Help_Menu2Menu", actId.toString());
						updateAct.add("ToLink", id2cmd.get(exId));
						updateAct.execute();
						updateAct.close();
					}
					break;
				}
			}
		}
	}
}
