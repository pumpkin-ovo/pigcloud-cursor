-- 该脚本不要直接执行， 注意维护菜单的父节点ID 默认 父节点-1 , 

-- 菜单SQL
insert into sys_menu ( menu_id,parent_id, path, permission, menu_type, icon, del_flag, create_time, sort_order, update_time, name)
values (1733883231537, '-1', '/books/books/index', '', '0', 'icon-bangzhushouji', '0', null , '8', null , '图书馆图书信息表管理');

-- 菜单对应按钮SQL
insert into sys_menu ( menu_id,parent_id, permission, menu_type, path, icon, del_flag, create_time, sort_order, update_time, name)
values (1733883231538,1733883231537, 'books_books_view', '1', null, '1',  '0', null, '0', null, '图书馆图书信息表查看');

insert into sys_menu ( menu_id,parent_id, permission, menu_type, path, icon, del_flag, create_time, sort_order, update_time, name)
values (1733883231539,1733883231537, 'books_books_add', '1', null, '1',  '0', null, '1', null, '图书馆图书信息表新增');

insert into sys_menu (menu_id, parent_id, permission, menu_type, path, icon,  del_flag, create_time, sort_order, update_time, name)
values (1733883231540,1733883231537, 'books_books_edit', '1', null, '1',  '0', null, '2', null, '图书馆图书信息表修改');

insert into sys_menu (menu_id, parent_id, permission, menu_type, path, icon, del_flag, create_time, sort_order, update_time, name)
values (1733883231541,1733883231537, 'books_books_del', '1', null, '1',  '0', null, '3', null, '图书馆图书信息表删除');

insert into sys_menu ( menu_id,parent_id, permission, menu_type, path, icon, del_flag, create_time, sort_order, update_time, name)
values (1733883231542,1733883231537, 'books_books_export', '1', null, '1',  '0', null, '3', null, '导入导出');