-- 在已有 terminology 上增加「术语组」与 group_id（勿重复执行）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `terminology_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(32) DEFAULT NULL,
  `sort_weight` int(11) NOT NULL DEFAULT 0,
  `enabled` tinyint(4) NOT NULL DEFAULT 1,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_enabled` (`enabled`,`sort_weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 若列已存在会报错，可忽略或手动检查后执行
ALTER TABLE `terminology` ADD COLUMN `group_id` int(11) NULL DEFAULT NULL COMMENT '所属系统术语组' AFTER `owner_user_id`;
ALTER TABLE `terminology` ADD INDEX `idx_terminology_group` (`group_id`);
