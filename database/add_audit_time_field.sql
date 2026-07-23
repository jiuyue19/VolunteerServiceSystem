-- 为activity_application表添加审核时间字段
-- 执行日期：2025-12-05
-- 说明：添加audit_time字段用于记录管理员审核报名申请的时间

USE volunteer_system;

-- 检查字段是否已存在
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'volunteer_system' 
  AND TABLE_NAME = 'activity_application'
  AND COLUMN_NAME = 'audit_time';

-- 添加审核时间字段（MySQL 5.7不支持IF NOT EXISTS，需要先检查）
-- 如果字段已存在会报错，可以忽略
ALTER TABLE activity_application 
ADD COLUMN audit_time DATETIME NULL COMMENT '审核时间' 
AFTER apply_time;

-- 为已审核的申请设置审核时间（使用apply_time作为默认值）
UPDATE activity_application 
SET audit_time = apply_time 
WHERE status IN ('待参与', '已拒绝', '已签到', '已签退') 
  AND audit_time IS NULL;

-- 查看更新后的表结构
DESCRIBE activity_application;

-- 查看示例数据
SELECT 
    id,
    activity_id,
    volunteer_id,
    status,
    apply_time AS '申请时间',
    audit_time AS '审核时间',
    reject_reason AS '拒绝原因'
FROM activity_application
ORDER BY id DESC
LIMIT 10;

SELECT '审核时间字段添加完成！' AS message;
