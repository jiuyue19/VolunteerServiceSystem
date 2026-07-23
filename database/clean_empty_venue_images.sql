-- 清理场所图片中的空字符串数据
-- 执行日期：2025-12-05
-- 说明：将包含空字符串的场所图片数据清理为空数组

USE volunteer_system;

-- 查看需要清理的数据
SELECT 
    id,
    title,
    venue_image,
    CASE 
        WHEN venue_image LIKE '%""%' THEN '✅ 需要清理'
        ELSE '⭕ 无需清理'
    END AS '状态'
FROM activity
WHERE venue_image IS NOT NULL;

-- 备份原始数据（可选）
-- CREATE TABLE activity_backup_venue_image AS
-- SELECT id, venue_image FROM activity;

-- 清理空字符串的场所图片数据
-- 情况1: ["", "", ""] 这种包含空字符串的数组
UPDATE activity
SET venue_image = '[]'
WHERE venue_image LIKE '%""%';

-- 情况2: 完全为空的数据
UPDATE activity
SET venue_image = '[]'
WHERE venue_image IS NULL OR venue_image = '';

-- 验证清理结果
SELECT 
    id,
    title,
    venue_image,
    CASE 
        WHEN venue_image = '[]' THEN '✅ 空数组（正常）'
        WHEN venue_image LIKE '[%' AND JSON_VALID(venue_image) THEN '✅ 有效JSON数组'
        ELSE '⚠️ 需要检查'
    END AS '状态'
FROM activity
ORDER BY id;

-- 统计清理结果
SELECT 
    CASE 
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '[%' AND JSON_VALID(venue_image) AND JSON_LENGTH(venue_image) > 0 THEN '有图片'
        ELSE '其他'
    END AS '类型',
    COUNT(*) AS '数量'
FROM activity
GROUP BY 
    CASE 
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '[%' AND JSON_VALID(venue_image) AND JSON_LENGTH(venue_image) > 0 THEN '有图片'
        ELSE '其他'
    END;

SELECT '场所图片数据清理完成！' AS message;
