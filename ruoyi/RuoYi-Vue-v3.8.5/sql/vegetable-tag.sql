-- Run against the existing Vegetable database. Safe to run more than once.
-- Restart the backend (or refresh the dictionary cache) after applying to a running instance.
USE Vegetable;
START TRANSACTION;

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '蔬菜标签', 'vegetable_tag', '0', 'admin', NOW(), '病害问答作物分类'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'vegetable_tag');

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT seed.sort_order, seed.name, seed.name, 'vegetable_tag', '', 'default', 'N', '0', 'admin', NOW(), '病害问答作物'
FROM (
    SELECT 1 AS sort_order, '黄瓜' AS name UNION ALL
    SELECT 2, '茄子' UNION ALL
    SELECT 3, '辣椒' UNION ALL
    SELECT 4, '番茄' UNION ALL
    SELECT 5, '丝瓜'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data d WHERE d.dict_type = 'vegetable_tag' AND d.dict_label = seed.name);

-- Make room immediately after the user-management menu only on first installation.
SET @vegetable_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND path = 'vegetable-tag' LIMIT 1);
UPDATE sys_menu SET order_num = order_num + 1
WHERE parent_id = 1 AND order_num >= 2 AND @vegetable_menu_id IS NULL;

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '蔬菜标签', 1, 2, 'vegetable-tag', 'system/vegetableTag/index', '', 1, 0, 'C', '0', '0', 'system:vegetableTag:list', 'dict', 'admin', NOW(), '蔬菜标签管理'
WHERE @vegetable_menu_id IS NULL;
SET @vegetable_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND path = 'vegetable-tag' LIMIT 1);

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT actions.name, @vegetable_menu_id, actions.sort_order, '', '', '', 1, 0, 'F', '0', '0', actions.perms, '#', 'admin', NOW(), ''
FROM (
    SELECT 1 AS sort_order, '蔬菜标签查询' AS name, 'system:vegetableTag:query' AS perms UNION ALL
    SELECT 2, '蔬菜标签新增', 'system:vegetableTag:add' UNION ALL
    SELECT 3, '蔬菜标签修改', 'system:vegetableTag:edit' UNION ALL
    SELECT 4, '蔬菜标签删除', 'system:vegetableTag:remove'
) AS actions
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.parent_id = @vegetable_menu_id AND m.perms = actions.perms);

-- Preserve each role's current access policy: only roles already granted user management receive this menu.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (SELECT DISTINCT role_id FROM sys_role_menu WHERE menu_id = 100) AS roles
CROSS JOIN (SELECT menu_id FROM sys_menu WHERE menu_id = @vegetable_menu_id OR parent_id = @vegetable_menu_id) AS menus
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu existing
    WHERE existing.role_id = roles.role_id AND existing.menu_id = menus.menu_id
);
COMMIT;
