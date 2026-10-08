-- 删除部门管理菜单，并将部门树替换为四个同级用户身份。
USE Vegetable;

START TRANSACTION;

-- 按当前数据库中的名称、路径和权限匹配，兼容自定义 menu_id。
DROP TEMPORARY TABLE IF EXISTS tmp_department_menu_ids;
CREATE TEMPORARY TABLE tmp_department_menu_ids (
    menu_id bigint NOT NULL PRIMARY KEY
);

INSERT INTO tmp_department_menu_ids (menu_id)
SELECT menu_id
FROM sys_menu
WHERE menu_name = '部门管理'
   OR path = 'system/dept/index'
   OR perms LIKE 'system:dept:%';

DELETE rm
FROM sys_role_menu rm
JOIN tmp_department_menu_ids dm ON dm.menu_id = rm.menu_id;

DELETE sm
FROM sys_menu sm
JOIN tmp_department_menu_ids dm ON dm.menu_id = sm.menu_id;

DROP TEMPORARY TABLE tmp_department_menu_ids;

-- 保留现有用户：原部门用户统一归入“农友”，避免出现无效 dept_id。
UPDATE sys_user SET dept_id = 100 WHERE del_flag = '0';

-- 删除旧部门及角色的数据范围关联，重建四个同级身份节点。
DELETE FROM sys_role_dept;
DELETE FROM sys_dept;

INSERT INTO sys_dept
    (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time)
VALUES
    (100, 0, '0', '农友',     1, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '', NULL),
    (101, 0, '0', '农技老师', 2, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '', NULL),
    (102, 0, '0', '农技达人', 3, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '', NULL),
    (103, 0, '0', '专家',     4, NULL, NULL, NULL, '0', '0', 'admin', NOW(), '', NULL);

ALTER TABLE sys_dept AUTO_INCREMENT = 104;
COMMIT;
