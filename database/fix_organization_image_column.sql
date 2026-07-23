-- 修复 organization_image 字段数据类型
-- 执行日期：2025-12-12
-- 说明：将 organization_image 字段从 VARCHAR 修改为 LONGTEXT 以支持 Base64 编码的大图片

USE volunteer_system;

-- 修改 activity 表的 organization_image 字段
ALTER TABLE activity
MODIFY COLUMN organization_image LONGTEXT COMMENT '组织图片(Base64编码或URL)';

-- 验证修改结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'volunteer_system'
    AND TABLE_NAME = 'activity'
    AND COLUMN_NAME = 'organization_image';

SELECT 'organization_image 字段类型修复完成！' AS message;
