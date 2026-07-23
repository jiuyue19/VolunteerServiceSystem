# 区块链证书系统故障排查指南

## ❌ 常见错误 1: rlp: input string too long for common.Address

### 错误信息
```
Error: 上链失败: 交易失败: rlp: input string too long for common.Address, decoding into (types.LegacyTx).To
```

### 错误原因
这个错误表示传入的钱包地址**格式不正确**或**为空**。区块链智能合约要求使用**以太坊地址**（42个字符，以`0x`开头），例如：`0xBC7567A0055ECF24A908f526Eb51A5cfc214de61`

可能的原因：
1. 志愿者**未绑定钱包地址**（数据库 `volunteer.wallet_address` 字段为NULL或空字符串）
2. 钱包地址**格式错误**（不是有效的以太坊地址）
3. 传入了**志愿者姓名**或其他长字符串（而不是钱包地址）
4. ⚠️ **合约函数名不匹配**（Java代码中的函数名与智能合约中的不一致）

### 解决方案

#### 方案1：确保志愿者已绑定钱包地址

**步骤1：检查数据库**
```sql
-- 查看志愿者的钱包地址
SELECT id, real_name, wallet_address FROM volunteer WHERE id = 1;
```

**步骤2：如果wallet_address为NULL，需要先绑定**

有两种方式绑定钱包：

**方式A：志愿者端自行绑定**
1. 志愿者登录系统
2. 进入"服务证书"页面
3. 点击"连接 MetaMask"
4. 系统自动保存钱包地址到数据库

**方式B：管理员手动配置**
```sql
-- 手动更新钱包地址（管理员为志愿者创建/分配钱包）
UPDATE volunteer 
SET wallet_address = '0xBC7567A0055ECF24A908f526Eb51A5cfc214de61'  -- 志愿者的MetaMask地址
WHERE id = 1;
```

#### 方案2：使用改进的前端界面

最新的前端界面（Blockchain.vue）已经改进为：
- ✅ 使用下拉选择框显示**志愿者姓名**
- ✅ 显示钱包绑定状态（"已绑定"或"未绑定钱包"）
- ✅ 支持搜索过滤志愿者
- ✅ 只能选择已绑定钱包的志愿者进行上链

**使用方法：**
1. 管理员打开"区块链证书管理"页面
2. 在"数据上链"区域，点击下拉框选择志愿者
3. 确认志愿者显示"已绑定"状态
4. 点击"同步上链"按钮

#### 方案3：检查合约函数名（已修复）

如果你遇到这个错误，可能是Java代码中的函数名与智能合约不一致。**此问题已在最新代码中修复**。

合约函数名映射（已修正）：
- ✅ `updateTotalDuration` (合约) ← `updateTotalHours` (Java方法名)
- ✅ `addReplenishDuration` (合约) ← `addReplenishHours` (Java方法名)
- ✅ `getTotalDuration` (合约) ← `getTotalHours` (Java方法名)
- ✅ `getReplenishDuration` (合约) ← `getReplenishHours` (Java方法名)

**验证方法**：查看后端控制台日志，应该显示详细的地址验证信息：
```
[区块链] 志愿者地址: 0x1be31a...
[区块链] 合约地址: 0xEC6AEE...
[区块链] 服务时长: 2.5 小时
```

---

## ❌ 常见错误 2: 志愿者未绑定钱包地址

### 错误信息
```
Error: 志愿者未绑定钱包地址
```

### 解决方案
参考上面"方案1"中的步骤绑定钱包地址。

---

## ❌ 常见错误 3: Gas estimation failed

### 错误信息
```
Gas estimation failed: missing revert data
```

### 错误原因
1. Geth 私链未正常启动或挖矿
2. Gas 价格或 Gas Limit 设置不当
3. 合约函数执行时revert（例如权限检查失败）

### 解决方案

