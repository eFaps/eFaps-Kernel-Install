--
-- Copyright © 2003 - 2024 The eFaps Team (-)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

INSERT INTO t_cmabstract(
	 typeid, name, uuid, creator, created, modifier, modified)
	VALUES ((SELECT ID FROM t_cmabstract where name = 'Admin_DataModel_AttributeType'), 'AssociationLink', '0d296eba-0c1e-4b78-a2e3-01b1f4991cfe', 1, current_timestamp, 1, current_timestamp);

INSERT INTO public.t_dmattributetype(id, classname, classnameui, createupdate)
	VALUES ((SELECT ID FROM t_cmabstract where uuid = '0d296eba-0c1e-4b78-a2e3-01b1f4991cfe'), 'org.efaps.admin.datamodel.attributetype.AssociationLinkType', 'org.efaps.admin.datamodel.ui.StringUI', 1);

INSERT INTO t_runleveldef(
	runlevelid, priority, class, method)
	VALUES (1, 12, 'org.efaps.admin.common.Association', 'initialize');

