INSERT INTO t_cmabstract(
	 typeid, name, uuid, creator, created, modifier, modified)
	VALUES ((SELECT ID FROM t_cmabstract where name = 'Admin_DataModel_AttributeType'), 'AssociationLink', '0d296eba-0c1e-4b78-a2e3-01b1f4991cfe', 1, current_timestamp, 1, current_timestamp);

INSERT INTO public.t_dmattributetype(id, classname, classnameui, createupdate)
	VALUES ((SELECT ID FROM t_cmabstract where uuid = '0d296eba-0c1e-4b78-a2e3-01b1f4991cfe'), 'org.efaps.admin.datamodel.attributetype.AssociationLinkType', 'org.efaps.admin.datamodel.ui.StringUI', 1);

INSERT INTO t_runleveldef(
	runlevelid, priority, class, method)
	VALUES (1, 12, 'org.efaps.admin.common.Association', 'initialize');

