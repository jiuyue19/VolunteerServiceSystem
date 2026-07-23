-- 更新所有活动时间到2026年1-6月
-- 执行日期：2025-12-05
-- 说明：将现有活动平均分配到2026年1-6月，每个月都有活动

USE volunteer_system;

-- 查看更新前的活动时间
SELECT id, title, start_time, end_time FROM activity ORDER BY id;

-- 更新活动时间到2026年1-6月
-- 根据活动ID分配到不同月份，确保每个月都有活动

-- 1月活动
UPDATE activity 
SET start_time = '2026-01-10 09:00:00',
    end_time = '2026-01-10 17:00:00'
WHERE id % 6 = 1;

-- 2月活动  
UPDATE activity 
SET start_time = '2026-02-15 09:00:00',
    end_time = '2026-02-15 17:00:00'
WHERE id % 6 = 2;

-- 3月活动
UPDATE activity 
SET start_time = '2026-03-20 09:00:00',
    end_time = '2026-03-20 17:00:00'
WHERE id % 6 = 3;

-- 4月活动
UPDATE activity 
SET start_time = '2026-04-18 09:00:00',
    end_time = '2026-04-18 17:00:00'
WHERE id % 6 = 4;

-- 5月活动
UPDATE activity 
SET start_time = '2026-05-16 09:00:00',
    end_time = '2026-05-16 17:00:00'
WHERE id % 6 = 5;

-- 6月活动
UPDATE activity 
SET start_time = '2026-06-21 09:00:00',
    end_time = '2026-06-21 17:00:00'
WHERE id % 6 = 0;

-- 查看更新后的结果
SELECT 
    id, 
    title, 
    DATE_FORMAT(start_time, '%Y年%m月%d日 %H:%i') AS '开始时间',
    DATE_FORMAT(end_time, '%Y年%m月%d日 %H:%i') AS '结束时间',
    MONTH(start_time) AS '月份'
FROM activity 
ORDER BY start_time, id;

-- 按月份统计活动数量
SELECT 
    MONTH(start_time) AS '月份',
    COUNT(*) AS '活动数量',
    GROUP_CONCAT(title SEPARATOR ', ') AS '活动列表'
FROM activity
GROUP BY MONTH(start_time)
ORDER BY MONTH(start_time);

SELECT '活动时间已更新到2026年1-6月！' AS message;
