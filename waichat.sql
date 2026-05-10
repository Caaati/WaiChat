/*
 Navicat Premium Dump SQL

 Source Server         : 124.220.61.45
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : 124.220.61.45:3306
 Source Schema         : waichat

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 10/05/2026 22:33:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat
-- ----------------------------
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `target_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '聊天目标用户Id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '聊天内容',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息类型: \"TEXT\" 或 \"VOICE\"',
  `audio_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '音频url',
  `duration` int(3) NULL DEFAULT NULL COMMENT '音频时长(秒)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '显示 0/1',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_target_create`(`user_id`, `target_id`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 180 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for language
-- ----------------------------
DROP TABLE IF EXISTS `language`;
CREATE TABLE `language`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `english_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `chinese_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `nickname` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for terminology_group
-- ----------------------------
DROP TABLE IF EXISTS `terminology_alias`;
DROP TABLE IF EXISTS `terminology`;
DROP TABLE IF EXISTS `terminology_group`;
CREATE TABLE `terminology_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组展示名',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '可选唯一键',
  `sort_weight` int(11) NOT NULL DEFAULT 0,
  `enabled` tinyint(4) NOT NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_enabled`(`enabled`, `sort_weight`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for terminology
-- ----------------------------
CREATE TABLE `terminology`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_user_id` int(11) NULL DEFAULT NULL COMMENT 'NULL=系统默认术语库，非空=用户自定义',
  `group_id` int(11) NULL DEFAULT NULL COMMENT '所属系统术语组，NULL=无组',
  `term` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标准术语',
  `definition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '释义',
  `enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort_weight` int(11) NOT NULL DEFAULT 0 COMMENT '匹配排序权重，越大越优先',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `cloned_from_system_id` int(11) NULL DEFAULT NULL COMMENT '从系统默认复制到「我的术语」时记录源词条 id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_enabled`(`owner_user_id`, `enabled`) USING BTREE,
  INDEX `idx_owner_clone`(`owner_user_id`, `cloned_from_system_id`) USING BTREE,
  INDEX `idx_terminology_group`(`group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for terminology_alias
-- ----------------------------
CREATE TABLE `terminology_alias`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `terminology_id` int(11) NOT NULL,
  `alias` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_terminology_id`(`terminology_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- 系统术语组 + 示例（可按需维护）
INSERT INTO `terminology_group` (`id`, `name`, `code`, `sort_weight`, `enabled`, `create_time`, `update_time`)
VALUES (1, '医疗行业', 'medical', 50, 1, NOW(), NOW());

INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`)
VALUES (1, NULL, NULL, 'WaiChat', '本项目即时通讯与协作系统名称。', 1, 100, NOW(), NOW());
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`)
VALUES
(2, NULL, 1, 'CT', '计算机断层扫描（Computed Tomography）。', 1, 40, NOW(), NOW()),
(3, NULL, 1, 'MRI', '磁共振成像（Magnetic Resonance Imaging）。', 1, 40, NOW(), NOW()),
(4, NULL, 1, 'ICU', '重症监护病房（Intensive Care Unit）。', 1, 30, NOW(), NOW());

INSERT INTO `terminology_alias` (`terminology_id`, `alias`) VALUES (1, '外聊');
INSERT INTO `terminology_alias` (`terminology_id`, `alias`) VALUES (2, '电子计算机断层扫描');

SET FOREIGN_KEY_CHECKS = 1;
