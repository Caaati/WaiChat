-- 已有 terminology 表但无「从系统复制」字段时执行一次（勿重复执行索引部分）
SET NAMES utf8mb4;

ALTER TABLE `terminology`
  ADD COLUMN `cloned_from_system_id` int(11) NULL DEFAULT NULL COMMENT '从系统默认复制到「我的术语」时记录源词条 id' AFTER `update_time`,
  ADD INDEX `idx_owner_clone` (`owner_user_id`, `cloned_from_system_id`);
