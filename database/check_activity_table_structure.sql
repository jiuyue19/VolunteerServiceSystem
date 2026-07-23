-- 检查activity表的完整结构
USE volunteer_system;

-- 查看表结构
DESC activity;

-- 查看所有列的详细信息
SELECT 
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    CHARACTER_MAXIMUM_LENGTH AS '最大长度',
    IS_NULLABLE AS '可为空',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'volunteer_system' 
AND TABLE_NAME = 'activity'
ORDER BY ORDINAL_POSITION;

-- 查看示例数据
SELECT 
    id,
    title,
    address,
    detailed_address,
    start_time,
    end_time,
    target_number,
    current_number,
    volunteer_field,
    volunteer_target,
    service_location,
    organization_name,
    contact_person,
    contact_phone
FROM activity 
LIMIT 3;
