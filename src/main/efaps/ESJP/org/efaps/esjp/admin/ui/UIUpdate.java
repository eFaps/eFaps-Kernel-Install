/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("2931694f-8a00-491e-8bee-d641145119f0")
@EFapsApplication("eFaps-Kernel")
public class UIUpdate
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIUpdate.class);

    /**
     * Method is used to add a Command or Menu to an existing Menu in a defined
     * position. This esjp is only used from the install scripts.
     *
     * @param _uuidAdd      UUID of the command or menu to add
     * @param _uuidMenu     UUID of the menu the command or menu will be added to
     * @param _pos          position in the menu, to ignore this "-1" can be used
     * @throws EFapsException on error
     */
    public void add2Menu(final String _uuidAdd,
                         final String _uuidMenu,
                         final Integer _pos)
        throws EFapsException
    {
        add2Menu(_uuidAdd, _uuidMenu, _pos,
                new String[] { "Admin_UI_Command", "Admin_UI_Menu", "Admin_UI_Menu2Command", "FromMenu", "ToCommand" });
    }

    /**
     * Method is used to add a Command/Menu/Help to an existing Menu in a defined
     * position. This esjp is only used from the install scripts.
     *
     * @param _uuidAdd      UUID of the command or menu to add
     * @param _uuidMenu     UUID of the menu the command or menu will be added to
     * @param _pos          position in the menu, to ignore this "-1" can be used
     * @param _types        types used for the queries
     * @throws EFapsException on error
     * @throws EFapsException on error
     */
    protected void add2Menu(final String _uuidAdd,
                            final String _uuidMenu,
                            final Integer _pos,
                            final String[] _types)
        throws EFapsException
    {
        // get the Menu/Command to be connected
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(_types[0]));
        queryBldr.addWhereAttrEqValue("UUID", _uuidAdd);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        if (query.next()) {
            final Instance addInst = query.getCurrentValue();
            // get the Menu that the Menu must be connected to
            final QueryBuilder queryBldrMenu = new QueryBuilder(Type.get(_types[1]));
            queryBldrMenu.addWhereAttrEqValue("UUID", _uuidMenu);
            final InstanceQuery queryMenu = queryBldrMenu.getQuery();
            queryMenu.execute();
            if (queryMenu.next()) {
                // get the relation and if it does not exist create one
                final Instance menuInst = queryMenu.getCurrentValue();

                final QueryBuilder queryBldrRel = new QueryBuilder(Type.get(_types[2]));
                queryBldrRel.addWhereAttrEqValue(_types[3], menuInst.getId());
                queryBldrRel.addWhereAttrEqValue(_types[4], addInst.getId());
                final InstanceQuery queryRel = queryBldrRel.getQuery();
                queryRel.execute();

                if (!queryRel.next()) {
                    final Insert insert = new Insert(_types[2]);
                    insert.add(_types[3], menuInst.getId());
                    insert.add(_types[4], addInst.getId());
                    insert.execute();
                    if (_pos > -1) {
                        // sort the instances so that the new one is add the given position
                        final QueryBuilder queryBldrSort = new QueryBuilder(Type.get(_types[2]));
                        queryBldrSort.addWhereAttrEqValue(_types[3], menuInst.getId());
                        final MultiPrintQuery multi = queryBldrSort.getPrint();
                        multi.addAttribute("ID", _types[4]);
                        multi.execute();
                        final Map<Long, Long> pos2cmds = new TreeMap<Long, Long>();
                        while (multi.next()) {
                            pos2cmds.put(multi.<Long>getAttribute("ID"), multi.<Long>getAttribute(_types[4]));
                        }
                        final List<Long> target = new ArrayList<Long>();
                        for (final Entry<Long, Long> entry : pos2cmds.entrySet()) {
                            if (addInst.getId() == entry.getValue()) {
                                target.add(_pos, entry.getValue());
                            } else {
                                target.add(entry.getValue());
                            }
                        }
                        final Iterator<Long> iter = target.iterator();
                        for (final Entry<Long, Long> entry : pos2cmds.entrySet()) {
                            final Update update = new Update(_types[2], entry.getKey().toString());
                            update.add(_types[4], iter.next());
                            update.execute();
                        }
                    }
                }
            } else {
                UIUpdate.LOG.warn("Could not find Cmd/Menu '{}' that should be added to Cmd/Menu '{}'.", _uuidMenu,
                                _uuidAdd);
            }
        } else {
            UIUpdate.LOG.warn("Could not find the Cmd/Menu '{}' the Cmd/Menu '{}' should be added to", _uuidAdd,
                            _uuidMenu);
        }
    }


    /**
     * Method is used to disconnect a command or menu from a menu.
     * This esjp is only used from the install scripts. The caches
     * are not used so that the kernel runlevel is enough to execute
     * this method.
     * @param _uuidRemove   uuid of the command,menu to be disconnected
     * @param _uuidMenu     uuid of the menu the <code>_uuidRemove</code> will
     *                      be removed from
     * @param _types        types used for the queries
     * @throws EFapsException on error
     */
    protected void removeFromMenu(final String _uuidRemove,
                                  final String _uuidMenu,
                                  final String[] _types)
        throws EFapsException
    {
        // get the command to be removed
        final QueryBuilder queryBldrRem = new QueryBuilder(Type.get(_types[0]));
        queryBldrRem.addWhereAttrEqValue("UUID", _uuidRemove);
        final InstanceQuery queryRem = queryBldrRem.getQuery();
        queryRem.execute();
        if (queryRem.next()) {
            final QueryBuilder queryBldrMenu = new QueryBuilder(Type.get(_types[0]));
            queryBldrMenu.addWhereAttrEqValue("UUID", _uuidMenu);
            final InstanceQuery queryMenu = queryBldrMenu.getQuery();
            queryMenu.execute();
            if (queryMenu.next()) {
                final QueryBuilder queryBldr = new QueryBuilder(Type.get(_types[1]));
                queryBldr.addWhereAttrEqValue(_types[2], queryMenu.getCurrentValue().getId());
                queryBldr.addWhereAttrEqValue(_types[3], queryRem.getCurrentValue().getId());
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                if (query.next()) {
                    final Delete del = new Delete(query.getCurrentValue());
                    del.execute();
                }
            }
        }
    }


    /**
     * Method is used to disconnect a command or menu from a menu.
     * This esjp is only used from the install scripts. The caches
     * are not used so that the kernel runlevel is enough to execute
     * this method.
     * @param _uuidRemove   uuid of the command,menu to be disconnected
     * @param _uuidMenu     uuid of the menu the <code>_uuidRemove</code> will
     *                      be removed from
     * @throws EFapsException on error
     */
    public void removeFromMenu(final String _uuidRemove,
                               final String _uuidMenu)
        throws EFapsException
    {
        removeFromMenu(_uuidRemove, _uuidMenu,
                        new String[] {"Admin_UI_Command", "Admin_UI_Menu2Command", "FromMenu", "ToCommand" });
    }

    /**
     * Method is used to add a HelpMenu an existing HelpMenu in a defined
     * position. This esjp is only used from the install scripts.
     *
     * @param _uuidAdd      UUID of the HelpMenu to add
     * @param _uuidMenu     UUID of the HelpMenu be added to
     * @param _pos          position in the menu, to ignore this "-1" can be used
     * @throws EFapsException on error
     */
    public void add2Help(final String _uuidAdd,
                         final String _uuidMenu,
                         final Integer _pos)
        throws EFapsException
    {
        add2Menu(_uuidAdd, _uuidMenu, _pos,
                   new String[] { "Admin_Help_Menu", "Admin_Help_Menu", "Admin_Help_Menu2Menu", "FromLink", "ToLink" });
    }

    /**
     * Method is used to disconnect a HelpMenu from a HelpMenu.
     * This esjp is only used from the install scripts. The caches
     * are not used so that the kernel runlevel is enough to execute
     * this method.
     * @param _uuidRemove   uuid of the command,menu to be disconnected
     * @param _uuidMenu     uuid of the menu the <code>_uuidRemove</code> will
     *                      be removed from
     * @throws EFapsException on error
     */
    public void removeFromHelp(final String _uuidRemove,
                               final String _uuidMenu)
        throws EFapsException
    {
        removeFromMenu(_uuidRemove, _uuidMenu,
                        new String[] {"Admin_Help_Menu", "Admin_Help_Menu2Menu", "FromLink", "ToLink" });
    }
}
