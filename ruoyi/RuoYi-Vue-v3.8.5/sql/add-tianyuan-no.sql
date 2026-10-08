-- 田园号：为已有用户补齐服务端生成的唯一身份标识。
-- 适用于已部署旧版本数据库；全新数据库请直接执行 ry_20220822.sql。
alter table sys_user add column tianyuan_no varchar(16) default null comment '田园号（TY+14位数字，注册后不可修改）';
alter table sys_user add column birthday varchar(10) default '' comment '出生日期';
-- 历史账号仅在缺失时补齐随机田园号；已有田园号保持不变。
update sys_user set tianyuan_no = concat('TY', lpad(user_id, 14, '0')) where tianyuan_no is null or tianyuan_no = '';
update sys_user set tianyuan_no = concat('TY', lpad(user_id, 14, '0')) where tianyuan_no not regexp '^TY[0-9]{14}$';
alter table sys_user add unique key uk_sys_user_tianyuan_no (tianyuan_no);
alter table sys_user add unique key uk_sys_user_user_name (user_name);
