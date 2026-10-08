-- 清空若依已有字典，并创建田园采风用户身份字典。
-- 请在 Vegetable 数据库中执行；执行前请确认确实要删除所有现有字典。
USE Vegetable;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM sys_dict_data;
DELETE FROM sys_dict_type;
ALTER TABLE sys_dict_data AUTO_INCREMENT = 1;
ALTER TABLE sys_dict_type AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_dict_type
    (dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    ('用户身份', 'user_identity', '0', 'admin', NOW(), '田园采风用户身份');

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
    (1, '农友', 'farmer', 'user_identity', '', 'success', 'Y', '0', 'admin', NOW(), '普通农业用户'),
    (2, '农技达人', 'expert', 'user_identity', '', 'warning', 'N', '0', 'admin', NOW(), '农业技术达人'),
    (3, '农技老师', 'teacher', 'user_identity', '', 'primary', 'N', '0', 'admin', NOW(), '农业技术教师'),
    (4, '专家', 'specialist', 'user_identity', '', 'danger', 'N', '0', 'admin', NOW(), '农业领域专家');
