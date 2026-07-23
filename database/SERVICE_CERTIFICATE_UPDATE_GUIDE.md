# 志愿服务证书表结构修改指南

## 📋 修改概述

本次修改将对 `service_certificate` 表进行结构优化，增加区块链证书生成所需的关键字段。

## 🎯 修改目标

### 新增字段

1. **volunteer_name** - 志愿者姓名
   - 类型：`VARCHAR(50) NOT NULL`
   - 说明：存储志愿者真实姓名，用于证书显示

2. **wallet_address** - 志愿者钱包地址
   - 类型：`VARCHAR(100)`
   - 说明：存储区块链钱包地址，用于区块链验证

3. **volunteer_hours** - 志愿时长
   - 类型：`DECIMAL(10,2) DEFAULT 0`
   - 说明：正常打卡累计的志愿服务时长

4. **replenish_hours** - 补录时长
   - 类型：`DECIMAL(10,2) DEFAULT 0`
   - 说明：申请补录成功的志愿服务时长

5. **certificate_number** - 证书编号
   - 类型：`VARCHAR(50) NOT NULL`
   - 说明：唯一的证书编号，格式如：CERT-1BE3-1234

### 索引优化

- 为 `certificate_number` 添加唯一索引
- 为 `wallet_address` 添加普通索引

## 🚀 执行步骤

### 方法1：使用MySQL命令行工具

```bash
# 1. 连接到MySQL
mysql -u root -p

# 2. 选择数据库
USE volunteer_system;

# 3. 执行修改脚本
source e:/毕业设计/VolunteerServiceSystem/database/update_service_certificate_simple.sql;
```

### 方法2：使用MySQL Workbench或其他GUI工具

1. 打开MySQL Workbench
2. 连接到数据库
3. 打开 `update_service_certificate_simple.sql` 文件
4. 执行整个脚本

### 方法3：逐步手动执行

```sql
-- 1. 选择数据库
USE volunteer_system;

-- 2. 清空现有数据
TRUNCATE TABLE service_certificate;

-- 3. 添加志愿者姓名字段
ALTER TABLE service_certificate 
ADD COLUMN volunteer_name VARCHAR(50) NOT NULL COMMENT '志愿者姓名' AFTER volunteer_id;

-- 4. 添加钱包地址字段
ALTER TABLE service_certificate 
ADD COLUMN wallet_address VARCHAR(100) COMMENT '志愿者钱包地址' AFTER volunteer_name;

-- 5. 添加志愿时长字段
ALTER TABLE service_certificate 
ADD COLUMN volunteer_hours DECIMAL(10,2) DEFAULT 0 COMMENT '志愿时长（正常打卡累计的时长）' AFTER wallet_address;

-- 6. 添加补录时长字段
ALTER TABLE service_certificate 
ADD COLUMN replenish_hours DECIMAL(10,2) DEFAULT 0 COMMENT '补录时长（申请补录成功的时长）' AFTER volunteer_hours;

-- 7. 添加证书编号字段
ALTER TABLE service_certificate 
ADD COLUMN certificate_number VARCHAR(50) NOT NULL COMMENT '证书编号' AFTER replenish_hours;

-- 8. 添加唯一索引
ALTER TABLE service_certificate 
ADD UNIQUE INDEX idx_certificate_number (certificate_number);

-- 9. 添加钱包地址索引
ALTER TABLE service_certificate 
ADD INDEX idx_wallet_address (wallet_address);

-- 10. 查看修改后的表结构
DESCRIBE service_certificate;
```

## 📊 修改前后对比

### 修改前的表结构
```sql
CREATE TABLE service_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    total_hours DECIMAL(10,2) NOT NULL COMMENT '累计服务时长（小时）',
    issue_date DATE NOT NULL COMMENT '发证日期',
    certificate_hash VARCHAR(255) COMMENT '证书区块链哈希值',
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
);
```

