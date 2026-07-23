-- 快速修复活动封面图片字段
-- 执行日期：2025-12-04
-- 说明：立即修复cover_image字段以解决当前报错

USE volunteer_system;

ALTER TABLE activity
MODIFY COLUMN cover_image LONGTEXT COMMENT '封面图片(Base64编码或URL)';

SELECT 'Cover_image字段修复完成！现在可以存储Base64编码的大图片了。' AS message;
