-- 修改志愿服务证书表结构
USE volunteer_system;

-- 1. 清空表中所有数据
TRUNCATE TABLE service_certificate;

-- 2. 增加新字段
ALTER TABLE service_certificate 
ADD COLUMN volunteer_name VARCHAR(50) NOT NULL COMMENT '志愿者姓名' AFTER volunteer_id;

ALTER TABLE service_certificate 
ADD COLUMN wallet_address VARCHAR(100) COMMENT '志愿者钱包地址' AFTER volunteer_name;

ALTER TABLE service_certificate 
ADD COLUMN volunteer_hours DECIMAL(10,2) DEFAULT 0 COMMENT '志愿时长（正常打卡累计的时长）' AFTER wallet_address;

ALTER TABLE service_certificate 
ADD COLUMN replenish_hours DECIMAL(10,2) DEFAULT 0 COMMENT '补录时长（申请补录成功的时长）' AFTER volunteer_hours;

ALTER TABLE service_certificate 
ADD COLUMN certificate_number VARCHAR(50) NOT NULL COMMENT '证书编号' AFTER replenish_hours;

-- 3. 添加索引
ALTER TABLE service_certificate 
ADD UNIQUE INDEX idx_certificate_number (certificate_number);

ALTER TABLE service_certificate 
ADD INDEX idx_wallet_address (wallet_address);

-- 4. 查看修改后的表结构
DESCRIBE service_certificate;
