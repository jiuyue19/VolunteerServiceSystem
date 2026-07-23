-- 检查活动ID=13的数据
USE volunteer_system;

SELECT '========== 活动ID=13的完整数据 ==========' AS '';

SELECT 
    id AS '活动ID',
    title AS '标题',
    start_time AS '开始时间',
    end_time AS '结束时间',
    target_number AS '目标人数',
    current_number AS '当前人数',
    address AS '地址',
    detailed_address AS '详细地址',
    volunteer_field AS '志愿领域',
    volunteer_target AS '志愿对象',
    service_location AS '志愿场所',
    organization_name AS '组织名称',
    contact_person AS '联系人',
    contact_phone AS '联系电话',
    content AS '活动内容'
FROM activity 
WHERE id = 13;

-- 检查字段是否为空
SELECT '========== 空字段检查 ==========' AS '';

SELECT 
    CASE WHEN start_time IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '开始时间',
    CASE WHEN end_time IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '结束时间',
    CASE WHEN target_number IS NULL OR target_number = 0 THEN '❌ 空' ELSE '✅ 有值' END AS '目标人数',
    CASE WHEN address IS NULL AND detailed_address IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '地址',
    CASE WHEN volunteer_field IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '志愿领域',
    CASE WHEN volunteer_target IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '志愿对象',
    CASE WHEN service_location IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '志愿场所',
    CASE WHEN content IS NULL THEN '❌ 空' ELSE '✅ 有值' END AS '活动内容'
FROM activity 
WHERE id = 13;

-- 如果活动不存在
SELECT '========== 活动是否存在 ==========' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '❌ 活动ID=13不存在！'
        ELSE CONCAT('✅ 活动存在，标题：', MAX(title))
    END AS '检查结果'
FROM activity 
WHERE id = 13;
