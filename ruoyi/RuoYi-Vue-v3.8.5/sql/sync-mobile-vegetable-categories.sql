-- Backfill valid mobile crops without importing numeric test names.
INSERT INTO sys_vegetable_category
    (parent_id, ancestors, category_name, order_num, status, create_by, create_time)
SELECT 0, '0', d.dict_label, 0, '0', d.create_by, NOW()
FROM sys_dict_data d
WHERE d.dict_type = 'vegetable_tag' AND d.status = '0'
  AND TRIM(d.dict_label) <> '' AND d.dict_label <> '全部'
  AND d.dict_label NOT REGEXP '^[[:digit:][:space:]]+$'
  AND NOT EXISTS (
    SELECT 1 FROM sys_vegetable_category c
    WHERE c.category_name COLLATE utf8mb4_unicode_ci = d.dict_label COLLATE utf8mb4_unicode_ci
  );
