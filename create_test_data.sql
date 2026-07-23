-- ============================================
-- 管理员数据大屏测试数据生成脚本
-- ============================================
-- 用途: 为管理员数据大屏生成足够的测试数据
-- 使用: mysql -u root -p volunteer_system < create_test_data.sql
-- ============================================

-- 设置变量
SET @row = 0;

-- ============================================
-- 1. 添加活动类型（如果不存在）
-- ============================================
INSERT IGNORE INTO activity_category (id, name, description, create_time) VALUES
(1, '社区服务', '社区志愿服务活动', NOW()),
(2, '环境保护', '环保类志愿活动', NOW()),
(3, '支教助学', '教育类志愿活动', NOW()),
(4, '景区引导', '旅游景区引导服务', NOW()),
(5, '禁毒宣传', '禁毒知识宣传活动', NOW());

-- ============================================
-- 2. 添加测试志愿者（如果数量不足10个）
-- ============================================
INSERT INTO volunteer (username, password, real_name, gender, phone, email, points, total_hours, province, city, district, create_time)
SELECT 
    CONCAT('test_volunteer_', FLOOR(RAND() * 10000)),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- 密码: 123456
    CONCAT('测试志愿者', FLOOR(RAND() * 1000)),
    IF(RAND() > 0.5, '男', '女'),
    CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
    CONCAT('test', FLOOR(RAND() * 10000), '@example.com'),
    FLOOR(RAND() * 1500) + 100,  -- 100-1600积分
    FLOOR(RAND() * 80) + 5,      -- 5-85小时
    ELT(FLOOR(RAND() * 5) + 1, '北京', '上海', '广东', '浙江', '江苏'),
    ELT(FLOOR(RAND() * 5) + 1, '北京市', '上海市', '广州市', '杭州市', '南京市'),
    ELT(FLOOR(RAND() * 5) + 1, '朝阳区', '浦东新区', '天河区', '西湖区', '玄武区'),
    DATE_ADD(NOW(), INTERVAL -FLOOR(RAND() * 365) DAY)
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
      UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15) t
WHERE (SELECT COUNT(*) FROM volunteer) < 15;

-- ============================================
-- 3. 添加测试活动（最近6个月）
-- ============================================
INSERT INTO activity (title, category_id, description, address, start_time, end_time, 
                      target_number, current_number, service_hours, reward_points, status, 
                      detailed_address, create_time)
SELECT 
    CONCAT(
        ELT((t.n % 5) + 1, '社区服务', '环境保护', '支教助学', '景区引导', '禁毒宣传'),
        '活动 - ',
        DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -t.n * 7 DAY), '%Y%m%d')
    ),
    ((t.n % 5) + 1),  -- category_id 1-5
    CONCAT('这是一个', ELT((t.n % 5) + 1, '社区服务', '环境保护', '支教助学', '景区引导', '禁毒宣传'), '类型的测试活动'),
    ELT((t.n % 5) + 1, '北京市朝阳区', '上海市浦东新区', '广州市天河区', '杭州市西湖区', '南京市玄武区'),
    DATE_ADD(NOW(), INTERVAL -t.n * 7 DAY),
    DATE_ADD(DATE_ADD(NOW(), INTERVAL -t.n * 7 DAY), INTERVAL 3 HOUR),
    20,
    FLOOR(RAND() * 15) + 5,  -- 5-20人
    2.5,
    25,
    IF(t.n < 10, '已结束', '进行中'),
    CONCAT('详细地址', t.n),
    DATE_ADD(NOW(), INTERVAL -(t.n * 7 + 10) DAY)
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
) t
WHERE NOT EXISTS (
    SELECT 1 FROM activity 
    WHERE title = CONCAT(
        ELT((t.n % 5) + 1, '社区服务', '环境保护', '支教助学', '景区引导', '禁毒宣传'),
        '活动 - ',
        DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -t.n * 7 DAY), '%Y%m%d')
    )
);

-- ============================================
-- 4. 添加活动申请记录
-- ============================================
INSERT INTO activity_application (activity_id, volunteer_id, status, apply_time, audit_time)
SELECT 
    a.id,
    v.id,
    '已通过',
    a.create_time,
    DATE_ADD(a.create_time, INTERVAL 1 DAY)
FROM activity a
CROSS JOIN (
    SELECT id FROM volunteer ORDER BY RAND() LIMIT 10
) v
WHERE NOT EXISTS (
    SELECT 1 FROM activity_application aa 
    WHERE aa.activity_id = a.id AND aa.volunteer_id = v.id
)
LIMIT 50;

-- ============================================
-- 5. 添加签退记录（最近6个月，分布在不同月份）
-- ============================================
INSERT INTO activity_checkin (activity_id, volunteer_id, checkin_time, checkout_time, 
                               service_hours, earned_points, status, is_valid, is_counted, create_time)
SELECT 
    a.id,
    aa.volunteer_id,
    a.start_time,
    a.end_time,
    a.service_hours,
    a.reward_points,
    2,  -- 已签退
    1,  -- 有效
    1,  -- 已计数
    a.start_time
FROM activity a
INNER JOIN activity_application aa ON a.id = aa.activity_id
WHERE aa.status = '已通过'
  AND a.status = '已结束'
  AND NOT EXISTS (
      SELECT 1 FROM activity_checkin ac 
      WHERE ac.activity_id = a.id AND ac.volunteer_id = aa.volunteer_id
  )
LIMIT 60;

-- ============================================
-- 6. 添加补录记录（最近6个月）
-- ============================================
INSERT INTO service_hour_replenish (volunteer_id, activity_name, hours, earned_points, 
                                     reason, status, apply_time, audit_time, create_time)
