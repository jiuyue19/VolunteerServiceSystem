-- 志愿服务证书库表
-- 用于存储管理员生成的区块链证书记录

DROP TABLE IF EXISTS `certificate_library`;

CREATE TABLE `certificate_library` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `certificate_no` VARCHAR(100) NOT NULL COMMENT '证书编号（唯一）',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `volunteer_name` VARCHAR(100) NOT NULL COMMENT '志愿者姓名',
  `wallet_address` VARCHAR(100) NOT NULL COMMENT '钱包地址',
  
  -- 服务数据（从区块链读取）
  `total_hours` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总服务时长（小时）',
  `replenish_hours` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '补录时长（小时）',
  `activity_count` INT NOT NULL DEFAULT 0 COMMENT '参与活动数量',
  
  -- 区块链信息
  `chain_total_hours` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '链上总时长',
  `chain_replenish_hours` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '链上补录时长',
  `tx_hash` VARCHAR(100) DEFAULT NULL COMMENT '上链交易哈希',
  
  -- 证书状态
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '证书状态：pending待发放，issued已发放，revoked已撤销',
  `issue_date` DATETIME DEFAULT NULL COMMENT '发放时间',
  `issuer_id` BIGINT DEFAULT NULL COMMENT '发放人ID（管理员）',
  
  -- 证书图片
  `certificate_image_url` VARCHAR(500) DEFAULT NULL COMMENT '证书图片URL',
  
  -- 备注信息
  `remark` TEXT DEFAULT NULL COMMENT '备注',
  
  -- 时间戳
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_certificate_no` (`certificate_no`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_wallet_address` (`wallet_address`),
  KEY `idx_status` (`status`),
  KEY `idx_volunteer_name` (`volunteer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿服务证书库';
