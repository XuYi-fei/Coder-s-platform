-- =====================================================
-- 用户基础表（user & user_info）
-- 原项目表结构未纳入 Liquibase，此处补充，供全新安装使用
-- =====================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID（主键）',
    `third_account_id`  VARCHAR(128) NOT NULL DEFAULT ''    COMMENT '第三方账号 ID（微信等）',
    `login_type`        TINYINT      NOT NULL DEFAULT 0     COMMENT '登录方式：0=微信 1=用户名密码',
    `deleted`           TINYINT      NOT NULL DEFAULT 0     COMMENT '软删除标记：0=正常 1=已删除',
    `user_name`         VARCHAR(64)           DEFAULT NULL  COMMENT '登录用户名',
    `password`          VARCHAR(128)          DEFAULT NULL  COMMENT '密码（MD5+盐）',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`),
    INDEX `idx_third_account` (`third_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录表';

CREATE TABLE IF NOT EXISTS `user_info` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL               COMMENT '关联 user.id',
    `user_name`   VARCHAR(64)           DEFAULT NULL  COMMENT '昵称（显示名称）',
    `photo`       VARCHAR(256)          DEFAULT NULL  COMMENT '头像 URL',
    `position`    VARCHAR(50)           DEFAULT ''    COMMENT '职位',
    `company`     VARCHAR(50)           DEFAULT ''    COMMENT '公司',
    `profile`     VARCHAR(255)          DEFAULT ''    COMMENT '个人简介',
    `extend`      VARCHAR(1024)         DEFAULT NULL  COMMENT '扩展字段（JSON 字符串）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0     COMMENT '软删除：0=正常 1=已删除',
    `user_role`   TINYINT      NOT NULL DEFAULT 0     COMMENT '角色：0=普通用户 1=超级管理员',
    `ip`          JSON                  DEFAULT NULL  COMMENT 'IP 信息（JSON）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个人信息表';

-- =====================================================
-- 初始管理员账号
-- 账号：admin  密码：admin
-- 加密规则：MD5(plain[0:3] + salt + plain[3:])
--   salt="tech_π"  salt-index=3
--   salted="admtech_πin"  →  MD5 = df3a4143b663a086d1c006c8084db1b1
--
-- 如需修改密码，用如下 Python 片段重新计算哈希值：
--   import hashlib
--   salt = 'tech_\u03c0'   # tech_π
--   p = 'your_new_password'
--   salted = p[:3] + salt + p[3:] if len(p) > 3 else p + salt
--   print(hashlib.md5(salted.encode('utf-8')).hexdigest())
-- =====================================================

-- 插入 user 记录（如果 admin 用户名已存在则跳过）
INSERT IGNORE INTO `user`
  (`third_account_id`, `login_type`, `deleted`, `user_name`, `password`, `create_time`, `update_time`)
VALUES
  ('', 1, 0, 'admin', 'df3a4143b663a086d1c006c8084db1b1', NOW(), NOW());

-- 插入 user_info 记录（关联到上面插入的 user，user_role=1 代表超级管理员）
INSERT IGNORE INTO `user_info`
  (`user_id`, `user_name`, `photo`, `position`, `company`, `profile`, `deleted`, `user_role`, `create_time`, `update_time`)
SELECT
  u.id,
  'Admin',
  'https://static.developers.pub/static/img/logo.b2ff606.jpeg',
  'Administrator',
  '',
  '系统管理员',
  0,
  1,
  NOW(),
  NOW()
FROM `user` u
WHERE u.user_name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `user_info` ui WHERE ui.user_id = u.id
  );