### 修改后的表结构
```sql
CREATE TABLE service_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    volunteer_name VARCHAR(50) NOT NULL COMMENT '志愿者姓名',
    wallet_address VARCHAR(100) COMMENT '志愿者钱包地址',
    volunteer_hours DECIMAL(10,2) DEFAULT 0 COMMENT '志愿时长（正常打卡累计的时长）',
    replenish_hours DECIMAL(10,2) DEFAULT 0 COMMENT '补录时长（申请补录成功的时长）',
    certificate_number VARCHAR(50) NOT NULL COMMENT '证书编号',
    total_hours DECIMAL(10,2) NOT NULL COMMENT '累计服务时长（小时）',
    issue_date DATE NOT NULL COMMENT '发证日期',
    certificate_hash VARCHAR(255) COMMENT '证书区块链哈希值',
    UNIQUE KEY idx_certificate_number (certificate_number),
    KEY idx_wallet_address (wallet_address),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
);
```

## ✅ 验证修改

执行以下SQL验证修改是否成功：

```sql
-- 1. 查看表结构
DESCRIBE service_certificate;

-- 2. 查看索引
SHOW INDEX FROM service_certificate;

-- 3. 确认数据已清空
SELECT COUNT(*) as record_count FROM service_certificate;

-- 4. 测试插入数据（可选）
INSERT INTO service_certificate (
    volunteer_id, 
    volunteer_name, 
    wallet_address, 
    volunteer_hours, 
    replenish_hours, 
    certificate_number, 
    total_hours, 
    issue_date
) VALUES (
    1, 
    '测试志愿者', 
    '0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb', 
    10.5, 
    2.0, 
    'CERT-1BE3-1234', 
    12.5, 
    CURDATE()
);

-- 5. 查看插入结果
SELECT * FROM service_certificate;

-- 6. 清理测试数据
DELETE FROM service_certificate WHERE certificate_number = 'CERT-1BE3-1234';
```

## 🎯 字段用途说明

### 1. volunteer_name（志愿者姓名）
- **来源**：从 `volunteer.real_name` 获取
- **用途**：证书上显示的志愿者姓名
- **示例**：`张三`

### 2. wallet_address（钱包地址）
- **来源**：从 `volunteer.wallet_address` 获取
- **用途**：区块链验证和证书防伪
- **示例**：`0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb`

### 3. volunteer_hours（志愿时长）
- **来源**：从区块链 `chainTotalHours` 获取
- **用途**：正常打卡累计的志愿服务时长
- **示例**：`10.50`（10小时30分钟）

### 4. replenish_hours（补录时长）
- **来源**：从区块链 `chainReplenishHours` 获取
- **用途**：申请补录成功的志愿服务时长
- **示例**：`2.00`（2小时）

### 5. certificate_number（证书编号）
- **来源**：系统自动生成
- **格式**：`CERT-{钱包地址前4位}-{时间戳后4位}`
- **示例**：`CERT-1BE3-1234`

## 🔄 与区块链数据同步

修改后的表结构完全支持区块链证书生成工作流程：

1. **数据来源**：所有核心数据来自区块链
2. **数据一致性**：确保证书数据与链上数据完全一致
3. **防篡改**：通过钱包地址和证书编号实现防伪验证

## ⚠️ 注意事项

1. **数据清空**：执行脚本会清空表中所有现有数据
2. **备份建议**：修改前建议备份现有数据
3. **字段约束**：`volunteer_name` 和 `certificate_number` 为必填字段
4. **唯一性**：`certificate_number` 必须唯一
5. **索引影响**：新增索引可能影响插入性能，但提升查询性能

## 📞 技术支持

如遇到问题，请检查：
1. MySQL版本兼容性（建议5.7+）
2. 数据库连接权限
3. 表是否被其他进程锁定
4. 字符集设置是否正确

---

**修改时间**: 2024年12月1日  
**版本**: v1.0  
**状态**: ✅ 准备就绪
