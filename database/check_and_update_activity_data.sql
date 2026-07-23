-- 检查并更新活动表数据
-- 执行日期：2025-12-04
-- 说明：检查现有活动数据完整性，并提供更新建议

USE volunteer_system;

-- ============================================
-- 第一步：检查现有数据
-- ============================================

SELECT '========== 检查活动表数据完整性 ==========' AS message;

-- 统计总活动数
SELECT COUNT(*) AS '总活动数' FROM activity;

-- 检查关键字段为空的活动数量
SELECT 
    COUNT(*) AS '总数',
    SUM(CASE WHEN start_time IS NULL THEN 1 ELSE 0 END) AS '缺少活动日期',
    SUM(CASE WHEN target_number IS NULL OR target_number = 0 THEN 1 ELSE 0 END) AS '缺少招募人数',
    SUM(CASE WHEN detailed_address IS NULL AND address IS NULL THEN 1 ELSE 0 END) AS '缺少地址',
    SUM(CASE WHEN volunteer_field IS NULL OR volunteer_field = '' THEN 1 ELSE 0 END) AS '缺少志愿领域',
    SUM(CASE WHEN volunteer_target IS NULL OR volunteer_target = '' THEN 1 ELSE 0 END) AS '缺少志愿对象',
    SUM(CASE WHEN service_location IS NULL OR service_location = '' THEN 1 ELSE 0 END) AS '缺少志愿场所'
FROM activity;

-- 查看具体的空数据活动
SELECT 
    id AS '活动ID',
    title AS '活动标题',
    CASE WHEN start_time IS NULL THEN '缺失' ELSE '正常' END AS '活动日期',
    CASE WHEN target_number IS NULL OR target_number = 0 THEN '缺失' ELSE target_number END AS '招募人数',
    CASE WHEN detailed_address IS NULL AND address IS NULL THEN '缺失' ELSE '正常' END AS '地址',
    CASE WHEN volunteer_field IS NULL OR volunteer_field = '' THEN '缺失' ELSE volunteer_field END AS '志愿领域',
    CASE WHEN volunteer_target IS NULL OR volunteer_target = '' THEN '缺失' ELSE volunteer_target END AS '志愿对象',
    CASE WHEN service_location IS NULL OR service_location = '' THEN '缺失' ELSE service_location END AS '志愿场所'
FROM activity
WHERE 
    start_time IS NULL 
    OR target_number IS NULL 
    OR target_number = 0
    OR (detailed_address IS NULL AND address IS NULL)
    OR volunteer_field IS NULL 
    OR volunteer_field = ''
    OR volunteer_target IS NULL 
    OR volunteer_target = ''
    OR service_location IS NULL 
    OR service_location = '';

SELECT '========== 数据检查完成 ==========' AS message;

-- ============================================
-- 第二步：为空字段填充默认值（可选）
-- ============================================

-- 注意：以下SQL仅作为参考，请根据实际情况修改后再执行
-- 取消下面的注释以执行更新

/*
-- 为缺少志愿领域的活动设置默认值
UPDATE activity 
SET volunteer_field = '社区服务' 
WHERE volunteer_field IS NULL OR volunteer_field = '';

-- 为缺少志愿对象的活动设置默认值
UPDATE activity 
SET volunteer_target = '全体志愿者' 
WHERE volunteer_target IS NULL OR volunteer_target = '';

-- 为缺少志愿场所的活动设置默认值
UPDATE activity 
SET service_location = '社区' 
WHERE service_location IS NULL OR service_location = '';

-- 为缺少详细地址的活动，使用基本地址
UPDATE activity 
SET detailed_address = address 
WHERE (detailed_address IS NULL OR detailed_address = '') 
AND address IS NOT NULL AND address != '';

-- 为缺少招募人数的活动设置默认值
UPDATE activity 
SET target_number = 50 
WHERE target_number IS NULL OR target_number = 0;

SELECT '数据更新完成！' AS message;
*/

-- ============================================
-- 第三步：查看更新后的数据
-- ============================================

-- 取消下面的注释查看所有活动的关键信息
/*
SELECT 
    id AS '活动ID',
    title AS '活动标题',
    start_time AS '开始时间',
    end_time AS '结束时间',
    CONCAT(current_number, '/', target_number) AS '报名人数',
    COALESCE(detailed_address, address, '未设置') AS '地址',
    COALESCE(volunteer_field, '未设置') AS '志愿领域',
    COALESCE(volunteer_target, '未设置') AS '志愿对象',
    COALESCE(service_location, '未设置') AS '志愿场所'
FROM activity
ORDER BY create_time DESC;
*/
