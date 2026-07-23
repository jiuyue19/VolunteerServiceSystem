-- 为证书库表添加证书哈希字段

ALTER TABLE `certificate_library` 
ADD COLUMN `certificate_hash` VARCHAR(64) NOT NULL COMMENT '证书哈希（SHA-256）' AFTER `certificate_no`,
ADD UNIQUE KEY `uk_certificate_hash` (`certificate_hash`);

-- 说明：
-- 证书哈希由以下字段生成：
-- SHA256(志愿者姓名 + 钱包地址 + 链上总时长 + 链上补录时长 + 创建时间戳)
-- 用于证书防伪验证
