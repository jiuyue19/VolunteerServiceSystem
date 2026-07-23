-- 根据活动起止时间重新计算志愿时长和积分
-- 执行日期：2025-12-05
-- 说明：自动计算所有活动的志愿时长和积分
-- 业务规则：志愿时长 = 结束时间 - 起始时间，积分 = 志愿时长 × 10

USE volunteer_system;

-- 查看更新前的数据
SELECT 
    id,
    title,
    start_time AS '开始时间',
    end_time AS '结束时间',
    service_hours AS '当前志愿时长',
    reward_points AS '当前积分',
    ROUND(TIMESTAMPDIFF(MINUTE, start_time, end_time) / 60, 2) AS '计算后的时长',
    ROUND(TIMESTAMPDIFF(MINUTE, start_time, end_time) / 60 * 10) AS '计算后的积分'
FROM activity
WHERE start_time IS NOT NULL AND end_time IS NOT NULL
ORDER BY id;

-- 更新所有活动的志愿时长和积分
UPDATE activity
SET 
    service_hours = ROUND(TIMESTAMPDIFF(MINUTE, start_time, end_time) / 60, 2),
    reward_points = ROUND(TIMESTAMPDIFF(MINUTE, start_time, end_time) / 60 * 10)
WHERE start_time IS NOT NULL 
  AND end_time IS NOT NULL
  AND end_time > start_time;

-- 查看更新后的数据
SELECT 
    id,
    title,
    DATE_FORMAT(start_time, '%Y-%m-%d %H:%i') AS '开始时间',
    DATE_FORMAT(end_time, '%Y-%m-%d %H:%i') AS '结束时间',
    service_hours AS '志愿时长(小时)',
    reward_points AS '积分',
    CONCAT(FLOOR(service_hours), '小时', ROUND((service_hours - FLOOR(service_hours)) * 60), '分钟') AS '时长说明'
FROM activity
WHERE start_time IS NOT NULL AND end_time IS NOT NULL
ORDER BY start_time, id;

-- 统计信息
SELECT 
    '数据统计' AS '类型',
    COUNT(*) AS '活动总数',
    CONCAT(MIN(service_hours), ' - ', MAX(service_hours), ' 小时') AS '时长范围',
    CONCAT(MIN(reward_points), ' - ', MAX(reward_points), ' 积分') AS '积分范围',
    ROUND(AVG(service_hours), 2) AS '平均时长',
    ROUND(AVG(reward_points), 2) AS '平均积分'
FROM activity
WHERE start_time IS NOT NULL AND end_time IS NOT NULL;

-- 检查异常数据（结束时间早于开始时间）
SELECT 
    id,
    title,
    start_time AS '开始时间',
    end_time AS '结束时间',
    '结束时间早于开始时间' AS '异常说明'
FROM activity
WHERE end_time <= start_time;

-- 检查缺失时间数据的活动
SELECT 
    id,
    title,
    start_time AS '开始时间',
    end_time AS '结束时间',
    '缺少时间数据' AS '异常说明'
FROM activity
WHERE start_time IS NULL OR end_time IS NULL;

SELECT '活动志愿时长和积分重新计算完成！' AS message;
