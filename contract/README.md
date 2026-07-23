# 志愿者服务系统智能合约

## 合约简介

本项目包含志愿者服务时长管理智能合约：

### VolunteerHours.sol - 志愿时长管理合约
用于记录和管理志愿者的服务时长，确保数据的不可篡改性和可追溯性。

---

## 快速开始

继续阅读本文档了解 VolunteerHours.sol 的部署和使用方法。

---

## 合约一：VolunteerHours.sol

### 合约简介

`VolunteerHours.sol` 是一个基于以太坊的智能合约，用于记录和管理志愿者的服务时长，确保数据的不可篡改性和可追溯性。

## 合约功能

### 核心功能

1. **时长管理**
   - 记录志愿者累计服务时长
   - 区分活动时长和补录时长
   - 支持批量更新

2. **上链记录**
   - 每次上链操作都会生成记录
   - 记录包含时长、时间戳、类型（活动/补录）、哈希值
   - 支持查询历史记录

3. **权限控制**
   - 仅管理员可以执行写操作
   - 任何人都可以查询链上数据

## 合约部署

### 使用 Remix IDE 部署

1. 打开 [Remix IDE](https://remix.ethereum.org/)

2. 创建新文件 `VolunteerHours.sol`，复制合约代码

3. 编译合约
   - 选择编译器版本：0.8.0 或更高
   - 点击 "Compile VolunteerHours.sol"

4. 部署合约
   - 切换到 "Deploy & Run Transactions" 标签
   - 选择环境：
     - `Remix VM` - 本地测试
     - `Injected Provider - MetaMask` - 连接测试网或主网
     - `Ganache Provider` - 连接本地 Ganache
   - 点击 "Deploy"

5. 记录合约地址
   - 部署成功后，复制合约地址
   - 更新后端配置文件中的 `blockchain.contract-address`

### 使用 Hardhat 部署

```bash
# 安装依赖
npm install --save-dev hardhat @nomicfoundation/hardhat-toolbox

# 初始化项目
npx hardhat

# 创建部署脚本 scripts/deploy.js
```

```javascript
async function main() {
  const VolunteerHours = await ethers.getContractFactory("VolunteerHours");
  const contract = await VolunteerHours.deploy();
  await contract.deployed();
  console.log("Contract deployed to:", contract.address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
```

```bash
# 部署到本地网络
npx hardhat run scripts/deploy.js --network localhost

# 部署到测试网
npx hardhat run scripts/deploy.js --network sepolia
```

## 合约接口说明

### 写入函数（仅管理员）

#### 1. updateTotalHours
更新志愿者总服务时长

```solidity
function updateTotalHours(address volunteer, uint256 hours) public onlyAdmin
```

**参数：**
- `volunteer`: 志愿者钱包地址
- `hours`: 累计总时长（以0.01小时为单位，例如100表示1小时）

**后端调用示例：**
```java
blockchainService.updateTotalHours("0x123...", new BigDecimal("10.5"));
// 链上存储为 1050
```

#### 2. addReplenishHours
添加补录时长记录

```solidity
function addReplenishHours(
    address volunteer,
    uint256 hours,
    string memory recordHash
) public onlyAdmin
```

**参数：**
- `volunteer`: 志愿者地址
- `hours`: 补录时长（0.01小时单位）
- `recordHash`: 补录记录哈希（可以是IPFS哈希或SHA256）

#### 3. addActivityRecord
添加活动参与记录

```solidity
function addActivityRecord(
    address volunteer,
    uint256 hours,
    string memory recordHash
) public onlyAdmin
```

**参数：**
- `volunteer`: 志愿者地址
- `hours`: 活动服务时长（0.01小时单位）
- `recordHash`: 活动记录哈希

#### 4. batchUpdateHours
批量更新多个志愿者的时长

```solidity
function batchUpdateHours(
    address[] memory volunteers,
    uint256[] memory hours
) public onlyAdmin
```

### 查询函数（公开）

#### 1. getTotalHours
查询志愿者总服务时长

```solidity
function getTotalHours(address volunteer) public view returns (uint256)
```

**返回：** 累计总时长（0.01小时单位）

#### 2. getReplenishHours
查询志愿者补录时长

```solidity
function getReplenishHours(address volunteer) public view returns (uint256)
```

#### 3. getRecordCount
查询志愿者上链记录数量

```solidity
function getRecordCount(address volunteer) public view returns (uint256)
```

#### 4. getRecord
查询指定索引的上链记录

```solidity
function getRecord(address volunteer, uint256 index) public view returns (
    uint256 hours,
    uint256 timestamp,
    bool isReplenish,
    string memory recordHash
)
```

## 事件说明

### HoursUpdated
时长更新事件

```solidity
event HoursUpdated(
    address indexed volunteer,
    uint256 totalHours,
    uint256 timestamp
);
```

### HoursReplenished
补录时长事件

```solidity
event HoursReplenished(
    address indexed volunteer,
    uint256 replenishHours,
    uint256 timestamp,
    string recordHash
);
```

### RecordAdded
上链记录添加事件

```solidity
event RecordAdded(
    address indexed volunteer,
    uint256 hours,
    bool isReplenish,
    string recordHash,
    uint256 timestamp
);
```

## 后端集成

### 配置文件

```yaml
blockchain:
  contract-address: 0x你的合约地址
  rpc-url: http://127.0.0.1:8545  # 或测试网RPC
  admin-private-key: 你的私钥
```

### API接口

1. **同步总时长到链上**
   ```
   POST /api/blockchain/sync-hours
   Body: { "volunteerId": 1 }
   ```

2. **补录时长上链**
   ```
   POST /api/blockchain/replenish-hours
   Body: {
     "volunteerId": 1,
     "hours": "2.5",
     "recordHash": "QmXxx..."
   }
   ```

3. **活动记录上链**
   ```
   POST /api/blockchain/activity-record
   Body: {
     "volunteerId": 1,
     "hours": "3.0",
     "recordHash": "QmYxx..."
   }
   ```

4. **查询链上时长**
   ```
   GET /api/blockchain/total-hours/{volunteerId}
   ```

## 测试网络

### Sepolia 测试网
- RPC: https://sepolia.infura.io/v3/YOUR-PROJECT-ID
- 浏览器: https://sepolia.etherscan.io/
- 水龙头: https://sepoliafaucet.com/

### Goerli 测试网
- RPC: https://goerli.infura.io/v3/YOUR-PROJECT-ID
- 浏览器: https://goerli.etherscan.io/
- 水龙头: https://goerlifaucet.com/

## 安全注意事项

1. **私钥管理**
   - 永远不要将私钥提交到代码仓库
   - 使用环境变量或密钥管理服务
   - 生产环境使用硬件钱包或多签钱包

2. **Gas 费用**
   - 监控 Gas 价格，选择合适时机上链
   - 考虑使用批量操作减少交易次数
   - 预留足够的 ETH 用于交易费用

3. **合约升级**
   - 当前合约不支持升级
   - 如需升级，考虑使用代理模式（Proxy Pattern）

4. **权限管理**
   - 定期审查管理员权限
   - 考虑使用多签钱包作为管理员

## 常见问题

### Q: 为什么时长要乘以100？
A: Solidity 不支持小数，所以我们用整数表示。1小时 = 100单位，0.01小时 = 1单位。

### Q: 如何验证链上数据？
A: 使用区块浏览器（如 Etherscan）输入合约地址和交易哈希查看。

### Q: 上链失败怎么办？
A: 检查：
1. 管理员账户是否有足够的 ETH
2. RPC 节点是否正常
3. 合约地址是否正确
4. 志愿者是否绑定了钱包地址

### Q: 可以删除链上记录吗？
A: 不可以。区块链数据不可篡改，这正是其价值所在。

## 许可证

MIT License