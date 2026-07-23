-- ===========================================
-- 清空区块链证书相关的模拟数据
-- 保留表结构和模板，清空测试数据
-- ===========================================

USE volunteer_system;

-- 1. 清空志愿服务证书表（保留表结构）
TRUNCATE TABLE service_certificate;

-- 2. 重置志愿者钱包地址（可选，根据需要执行）
-- UPDATE volunteer SET wallet_address = NULL WHERE wallet_address IS NOT NULL;

-- 3. 清空测试用的签退记录（可选，根据需要执行）
-- DELETE FROM activity_checkin WHERE remark LIKE '%测试%' OR remark LIKE '%demo%';

-- 4. 清空测试用的补录记录（可选，根据需要执行）  
-- DELETE FROM service_hour_replenish WHERE reason LIKE '%测试%' OR reason LIKE '%demo%';

-- 5. 验证清理结果
SELECT 'service_certificate表记录数' as table_name, COUNT(*) as record_count FROM service_certificate
UNION ALL
SELECT '志愿者钱包绑定数', COUNT(*) FROM volunteer WHERE wallet_address IS NOT NULL
UNION ALL  
SELECT '签退记录总数', COUNT(*) FROM activity_checkin
UNION ALL
SELECT '补录记录总数', COUNT(*) FROM service_hour_replenish;

-- 6. 显示清理完成信息
SELECT 
    '区块链证书模拟数据清理完成！' as status,
    '已清空 service_certificate 表' as certificate_table,
    '保留了证书模板和表结构' as template_status,
    '准备与 Remix 合约进行数据联调' as next_step;
