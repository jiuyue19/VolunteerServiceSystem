# 区块链合约配置模板

## 📝 部署后填写此配置

### 1. 智能合约信息

```
部署日期: ____________________
部署人员: ____________________

合约地址: 0x________________________________________
交易哈希: 0x________________________________________
区块号: __________
```

### 2. 网络配置

```
网络名称: Geth Private Chain
RPC URL: http://127.0.0.1:8545
链 ID: 2025
货币符号: ETH
```

### 3. 管理员账户

```
钱包地址: 0x________________________________________
余额: __________ ETH
```

**⚠️ 安全警告**：私钥仅在 `application.yml` 中配置，不要在此处记录！

### 4. 后端配置 (application.yml)

```yaml
blockchain:
  contract-address: 0x________________________________________
  rpc-url: http://127.0.0.1:8545
  admin-private-key: ________________________________________(从MetaMask导出)
```

### 5. 验证步骤

- [ ] 在 Remix 中成功部署合约
- [ ] 在 MetaMask 中看到部署交易
- [ ] 已将合约地址复制到 application.yml
- [ ] 已配置管理员私钥
- [ ] 后端服务重启无报错
- [ ] 测试调用合约成功

---

## 🧪 快速测试命令

### Geth 控制台测试合约

```javascript
// 1. 检查合约代码是否存在
eth.getCode("0x你的合约地址")
// 应返回一长串十六进制代码（不是 "0x"）

// 2. 检查管理员余额
eth.getBalance("0x你的管理员地址")
// 应该大于 0

// 3. 查看最新区块
eth.blockNumber

// 4. 查看挖矿状态
eth.mining
// 应返回 true
```

### 数据库测试

```sql
-- 检查志愿者钱包绑定
SELECT id, real_name, wallet_address, total_hours 
FROM volunteer 
WHERE wallet_address IS NOT NULL;

-- 查看需要上链的志愿者
SELECT id, real_name, total_hours 
FROM volunteer 
WHERE total_hours > 0 AND wallet_address IS NOT NULL;
```

---

## 📊 部署记录表

| 日期 | 操作 | 结果 | 备注 |
|------|------|------|------|
| YYYY-MM-DD | 部署 VolunteerHours 合约 | ✅/❌ | 合约地址: 0x... |
| YYYY-MM-DD | 配置后端 application.yml | ✅/❌ | |
| YYYY-MM-DD | 测试数据上链 | ✅/❌ | 志愿者ID: 1 |
| YYYY-MM-DD | 测试数据查询 | ✅/❌ | |
| YYYY-MM-DD | 生成证书测试 | ✅/❌ | |

---

## 🔗 有用的链接

- **Remix IDE**: https://remix.ethereum.org
- **MetaMask**: https://metamask.io
- **Web3j 文档**: https://docs.web3j.io
- **Solidity 文档**: https://docs.soliditylang.org

---

**最后更新**: _______________
