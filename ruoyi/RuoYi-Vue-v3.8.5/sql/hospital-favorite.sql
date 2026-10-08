CREATE TABLE IF NOT EXISTS sys_hospital_favorite (
 favorite_id bigint(20) NOT NULL AUTO_INCREMENT,
 user_id bigint(20) NOT NULL,
 author_account varchar(64) NOT NULL,
 post_id bigint(20) NOT NULL,
 created_at varchar(30) NOT NULL,
 original_author_id varchar(64), original_author_account varchar(64), crop varchar(30), planting_time varchar(30), description varchar(1000),
 PRIMARY KEY (favorite_id), UNIQUE KEY uk_favorite_user_post (user_id,post_id), KEY idx_favorite_author_account (author_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
