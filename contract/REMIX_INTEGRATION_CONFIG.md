# Remix 合约集成配置指南

## 📋 概述

本文档说明如何配置管理员端与 Remix IDE 部署的智能合约进行数据联调。

## 🔧 配置步骤

### 1. 合约部署信息

在 Remix 中部署合约后，需要更新以下配置：

#### 1.1 更新 application.yml
```yaml
blockchain:
  contract-address: "0x你的合约地址"  # 从 Remix 部署结果获取
  rpc-url: http://127.0.0.1:8545     # Geth 节点地址
  admin-private-key: "你的管理员私钥"  # 管理员账户私钥
  chain-id: 2025                     # 与 genesis.json 保持一致
```

#### 1.2 验证合约地址格式
- 必须是 42 个字符的十六进制字符串
- 以 `0x` 开头
- 示例：`0xEC6AEE2828998E9122Fbe0A0F5c12ade9E026809`

### 2. Remix 部署流程

#### 2.1 准备工作
1. 启动本地 Geth 节点
2. 确保账户有足够的 ETH 用于部署
3. 在 Remix 中打开 `VolunteerHours.sol` 合约

#### 2.2 部署步骤
```javascript
// 1. 编译合约
// 2. 选择环境：Injected Provider - MetaMask 或 Web3 Provider
// 3. 连接到本地节点：http://127.0.0.1:8545
// 4. 部署合约
// 5. 复制合约地址
```

#### 2.3 部署后验证
```javascript
// 在 Remix Console 中验证合约功能
contract.methods.getTotalDuration("志愿者钱包地址").call()
contract.methods.getReplenishDuration("志愿者钱包地址").call()
```

### 3. 管理员端配置

#### 3.1 志愿者数据准备
确保数据库中有以下测试数据：

```sql
-- 插入测试志愿者（如果不存在）
INSERT INTO volunteer (username, password, real_name, wallet_address) 
VALUES ('test_volunteer', '$2a$10$...', '测试志愿者', '0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb')
ON DUPLICATE KEY UPDATE wallet_address = VALUES(wallet_address);

-- 插入测试活动记录
INSERT INTO activity_checkin (volunteer_id, activity_id, checkin_time, checkout_time, service_hours, earned_points, status)
VALUES (1, 1, NOW() - INTERVAL 2 HOUR, NOW(), 2.0, 20, 2);

-- 插入补录记录
INSERT INTO service_hour_replenish (volunteer_id, hours, earned_points, reason, status)
VALUES (1, 1.5, 15, '测试补录', '已通过');
```

#### 3.2 API 接口验证
测试以下接口是否正常工作：

```bash
# 1. 获取志愿者列表
GET /api/volunteer/list

# 2. 查询链上数据
GET /api/blockchain/total-hours/{volunteerId}

# 3. 同步上链
POST /api/blockchain/sync-hours
{
  "volunteerId": 1
}
```

### 4. 数据联调验证

#### 4.1 完整流程测试
1. **管理员端查询**：
   - 选择志愿者
   - 点击"查询链上数据"
   - 验证返回的数据格式

2. **数据上链**：
   - 点击"同步上链"
   - 查看上链确认对话框
   - 确认上链并验证交易哈希

3. **证书生成**：
   - 查询链上数据后
   - 点击"生成证书"
   - 验证证书数据来源于链上

#### 4.2 Remix 验证
在 Remix 中验证数据是否正确上链：

```javascript
// 查询总时长
await contract.methods.getTotalDuration("0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb").call()

// 查询补录时长  
await contract.methods.getReplenishDuration("0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb").call()

// 查询志愿者信息
await contract.methods.volunteers("0x1be31a94361a391bbafb2a4ccd704f57dc04d4bb").call()
```

## 🔍 故障排查

### 常见问题

#### 1. 合约地址无效
```
错误：合约地址格式不正确
解决：检查 application.yml 中的地址是否用引号包裹
```

#### 2. 交易失败
```
错误：only replay-protected (EIP-155) transactions allowed over RPC
解决：确保 chain-id 配置正确
```

#### 3. 连接失败
```
错误：Connection refused
解决：确保 Geth 节点正在运行并监听正确端口
```

#### 4. 数据不一致
```
错误：链上数据与数据库不匹配
解决：重新同步数据或检查合约函数调用
```

## 📊 数据流图

```
Remix IDE
    ↓ (部署合约)
智能合约 (Geth 节点)
    ↑ ↓ (读写数据)
后端 API (BlockchainService)
    ↑ ↓ (HTTP 请求)
管理员端 (Blockchain.vue)
    ↓ (用户操作)
证书生成 (基于链上数据)
```

## 🎯 最佳实践

### 1. 开发环境
- 使用本地 Geth 节点进行测试
- 保持合约代码与后端接口同步
- 定期备份钱包和私钥

### 2. 数据管理
- 上链前务必确认数据准确性
- 定期验证链上数据与数据库一致性
- 保留交易哈希用于审计

### 3. 安全考虑
- 私钥不要硬编码在代码中
- 使用环境变量管理敏感信息
- 定期更新依赖包版本

## 📞 技术支持

如遇到问题，请检查：
1. `TROUBLESHOOTING.md` - 故障排查指南
2. `BLOCKCHAIN_FIX_SUMMARY.md` - 修复说明
3. `BLOCKCHAIN_WORKFLOW_GUIDE.md` - 工作流程指南

---

**文档版本**: v1.0  
**更新时间**: 2024年12月1日  
**状态**: ✅ 准备就绪
