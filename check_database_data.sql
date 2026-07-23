-- 检查数据库中的数据是否存在
-- 用于排查管理员数据大屏显示为空的问题

-- 1. 检查活动数量
SELECT '活动总数' as 检查项, COUNT(*) as 数量 FROM activity;

-- 2. 检查活动类型
SELECT '活动类型数量' as 检查项, COUNT(*) as 数量 FROM activity_category;
SELECT '各类型活动数量' as 检查项, ac.name as 类型名称, COUNT(a.id) as 活动数量
FROM activity_category ac
LEFT JOIN activity a ON ac.id = a.category_id
GROUP BY ac.id, ac.name;

-- 3. 检查签退记录
SELECT '签退记录总数' as 检查项, COUNT(*) as 数量 FROM activity_checkin WHERE checkout_time IS NOT NULL;
SELECT '签退记录志愿时长' as 检查项, COALESCE(SUM(service_hours), 0) as 总时长 FROM activity_checkin WHERE checkout_time IS NOT NULL;

-- 4. 检查补录记录
SELECT '补录记录总数' as 检查项, COUNT(*) as 数量 FROM service_hour_replenish WHERE status = '已通过';
SELECT '补录记录总时长' as 检查项, COALESCE(SUM(hours), 0) as 总时长 FROM service_hour_replenish WHERE status = '已通过';

-- 5. 检查志愿者
SELECT '志愿者总数' as 检查项, COUNT(*) as 数量 FROM volunteer;
SELECT '志愿者总积分' as 检查项, COALESCE(SUM(points), 0) as 总积分 FROM volunteer;

-- 6. 检查证书
SELECT '证书总数' as 检查项, COUNT(*) as 数量 FROM certificate_library;

-- 7. 检查活动申请（用于统计参与人数）
SELECT '活动申请总数' as 检查项, COUNT(*) as 数量 FROM activity_application;
SELECT '已通过申请数' as 检查项, COUNT(*) as 数量 FROM activity_application WHERE status = '已通过';

-- 8. 检查论坛帖子
SELECT '论坛帖子总数' as 检查项, COUNT(*) as 数量 FROM forum_post;
SELECT '论坛分类数量' as 检查项, COUNT(*) as 数量 FROM forum_category;
SELECT '各分类帖子数量' as 检查项, fc.name as 分类名称, COUNT(fp.id) as 帖子数量
FROM forum_category fc
LEFT JOIN forum_post fp ON fc.id = fp.category_id
GROUP BY fc.id, fc.name;

-- 9. 检查志愿者地址
SELECT '志愿者地址总数' as 检查项, COUNT(*) as 数量 FROM volunteer_address;

-- 10. 检查最近6个月的数据
SELECT '最近6个月签退记录' as 检查项, 
       DATE_FORMAT(checkout_time, '%Y-%m') as 月份,
       COUNT(*) as 记录数,
       COALESCE(SUM(service_hours), 0) as 总时长
FROM activity_checkin 
WHERE checkout_time IS NOT NULL 
  AND checkout_time >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
GROUP BY DATE_FORMAT(checkout_time, '%Y-%m')
ORDER BY 月份;

SELECT '最近6个月补录记录' as 检查项,
       DATE_FORMAT(create_time, '%Y-%m') as 月份,
       COUNT(*) as 记录数,
       COALESCE(SUM(hours), 0) as 总时长
FROM service_hour_replenish
WHERE status = '已通过'
  AND create_time >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
GROUP BY DATE_FORMAT(create_time, '%Y-%m')
ORDER BY 月份;

-- 11. 检查积分排行榜数据
SELECT '积分前10名志愿者' as 检查项, real_name as 姓名, points as 积分
FROM volunteer
ORDER BY points DESC
LIMIT 10;

-- 12. 总结
SELECT '=== 数据检查总结 ===' as 说明;
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM activity) = 0 THEN '❌ 缺少活动数据'
        ELSE '✅ 有活动数据'
    END as 活动,
    CASE 
        WHEN (SELECT COUNT(*) FROM activity_checkin WHERE checkout_time IS NOT NULL) = 0 THEN '❌ 缺少签退记录'
        ELSE '✅ 有签退记录'
    END as 签退记录,
    CASE 
        WHEN (SELECT COUNT(*) FROM volunteer) = 0 THEN '❌ 缺少志愿者数据'
        ELSE '✅ 有志愿者数据'
    END as 志愿者,
    CASE 
        WHEN (SELECT COUNT(*) FROM forum_post) = 0 THEN '❌ 缺少论坛帖子'
        ELSE '✅ 有论坛帖子'
    END as 论坛帖子;