**步骤1：检查 Geth 是否正常运行**
```bash
# 在 Geth 控制台中检查
eth.mining          # 应返回 true
eth.blockNumber     # 应返回递增的区块号
```

**步骤2：确认 Gas 配置**
确保 Geth 启动时包含：
```bash
--miner.gasprice 0           # 私链使用零 gas 价格
--miner.gaslimit 30000000    # 足够大的 gas limit
```

**步骤3：检查合约权限**
```javascript
// 在 Geth 控制台检查管理员地址
var contractABI = [...]; // 合约 ABI
var contract = eth.contract(contractABI).at("0x合约地址");
contract.admin();  // 应返回管理员地址

// 检查当前发送交易的地址是否为管理员
eth.accounts[0]    // 应与 contract.admin() 返回的地址一致
```

---

## ❌ 常见错误 4: Transaction mined but execution failed

### 错误信息
```
0 Transaction mined but execution failed
```

### 错误原因
交易已上链，但智能合约执行失败。常见原因：
1. EVM 版本不兼容（合约编译版本高于链支持的版本）
2. 合约函数的 require 检查失败
3. 权限不足（非管理员调用 onlyAdmin 函数）

### 解决方案

**步骤1：检查 EVM 版本**
- Remix 编译器设置：使用 **`london`** 或 **`paris`**
- ❌ 不要使用 `shanghai`（除非私链已配置 Shanghai 硬分叉）

**步骤2：检查合约调用权限**
```java
// 确认 application.yml 中的 admin-private-key 是否正确
blockchain:
  admin-private-key: f34c...  # 必须是部署合约的账户私钥
```

**步骤3：查看交易详情**
```javascript
// 在 Geth 控制台
eth.getTransaction("0x交易哈希")
eth.getTransactionReceipt("0x交易哈希")
```

---

---

## ❌ 常见错误 5: 合约地址格式不正确（被解析为数字）

### 错误信息
```
Error: 上链失败: 合约地址格式不正确: 1349706447294783242400091914463065336231372154889
```

### 错误原因
YAML配置文件会将 `0x` 开头的值**解析为十六进制数字**，而不是字符串！

例如：
```yaml
# ❌ 错误 - 会被解析为十进制数字
blockchain:
  contract-address: 0xEC6AEE2828998E9122Fbe0A0F5c12ade9E026809
```

YAML解析器会把这个地址转换为：`1349706447294783242400091914463065336231372154889`

### 解决方案

**在 `application.yml` 中给所有以 `0x` 或十六进制开头的值加上引号**：

```yaml
# ✅ 正确 - 使用引号保持字符串格式
blockchain:
  contract-address: "0xEC6AEE2828998E9122Fbe0A0F5c12ade9E026809"
  rpc-url: http://127.0.0.1:8545
  admin-private-key: "f34c6c230f0317e3a3584cc306bebf78ec1372d207b56f5c4d64a9d214d89114"
```

**验证方法**：
1. 重启后端服务
2. 查看后端日志，应该显示正确的地址格式：
   ```
   [区块链] 合约地址: 0xEC6AEE2828998E9122Fbe0A0F5c12ade9E026809
   ```
   而不是：
   ```
   [区块链] 合约地址: 1349706447294783242400091914463065336231372154889
   ```

---

## ❌ 常见错误 6: EIP-155 交易保护错误

### 错误信息
```
上链失败: 交易失败: only replay-protected (EIP-155) transactions allowed over RPC
```

### 错误原因
Geth 节点要求所有通过 RPC 发送的交易必须包含 **chain ID**（链标识符），这是 EIP-155 标准的要求，用于防止重放攻击。

如果交易没有包含 chain ID，Geth 会拒绝该交易。

### 解决方案

#### 步骤1: 添加 chain-id 配置

在 `application.yml` 中添加 `chain-id`（与 `genesis.json` 中的 `chainId` 一致）：

