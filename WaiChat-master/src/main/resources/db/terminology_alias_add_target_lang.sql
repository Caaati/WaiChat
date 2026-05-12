-- 已有 terminology_alias 表时执行一次（勿重复执行）
ALTER TABLE `terminology_alias`
  ADD COLUMN `target_lang` varchar(32) NULL DEFAULT NULL COMMENT '翻译目标语言代码，与 /ai/translate 的 target 一致；NULL 表示任意目标语言' AFTER `alias`;
