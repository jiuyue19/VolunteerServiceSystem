-- 修复所有图片字段数据类型
-- 执行日期：2025-12-04
-- 说明：将图片字段从VARCHAR(255)修改为LONGTEXT以支持Base64编码的大图片

USE volunteer_system;

-- 1. 修改活动表的封面图片字段
ALTER TABLE activity
MODIFY COLUMN cover_image LONGTEXT COMMENT '封面图片(Base64编码或URL)';

-- 2. 修改轮播图表的图片字段
ALTER TABLE carousel
MODIFY COLUMN image_url LONGTEXT COMMENT '图片(Base64编码或URL)';

-- 3. 修改管理员表的头像字段
ALTER TABLE admin
MODIFY COLUMN avatar LONGTEXT COMMENT '头像(Base64编码或URL)';

-- 4. 检查并修改organization_image字段（如果存在）
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'volunteer_system' 
    AND TABLE_NAME = 'activity' 
    AND COLUMN_NAME = 'organization_image'
);

SET @sql = IF(@col_exists > 0,
    'ALTER TABLE activity MODIFY COLUMN organization_image LONGTEXT COMMENT ''组织图片(Base64编码或URL)''',
    'SELECT ''organization_image column does not exist, skipping'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 查看更新后的表结构
SELECT '=== Activity表结构 ===' AS message;
DESC activity;

SELECT '=== Carousel表结构 ===' AS message;
DESC carousel;

SELECT '=== Admin表结构 ===' AS message;
DESC admin;

SELECT '所有图片字段类型修复完成！' AS final_message;
