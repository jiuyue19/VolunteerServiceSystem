-- 格式化活动表的场所图片字段为JSON数组格式
-- 执行日期：2025-12-05
-- 说明：将现有的单张场所图片转换为JSON数组格式，支持多张图片存储

USE volunteer_system;

-- 备份现有数据（可选）
-- CREATE TABLE activity_backup_20251205 AS SELECT * FROM activity;

-- 将现有的单张场所图片数据格式化为JSON数组
UPDATE activity
SET venue_image = CONCAT('["', venue_image, '"]')
WHERE venue_image IS NOT NULL 
  AND venue_image != '' 
  AND venue_image NOT LIKE '[%'
  AND venue_image NOT LIKE '{%';

-- 对于空的或NULL的场所图片，设置为空数组
UPDATE activity
SET venue_image = '[]'
WHERE venue_image IS NULL OR venue_image = '';

-- 查看更新后的结果
SELECT id, title, venue_image
FROM activity
LIMIT 10;

SELECT 'Activity表场所图片字段格式化完成！所有图片已转换为JSON数组格式。' AS message;
