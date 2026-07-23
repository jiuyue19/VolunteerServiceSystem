-- 为现有活动填充默认数据
-- 执行日期：2025-12-04
-- 说明：为现有活动的空字段填充默认值，确保详情页正常显示

USE volunteer_system;

-- 查看当前活动数据情况
SELECT '========== 当前活动数据检查 ==========' AS message;

SELECT 
    id,
    title,
    start_time,
    end_time,
    target_number,
    current_number,
    detailed_address,
    volunteer_field,
    volunteer_target,
    service_location
FROM activity;

-- ============================================
-- 开始更新数据
-- ============================================

SELECT '========== 开始填充默认值 ==========' AS message;

-- 1. 为缺少详细地址的活动，使用基本地址
UPDATE activity 
SET detailed_address = COALESCE(address, '待补充')
WHERE detailed_address IS NULL OR detailed_address = '';

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的详细地址') AS message;

-- 2. 为缺少志愿领域的活动，根据分类ID设置默认值
UPDATE activity 
SET volunteer_field = CASE category_id
    WHEN 1 THEN '社区服务'
    WHEN 2 THEN '环境保护'
    WHEN 3 THEN '支教助学'
    WHEN 4 THEN '交通引导'
    WHEN 5 THEN '禁毒宣传'
    ELSE '社区服务'
END
WHERE volunteer_field IS NULL OR volunteer_field = '';

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的志愿领域') AS message;

-- 3. 为缺少志愿对象的活动设置默认值
UPDATE activity 
SET volunteer_target = '全体志愿者'
WHERE volunteer_target IS NULL OR volunteer_target = '';

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的志愿对象') AS message;

-- 4. 为缺少志愿场所的活动设置默认值
UPDATE activity 
SET service_location = '社区'
WHERE service_location IS NULL OR service_location = '';

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的志愿场所') AS message;

-- 5. 确保目标人数不为0
UPDATE activity 
SET target_number = 50
WHERE target_number IS NULL OR target_number = 0;

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的目标人数') AS message;

-- 6. 确保当前人数不为NULL
UPDATE activity 
SET current_number = 0
WHERE current_number IS NULL;

SELECT CONCAT('更新了 ', ROW_COUNT(), ' 条活动的当前人数') AS message;

-- ============================================
-- 查看更新后的结果
-- ============================================

SELECT '========== 更新后的数据 ==========' AS message;

SELECT 
    id AS '活动ID',
    title AS '活动标题',
    DATE_FORMAT(start_time, '%Y-%m-%d %H:%i') AS '开始时间',
    DATE_FORMAT(end_time, '%Y-%m-%d %H:%i') AS '结束时间',
    CONCAT(current_number, '/', target_number) AS '报名人数',
    COALESCE(detailed_address, address, '未设置') AS '地址',
    volunteer_field AS '志愿领域',
    volunteer_target AS '志愿对象',
    service_location AS '志愿场所',
    organization_name AS '组织名称',
    contact_person AS '联系人',
    contact_phone AS '联系电话'
FROM activity
ORDER BY create_time DESC;

SELECT '========== 数据更新完成！==========' AS message;
SELECT '提示：如需修改具体活动信息，请登录管理后台进行编辑' AS tip;
