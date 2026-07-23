-- 清理场所图片中的blob URL
-- 执行日期：2025-12-05
-- 说明：blob URL是临时的内存引用，刷新页面后就会失效，需要清理

USE volunteer_system;

-- 查看包含blob URL的活动
SELECT 
    id,
    title,
    venue_image,
    CASE 
        WHEN venue_image LIKE '%blob:%' THEN '❌ 包含blob URL（需要清理）'
        WHEN venue_image = '[]' THEN '⭕ 空数组'
        WHEN venue_image LIKE '%data:image%' THEN '✅ 包含base64图片'
        ELSE '❓ 其他格式'
    END AS '状态'
FROM activity
WHERE venue_image IS NOT NULL
ORDER BY id;

-- 统计blob URL数量
SELECT 
    COUNT(*) AS '包含blob URL的活动数量'
FROM activity
WHERE venue_image LIKE '%blob:%';

-- 备份包含blob URL的数据（可选）
-- CREATE TABLE activity_blob_url_backup AS
-- SELECT id, venue_image FROM activity WHERE venue_image LIKE '%blob:%';

-- 清理包含blob URL的场所图片
-- 将包含blob URL的活动的场所图片设置为空数组
UPDATE activity
SET venue_image = '[]'
WHERE venue_image LIKE '%blob:%';

-- 验证清理结果
SELECT 
    id,
    title,
    venue_image,
    CASE 
        WHEN venue_image = '[]' THEN '✅ 已清理为空数组'
        WHEN venue_image LIKE '%blob:%' THEN '❌ 仍包含blob URL'
        WHEN venue_image LIKE '%data:image%' THEN '✅ 包含base64图片'
        ELSE '❓ 其他格式'
    END AS '状态'
FROM activity
ORDER BY id;

-- 最终统计
SELECT 
    CASE 
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '%blob:%' THEN 'blob URL（异常）'
        WHEN venue_image LIKE '%data:image%' THEN 'base64图片（正常）'
        ELSE '其他'
    END AS '数据类型',
    COUNT(*) AS '数量'
FROM activity
GROUP BY 
    CASE 
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '%blob:%' THEN 'blob URL（异常）'
        WHEN venue_image LIKE '%data:image%' THEN 'base64图片（正常）'
        ELSE '其他'
    END;

SELECT '场所图片blob URL清理完成！请重新上传图片。' AS message;