```yaml
blockchain:
  contract-address: "0xEC6AEE2828998E9122Fbe0A0F5c12ade9E026809"
  rpc-url: http://127.0.0.1:8545
  admin-private-key: "f34c6c230f0317e3a3584cc306bebf78ec1372d207b56f5c4d64a9d214d89114"
  chain-id: 2025  # ← 新增：与genesis.json保持一致
```

#### 步骤2: 更新 BlockchainConfig.java

```java
@Value("${blockchain.chain-id}")
private long chainId;

public long getChainId() {
    return chainId;
}
```

#### 步骤3: 更新 BlockchainService.java

在所有使用 `RawTransactionManager` 的地方添加 chain ID：

```java
// 修改前 - ❌ 错误
RawTransactionManager transactionManager = new RawTransactionManager(
    blockchainConfig.getWeb3j(),
    blockchainConfig.getCredentials()
);

// 修改后 - ✅ 正确
RawTransactionManager transactionManager = new RawTransactionManager(
    blockchainConfig.getWeb3j(),
    blockchainConfig.getCredentials(),
    blockchainConfig.getChainId()  // ← 添加 chainId
);
```

#### 步骤4: 重启后端服务

修改配置后必须重启后端服务才能生效。

### 验证方法

1. 确认 `genesis.json` 中的 chainId：
   ```json
   {
     "config": {
       "chainId": 2025,
       ...
     }
   }
   ```

2. 确认 `application.yml` 中的 chain-id 与之一致：
   ```yaml
   blockchain:
     chain-id: 2025
   ```

3. 重启后端后尝试上链，应该不再出现此错误

---

## 🔧 最佳实践

### 1. 部署前检查清单
- [ ] Geth 私链已启动并正常挖矿
- [ ] 合约已成功部署（使用 london 或 paris EVM 版本）
- [ ] application.yml 已配置正确的合约地址和管理员私钥
- [ ] 志愿者已绑定 MetaMask 钱包地址
- [ ] 后端服务无报错启动

### 2. 上链前验证
```sql
-- 检查志愿者钱包绑定情况
SELECT 
  id, 
  real_name, 
  total_hours,
  wallet_address,
  CASE 
    WHEN wallet_address IS NULL THEN '未绑定'
    WHEN wallet_address = '' THEN '未绑定'
    ELSE '已绑定'
  END AS wallet_status
FROM volunteer 
WHERE id = 1;
```

### 3. 测试流程
1. **单个志愿者测试**：先为一个已绑定钱包的志愿者上链
2. **查询验证**：查询链上数据，确认一致性
3. **批量操作**：确认单个测试成功后，再批量上链

---

## 📚 相关文档
- **部署指南**: `BLOCKCHAIN_DEPLOYMENT_GUIDE.md`
- **配置模板**: `CONTRACT_CONFIG_TEMPLATE.md`
- **部署总结**: `DEPLOYMENT_SUMMARY.md`

---

## 🆘 紧急情况处理

### 问题：所有志愿者都未绑定钱包
**快速解决**：
```sql
-- 为测试账户分配测试钱包地址（仅用于开发测试）
UPDATE volunteer 
SET wallet_address = CONCAT('0x', LPAD(CONV(id, 10, 16), 40, '0'))
WHERE wallet_address IS NULL OR wallet_address = '';

-- 例如：ID=1 的志愿者会被分配 0x0000000000000000000000000000000000000001
```

⚠️ **警告**：这种方式生成的地址**不是真实的 MetaMask 地址**，只能用于开发测试，不要在生产环境使用！

### 问题：需要重新部署合约
如果合约部署错误需要重新部署：
1. 在 Remix 中重新部署合约
2. 获取新的合约地址
3. 更新 `application.yml` 中的 `contract-address`
4. 更新 `frontend/src/config/blockchain.js` 中的 `CONTRACT_ADDRESS`
5. 重启后端和前端服务

---

**最后更新**: 2024年12月
**版本**: 1.0
