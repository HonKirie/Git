USE Vegetable;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS sys_vegetable_category (
  category_id bigint NOT NULL AUTO_INCREMENT,
  parent_id bigint NOT NULL DEFAULT 0,
  ancestors varchar(500) NOT NULL DEFAULT '0',
  category_name varchar(30) NOT NULL,
  order_num int NOT NULL DEFAULT 0,
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (category_id),
  KEY idx_vegetable_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @has_ancestors = (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'sys_vegetable_category' AND column_name = 'ancestors');
SET @sql = IF(@has_ancestors = 0,
  'ALTER TABLE sys_vegetable_category ADD COLUMN ancestors varchar(500) NOT NULL DEFAULT ''0'' AFTER parent_id',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_vegetable_record (
  record_id bigint NOT NULL AUTO_INCREMENT,
  category_id bigint NOT NULL,
  user_id bigint NOT NULL,
  planting_days int NOT NULL,
  issue varchar(1000) NOT NULL,
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (record_id),
  KEY idx_vegetable_record_category (category_id),
  KEY idx_vegetable_record_user (user_id),
  CONSTRAINT fk_vegetable_record_category FOREIGN KEY (category_id) REFERENCES sys_vegetable_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_vegetable_category (parent_id, ancestors, category_name, order_num, status, create_by, create_time)
SELECT 0, '0', d.dict_label, d.dict_sort, d.status, 'admin', NOW()
FROM sys_dict_data d
WHERE d.dict_type = 'vegetable_tag'
  AND NOT EXISTS (SELECT 1 FROM sys_vegetable_category c WHERE c.parent_id = 0
      AND c.category_name COLLATE utf8mb4_unicode_ci = d.dict_label COLLATE utf8mb4_unicode_ci);

-- Reuse the existing menu ID so role grants are preserved.
UPDATE sys_menu
SET menu_name = '蔬菜管理', path = 'vegetable', component = 'system/vegetable/index',
    perms = 'system:vegetable:list', remark = '蔬菜分类与种植记录管理'
WHERE parent_id = 1 AND path = 'vegetable-tag';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:vegetableTag:', 'system:vegetable:')
WHERE parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND path = 'vegetable') x);
UPDATE sys_menu SET menu_name = REPLACE(menu_name, '蔬菜标签', '蔬菜')
WHERE parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND path = 'vegetable') x);
COMMIT;
