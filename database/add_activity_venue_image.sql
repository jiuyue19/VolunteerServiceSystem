-- 为活动表添加场所图片字段
-- 执行日期：2025-12-04
-- 说明：添加场所图片字段用于展示活动地点图片

USE volunteer_system;

-- 添加场所图片字段
ALTER TABLE activity
ADD COLUMN venue_image LONGTEXT COMMENT '场所图片(Base64编码或URL)' AFTER cover_image;

-- 查看更新后的表结构
DESC activity;

SELECT 'Activity表场所图片字段添加完成！' AS message;