SELECT 
    v.id,
    CONCAT('补录活动 - ', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -t.n * 10 DAY), '%Y%m%d')),
    ROUND(RAND() * 3 + 1, 1),  -- 1-4小时
    FLOOR(RAND() * 30) + 10,   -- 10-40积分
    '因特殊原因未能及时签退，申请补录',
    '已通过',
    DATE_ADD(NOW(), INTERVAL -t.n * 10 DAY),
    DATE_ADD(DATE_ADD(NOW(), INTERVAL -t.n * 10 DAY), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL -t.n * 10 DAY)
FROM volunteer v
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) t
WHERE v.id <= 10
  AND NOT EXISTS (
      SELECT 1 FROM service_hour_replenish r 
      WHERE r.volunteer_id = v.id 
        AND r.activity_name = CONCAT('补录活动 - ', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -t.n * 10 DAY), '%Y%m%d'))
  )
LIMIT 30;

-- ============================================
-- 7. 添加论坛分类（如果不存在）
-- ============================================
INSERT IGNORE INTO forum_category (id, name, description, create_time) VALUES
(1, '志愿事迹', '分享志愿服务经历和故事', NOW()),
(2, '志愿寄语', '志愿者的心声和寄语', NOW()),
(3, '志愿点滴', '记录志愿生活的点点滴滴', NOW()),
(4, '悸动心声', '志愿服务的感悟和体会', NOW());

-- ============================================
-- 8. 添加论坛帖子
-- ============================================
INSERT INTO forum_post (category_id, volunteer_id, title, content, status, views, likes, create_time)
SELECT 
    ((t.n % 4) + 1),  -- category_id 1-4
    (SELECT id FROM volunteer ORDER BY RAND() LIMIT 1),
    CONCAT(
        ELT((t.n % 4) + 1, '志愿事迹', '志愿寄语', '志愿点滴', '悸动心声'),
        ' - ',
        ELT((t.n % 10) + 1, '难忘的一天', '温暖的回忆', '成长的足迹', '感动瞬间', '志愿之路', 
            '服务心得', '青春无悔', '爱心传递', '责任担当', '美好时光'),
        ' ',
        t.n
    ),
    CONCAT('这是一篇关于', ELT((t.n % 4) + 1, '志愿事迹', '志愿寄语', '志愿点滴', '悸动心声'), '的测试帖子内容。'),
    'published',
    FLOOR(RAND() * 200),  -- 浏览量
    FLOOR(RAND() * 50),   -- 点赞数
    DATE_ADD(NOW(), INTERVAL -t.n * 3 DAY)
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
) t
WHERE NOT EXISTS (
    SELECT 1 FROM forum_post 
    WHERE title = CONCAT(
        ELT((t.n % 4) + 1, '志愿事迹', '志愿寄语', '志愿点滴', '悸动心声'),
        ' - ',
        ELT((t.n % 10) + 1, '难忘的一天', '温暖的回忆', '成长的足迹', '感动瞬间', '志愿之路', 
            '服务心得', '青春无悔', '爱心传递', '责任担当', '美好时光'),
        ' ',
        t.n
    )
);

-- ============================================
-- 9. 添加志愿者地址
-- ============================================
INSERT INTO volunteer_address (volunteer_id, contact_name, contact_phone, province, city, district, 
                                detail_address, label, is_default, create_time)
SELECT 
    v.id,
    v.real_name,
    v.phone,
    v.province,
    v.city,
    v.district,
    CONCAT(v.district, '某某街道某某号'),
    '家',
    1,
    v.create_time
FROM volunteer v
WHERE NOT EXISTS (
    SELECT 1 FROM volunteer_address va WHERE va.volunteer_id = v.id
)
LIMIT 15;

-- ============================================
-- 10. 添加证书记录
-- ============================================
INSERT INTO certificate_library (volunteer_id, volunteer_name, certificate_no, total_hours, 
                                  certificate_hash, created_at, update_time)
SELECT 
    v.id,
    v.real_name,
    CONCAT('CERT', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(v.id, 6, '0')),
    v.total_hours,
    CONCAT('0x', MD5(CONCAT(v.id, v.real_name, NOW()))),
    DATE_ADD(NOW(), INTERVAL -FLOOR(RAND() * 90) DAY),
    NOW()
FROM volunteer v
WHERE v.total_hours >= 10
  AND NOT EXISTS (
      SELECT 1 FROM certificate_library c WHERE c.volunteer_id = v.id
  )
LIMIT 10;

-- ============================================
-- 数据生成完成，显示统计信息
-- ============================================
SELECT '=== 测试数据生成完成 ===' as 状态;

SELECT 
    (SELECT COUNT(*) FROM activity) as 活动总数,
    (SELECT COUNT(*) FROM volunteer) as 志愿者总数,
    (SELECT COUNT(*) FROM activity_checkin WHERE checkout_time IS NOT NULL) as 签退记录数,
    (SELECT COUNT(*) FROM service_hour_replenish WHERE status = '已通过') as 补录记录数,
    (SELECT COUNT(*) FROM forum_post) as 论坛帖子数,
    (SELECT COUNT(*) FROM certificate_library) as 证书数;

SELECT 
    COALESCE(SUM(service_hours), 0) as 总志愿时长,
    COALESCE(SUM(earned_points), 0) as 总积分
FROM activity_checkin 
WHERE checkout_time IS NOT NULL;

SELECT '测试数据已成功添加到数据库！' as 提示;
SELECT '请重启后端服务，然后刷新前端页面查看效果。' as 下一步;
