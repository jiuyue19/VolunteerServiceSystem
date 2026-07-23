-- 修复志愿者表avatar字段类型
-- 执行日期：2025-12-04
-- 问题：avatar字段为VARCHAR类型，无法存储Base64编码的图片数据
-- 解决：将avatar字段修改为LONGTEXT类型

USE volunteer_system;

-- 1. 检查当前avatar字段的定义
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE,
    IS_NULLABLE, 
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'volunteer_system' 
  AND TABLE_NAME = 'volunteer' 
  AND COLUMN_NAME = 'avatar';

-- 2. 如果字段不存在，则添加字段（如果已存在则跳过）
-- 注意：如果执行时字段已存在，这条语句会报错，可以忽略
-- ALTER TABLE volunteer
-- ADD COLUMN avatar VARCHAR(255) COMMENT '头像(Base64编码或URL)' AFTER id_card;

-- 3. 修改avatar字段类型为LONGTEXT以支持Base64图片存储
ALTER TABLE volunteer
MODIFY COLUMN avatar LONGTEXT COMMENT '头像(Base64编码或URL)';

-- 4. 同时修复admin表的avatar字段（如果也是VARCHAR类型）
ALTER TABLE admin
MODIFY COLUMN avatar LONGTEXT COMMENT '头像(Base64编码或URL)';

-- 5. 查看修改后的字段定义
SELECT 
    TABLE_NAME,
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'volunteer_system' 
  AND TABLE_NAME IN ('volunteer', 'admin')
  AND COLUMN_NAME = 'avatar';

SELECT 'Avatar字段类型修复完成！现在可以存储Base64编码的图片数据。' AS message;
