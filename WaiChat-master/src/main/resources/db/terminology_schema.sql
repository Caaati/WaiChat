-- 已有库全量重建术语模块（慎用：会删数据）
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `terminology_alias`;
DROP TABLE IF EXISTS `terminology`;
DROP TABLE IF EXISTS `terminology_group`;

CREATE TABLE `terminology_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '组展示名',
  `code` varchar(32) DEFAULT NULL COMMENT '可选唯一键',
  `sort_weight` int(11) NOT NULL DEFAULT 0,
  `enabled` tinyint(4) NOT NULL DEFAULT 1,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_enabled` (`enabled`,`sort_weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `terminology` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_user_id` int(11) DEFAULT NULL COMMENT 'NULL=系统默认术语库，非空=用户自定义',
  `group_id` int(11) DEFAULT NULL COMMENT '所属系统术语组',
  `term` varchar(128) NOT NULL COMMENT '标准术语',
  `definition` text NOT NULL COMMENT '释义',
  `enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort_weight` int(11) NOT NULL DEFAULT 0 COMMENT '匹配排序权重',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `cloned_from_system_id` int(11) DEFAULT NULL COMMENT '从系统默认复制时记录源词条 id',
  PRIMARY KEY (`id`),
  KEY `idx_owner_enabled` (`owner_user_id`,`enabled`),
  KEY `idx_owner_clone` (`owner_user_id`,`cloned_from_system_id`),
  KEY `idx_terminology_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `terminology_alias` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `terminology_id` int(11) NOT NULL,
  `alias` varchar(128) NOT NULL,
  `target_lang` varchar(32) DEFAULT NULL COMMENT '翻译目标语言代码，空=任意',
  PRIMARY KEY (`id`),
  KEY `idx_terminology_id` (`terminology_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `terminology_group` (`id`, `name`, `code`, `sort_weight`, `enabled`, `create_time`, `update_time`)
VALUES (1, '医疗行业', 'medical', 50, 1, NOW(), NOW());

INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`)
VALUES (1, NULL, NULL, 'WaiChat', '本项目即时通讯与协作系统名称。', 1, 100, NOW(), NOW());
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`)
VALUES
(2, NULL, 1, 'CT', '计算机断层扫描（Computed Tomography）。', 1, 40, NOW(), NOW()),
(3, NULL, 1, 'MRI', '磁共振成像（Magnetic Resonance Imaging）。', 1, 40, NOW(), NOW()),
(4, NULL, 1, 'ICU', '重症监护病房（Intensive Care Unit）。', 1, 30, NOW(), NOW());

INSERT INTO `terminology_alias` (`terminology_id`, `alias`, `target_lang`) VALUES (1, '外聊', NULL);
INSERT INTO `terminology_alias` (`terminology_id`, `alias`, `target_lang`) VALUES (2, '电子计算机断层扫描', NULL);
