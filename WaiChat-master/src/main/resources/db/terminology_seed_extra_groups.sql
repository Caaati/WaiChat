-- 追加多组「系统术语组」及组内系统词条（owner_user_id 为 NULL）
-- 前提：已存在 terminology_group.id=1（医疗）及 terminology.id 1–4；本脚本使用组 id 2–5、词条 id 5–14。
-- 若 id 冲突，请修改下方数字或先 SELECT MAX(id)。

SET NAMES utf8mb4;

INSERT INTO `terminology_group` (`id`, `name`, `code`, `sort_weight`, `enabled`, `create_time`, `update_time`) VALUES
(2, '金融科技', 'fintech', 48, 1, NOW(), NOW()),
(3, '电商与物流', 'ecommerce_logistics', 46, 1, NOW(), NOW()),
(4, '法律与合规', 'legal', 44, 1, NOW(), NOW()),
(5, '教育与国际考试', 'education', 42, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `code` = VALUES(`code`),
  `sort_weight` = VALUES(`sort_weight`),
  `enabled` = VALUES(`enabled`),
  `update_time` = VALUES(`update_time`);

INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`) VALUES
(5, NULL, 2, 'KYC', '了解你的客户（Know Your Customer），金融机构客户身份识别与尽职调查制度。', 1, 35, NOW(), NOW()),
(6, NULL, 2, 'AML', '反洗钱（Anti-Money Laundering）。', 1, 35, NOW(), NOW()),
(7, NULL, 3, 'SKU', '库存量单位（Stock Keeping Unit），最小可管理商品编码。', 1, 35, NOW(), NOW()),
(8, NULL, 3, 'TMS', '运输管理系统（Transportation Management System）。', 1, 35, NOW(), NOW()),
(9, NULL, 4, 'GDPR', '欧盟《通用数据保护条例》（General Data Protection Regulation）。', 1, 35, NOW(), NOW()),
(10, NULL, 4, 'NDA', '保密协议（Non-Disclosure Agreement）。', 1, 35, NOW(), NOW()),
(11, NULL, 5, 'IELTS', '国际英语语言测试系统（International English Language Testing System）。', 1, 35, NOW(), NOW()),
(12, NULL, 5, 'TOEFL', '检定非英语为母语者的英语能力考试（Test of English as a Foreign Language）。', 1, 35, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `group_id` = VALUES(`group_id`),
  `term` = VALUES(`term`),
  `definition` = VALUES(`definition`),
  `enabled` = VALUES(`enabled`),
  `sort_weight` = VALUES(`sort_weight`),
  `update_time` = VALUES(`update_time`);

-- 别名（重复执行可能重复插入，无唯一约束时请只执行一次或先按 terminology_id 清理）
INSERT INTO `terminology_alias` (`terminology_id`, `alias`) VALUES
(5, '客户尽职调查'),
(7, '单品条码');

-- 若库中已有更大 id，请改为 MAX(id)+1 或删除下列两行
-- ALTER TABLE `terminology` AUTO_INCREMENT = 13;
-- ALTER TABLE `terminology_group` AUTO_INCREMENT = 6;
