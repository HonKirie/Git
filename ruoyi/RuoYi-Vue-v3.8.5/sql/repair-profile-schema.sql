-- 修复个人资料字段。请在 Vegetable 数据库中执行一次。
-- 适用于当前后端已经升级、但数据库仍是旧结构的情况。
USE Vegetable;

SET @sql = (SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'tianyuan_no'),
  'SELECT 1',
  'ALTER TABLE sys_user ADD COLUMN tianyuan_no varchar(30) DEFAULT NULL COMMENT ''田园号（注册时登录账号）'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'birthday'),
  'SELECT 1',
  'ALTER TABLE sys_user ADD COLUMN birthday varchar(10) DEFAULT '''' COMMENT ''出生日期'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史账号仅在缺失时补齐随机格式田园号；已有田园号保持不变。
UPDATE sys_user SET tianyuan_no = CONCAT('TY', LPAD(user_id, 14, '0')) WHERE tianyuan_no IS NULL OR tianyuan_no = '';
UPDATE sys_user SET tianyuan_no = CONCAT('TY', LPAD(user_id, 14, '0')) WHERE tianyuan_no NOT REGEXP '^TY[0-9]{14}$';

SET @sql = (SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND index_name = 'uk_sys_user_tianyuan_no'),
  'SELECT 1',
  'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_tianyuan_no (tianyuan_no)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND index_name = 'uk_sys_user_user_name'),
  'SELECT 1',
  'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_user_name (user_name)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT user_id, user_name, tianyuan_no, birthday FROM sys_user ORDER BY user_id;
