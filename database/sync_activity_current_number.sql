-- 同步活动当前人数与实际审核通过的志愿者数量
-- 执行日期：2025-12-05
-- 说明：将activity表的current_number字段更新为实际状态为"待参与"的申请数量

USE volunteer_system;

-- 步骤1：先将所有活动的当前人数清零（如果还没有清零）
UPDATE activity SET current_number = 0;

-- 步骤2：根据实际"待参与"状态的申请数量更新current_number
UPDATE activity a
SET current_number = (
    SELECT COUNT(*) 
    FROM activity_application aa 
    WHERE aa.activity_id = a.id 
    AND aa.status = '待参与'
);

-- 步骤3：查看更新后的结果
SELECT 
    a.id,
    a.title,
    a.current_number AS '数据库当前人数',
    a.target_number AS '目标人数',
    (SELECT COUNT(*) FROM activity_application WHERE activity_id = a.id AND status = '待参与') AS '实际待参与人数'
FROM activity a
ORDER BY a.id;

-- 步骤4：验证数据一致性
SELECT 
    '数据一致性检查' AS message,
    COUNT(*) AS '不一致的活动数量'
FROM activity a
WHERE a.current_number != (
    SELECT COUNT(*) 
    FROM activity_application aa 
    WHERE aa.activity_id = a.id 
    AND aa.status = '待参与'
);

SELECT 'Activity表当前人数同步完成！所有活动的current_number已与"待参与"状态的申请数量保持一致。' AS result;
