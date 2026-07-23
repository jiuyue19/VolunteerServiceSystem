-- 检查场所图片数据
USE volunteer_system;

-- 查看所有活动的场所图片数据
SELECT 
    id,
    title,
    CASE 
        WHEN venue_image IS NULL THEN '❌ NULL'
        WHEN venue_image = '' THEN '❌ 空字符串'
        WHEN venue_image = '[]' THEN '⚠️ 空数组'
        WHEN venue_image LIKE '[%' THEN '✅ JSON数组'
        WHEN venue_image LIKE 'data:image%' THEN '⚠️ 单个Base64'
        WHEN venue_image LIKE 'http%' THEN '⚠️ 单个URL'
        ELSE '❓ 未知格式'
    END AS '数据类型',
    LENGTH(venue_image) AS '数据长度',
    LEFT(venue_image, 100) AS '数据预览'
FROM activity
ORDER BY id;

-- 统计各种数据格式的数量
SELECT 
    CASE 
        WHEN venue_image IS NULL THEN 'NULL'
        WHEN venue_image = '' THEN '空字符串'
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '[%' THEN 'JSON数组'
        WHEN venue_image LIKE 'data:image%' THEN '单个Base64'
        WHEN venue_image LIKE 'http%' THEN '单个URL'
        ELSE '未知格式'
    END AS '数据类型',
    COUNT(*) AS '数量'
FROM activity
GROUP BY 
    CASE 
        WHEN venue_image IS NULL THEN 'NULL'
        WHEN venue_image = '' THEN '空字符串'
        WHEN venue_image = '[]' THEN '空数组'
        WHEN venue_image LIKE '[%' THEN 'JSON数组'
        WHEN venue_image LIKE 'data:image%' THEN '单个Base64'
        WHEN venue_image LIKE 'http%' THEN '单个URL'
        ELSE '未知格式'
    END;

-- 查看JSON数组格式的详细信息
SELECT 
    id,
    title,
    venue_image,
    JSON_LENGTH(venue_image) AS '图片数量'
FROM activity
WHERE venue_image LIKE '[%'
  AND JSON_VALID(venue_image)
ORDER BY id;

-- 检查无效的JSON数据
SELECT 
    id,
    title,
    venue_image AS '无效的JSON数据'
FROM activity
WHERE venue_image IS NOT NULL 
  AND venue_image != ''
  AND venue_image != '[]'
  AND venue_image LIKE '[%'
  AND NOT JSON_VALID(venue_image);
