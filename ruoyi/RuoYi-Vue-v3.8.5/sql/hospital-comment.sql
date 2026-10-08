-- 病害问答评论表。执行一次后，前端评论由后端数据库保存。
CREATE TABLE IF NOT EXISTS `sys_hospital_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `author_id` varchar(30) NOT NULL COMMENT '发布者账号',
  `crop` varchar(30) NOT NULL COMMENT '作物',
  `planting_time` varchar(30) NOT NULL COMMENT '定植时间',
  `description` varchar(1000) NOT NULL COMMENT '问题描述',
  `media_type` varchar(10) DEFAULT NULL COMMENT '媒体类型',
  `media_files` varchar(4000) DEFAULT NULL COMMENT '媒体路径JSON',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `created_at` varchar(20) NOT NULL COMMENT '前端展示时间',
  PRIMARY KEY (`post_id`),
  KEY `idx_hospital_post_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害问答问题表';

CREATE TABLE IF NOT EXISTS `sys_hospital_comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '前端问题ID',
  `user_id` bigint(20) NOT NULL COMMENT '作者用户ID',
  `author_id` varchar(30) NOT NULL COMMENT '作者账号',
  `comment_text` varchar(200) NOT NULL COMMENT '评论内容',
  `reply_to_id` varchar(30) DEFAULT NULL COMMENT '被回复的账号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_hospital_comment_post_id` (`post_id`),
  KEY `idx_hospital_comment_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害问答评论表';
