-- ===========================================
-- 修改志愿服务证书表结构
-- 增加志愿者姓名、钱包地址、志愿时长、补录时长、证书编号字段
-- ===========================================

USE volunteer_system;

-- 首先清空表中所有数据
TRUNCATE TABLE service_certificate;

-- 修改表结构，增加新字段
ALTER TABLE service_certificate 
ADD COLUMN volunteer_name VARCHAR(50) NOT NULL COMMENT '志愿者姓名' AFTER volunteer_id,
ADD COLUMN wallet_address VARCHAR(100) COMMENT '志愿者钱包地址' AFTER volunteer_name,
ADD COLUMN volunteer_hours DECIMAL(10,2) DEFAULT 0 COMMENT '志愿时长（正常打卡累计的时长）' AFTER wallet_address,
ADD COLUMN replenish_hours DECIMAL(10,2) DEFAULT 0 COMMENT '补录时长（申请补录成功的时长）' AFTER volunteer_hours,
ADD COLUMN certificate_number VARCHAR(50) NOT NULL COMMENT '证书编号' AFTER replenish_hours;

-- 为证书编号字段添加唯一索引
ALTER TABLE service_certificate 
ADD UNIQUE INDEX idx_certificate_number (certificate_number);

-- 为钱包地址字段添加索引
ALTER TABLE service_certificate 
ADD INDEX idx_wallet_address (wallet_address);

-- 查看修改后的表结构
DESCRIBE service_certificate;

-- 验证表是否为空
SELECT COUNT(*) as record_count FROM service_certificate;

-- 显示修改完成信息
SELECT 'service_certificate表结构修改完成！' as status,
       '已增加：volunteer_name, wallet_address, volunteer_hours, replenish_hours, certificate_number字段' as added_fields,
       '已清空所有数据' as data_status;
