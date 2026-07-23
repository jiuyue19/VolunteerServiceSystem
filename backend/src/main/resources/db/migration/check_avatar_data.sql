-- 检查volunteer表结构和avatar数据
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'volunteer' AND COLUMN_NAME = 'avatar';

-- 检查是否有avatar数据
SELECT id, username, avatar FROM volunteer WHERE avatar IS NOT NULL LIMIT 5;

-- 检查comment表是否有新字段
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'comment' AND COLUMN_NAME IN ('likes', 'liked_by');
