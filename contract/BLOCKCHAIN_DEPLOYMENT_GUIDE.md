# 区块链证书系统部署完整指南

## 📋 目录
- [部署流程概览](#部署流程概览)
- [步骤1：部署智能合约](#步骤1部署智能合约)
- [步骤2：配置后端服务](#步骤2配置后端服务)
- [步骤3：配置前端应用](#步骤3配置前端应用)
- [步骤4：绑定钱包地址](#步骤4绑定钱包地址)
- [步骤5：测试上链功能](#步骤5测试上链功能)
- [常见问题](#常见问题)

---

## 🎯 部署流程概览

```
Remix IDE (合约部署) 
    ↓ 获取合约地址
后端配置 (application.yml)
    ↓ 配置RPC和私钥
前端应用 (MetaMask连接)
    ↓ 绑定钱包地址
管理员操作 (数据上链)
    ↓ 区块链存储
志愿者查看 (证书生成)
```

---

## 步骤1：部署智能合约

### 1.1 启动 Geth 私链

创建启动脚本 `start-geth.bat`：

```batch
geth --datadir D:\Geth\PrivateChain\datadir ^
 --networkid 2025 ^
 --http --http.addr 127.0.0.1 --http.port 8545 ^
 --http.api eth,net,web3,personal,miner,txpool ^
 --http.corsdomain "*" ^
 --allow-insecure-unlock ^
 --unlock 0xbc7567a0055ecf24a908f526eb51a5cfc214de61 ^
 --password D:\Geth\PrivateChain\password.txt ^
 --miner.gasprice 0 ^
 --miner.gaslimit 30000000 ^
 --mine --miner.threads 1 ^
 --miner.etherbase 0xbc7567a0055ecf24a908f526eb51a5cfc214de61 ^
 console
```

**重要参数说明：**
- `--miner.gasprice 0`: 私链环境使用零 gas 价格
- `--http.corsdomain "*"`: 允许 Remix 和 MetaMask 跨域连接
- `--unlock`: 解锁管理员账户用于合约部署

### 1.2 配置 MetaMask

1. **添加自定义网络**
   - 网络名称：`Geth Private Chain`
   - RPC URL：`http://127.0.0.1:8545`
   - 链 ID：`2025`
   - 货币符号：`ETH`

2. **导入管理员账户**
   - 使用私钥导入已解锁的账户
   - 确保账户有足够余额（在 genesis.json 中预分配）

### 1.3 在 Remix 中部署合约

1. **打开 Remix IDE** (https://remix.ethereum.org)

2. **创建合约文件**
   - 新建文件：`VolunteerHours.sol`
   - 复制 `contract/VolunteerHours.sol` 的内容

3. **编译合约**
   - 进入 Solidity Compiler 标签
   - 选择编译器版本：`0.8.20` 或更高
   - **重要**：在 Advanced Configurations 中，将 EVM VERSION 改为 **`london`** 或 **`paris`**
   - 点击 "Compile VolunteerHours.sol"

4. **部署合约**
   - 进入 Deploy & Run Transactions 标签
   - Environment：选择 **`Injected Provider - MetaMask`**
   - 确认 MetaMask 连接到私链（账户地址和网络正确）
   - 点击 **Deploy** 按钮
   - 在 MetaMask 中确认交易

5. **获取合约地址**
   - 部署成功后，在 Deployed Contracts 区域会显示合约
   - 复制合约地址（例如：`0x2f4179604a913C7b286b3aCb3F9727208a858A64`）
   - **保存这个地址，后面配置需要使用**

---

## 步骤2：配置后端服务

### 2.1 更新 `application.yml`

编辑 `backend/src/main/resources/application.yml`，找到 `blockchain` 配置节：

```yaml
blockchain:
  contract-address: 0x2f4179604a913C7b286b3aCb3F9727208a858A64  # 替换为你的合约地址
  rpc-url: http://127.0.0.1:8545
  admin-private-key: 你的管理员账户私钥  # 从 MetaMask 导出
```

**获取私钥方法：**
1. 打开 MetaMask
2. 点击账户详情 → 导出私钥
3. 输入密码后复制私钥
4. **注意：私钥以 `0x` 开头，保留完整字符串**

### 2.2 确认依赖

确保 `backend/pom.xml` 包含 Web3j 依赖：

```xml
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>4.9.8</version>
</dependency>
```

### 2.3 重启后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

---

## 步骤3：配置前端应用

### 3.1 安装 MetaMask 浏览器插件

如果还未安装：
- Chrome: https://chrome.google.com/webstore (搜索 MetaMask)
- Firefox: https://addons.mozilla.org/firefox (搜索 MetaMask)

### 3.2 确认前端依赖

前端已包含必要的库：
- `ethers.js` 或 `web3.js` (用于区块链交互)
- `html2canvas` (用于证书导出)

### 3.3 启动前端应用

```bash
cd frontend
npm install
npm run dev
```

---

## 步骤4：绑定钱包地址

### 4.1 志愿者绑定钱包

**方式一：通过志愿者端界面**
1. 志愿者登录系统
2. 进入 "服务证书" 页面
3. 点击 "钱包管理" → "连接 MetaMask"
4. 在 MetaMask 中确认连接
5. 系统自动保存钱包地址到数据库

**方式二：管理员手动配置**
如果志愿者没有钱包，管理员可以：
1. 为志愿者创建 MetaMask 钱包
2. 在管理后台 "志愿者管理" 中，编辑志愿者信息
3. 填入钱包地址（`wallet_address` 字段）

### 4.2 验证绑定

检查数据库：
```sql
SELECT id, real_name, wallet_address FROM volunteer WHERE id = 1;
```

应该看到类似：
```
id | real_name | wallet_address
1  | 张三      | 0xBC7567A0055ECF24A908f526Eb51A5cfc214de61
```

---

## 步骤5：测试上链功能

### 5.1 管理员端上链测试

1. **登录管理员账户**
2. **进入 "区块链证书管理" 页面**
3. **测试数据上链**
   - 在 "数据上链" 卡片中
   - 输入志愿者 ID（例如：1）
   - 点击 "同步上链" 按钮
   - 等待交易确认

4. **查看交易结果**
   - 成功后会显示交易哈希（txHash）
   - 可以在 Geth 控制台查看：
     ```javascript
     eth.getTransaction("0x交易哈希")
     ```

### 5.2 查询链上数据

1. **在同一页面，找到 "查询链上数据" 卡片**
2. **输入志愿者 ID**
3. **点击 "查询" 按钮**
4. **验证结果**
   - 链上总时长 = 数据库总时长 ✅
   - 数据一致性：一致 ✅

### 5.3 志愿者端查看证书

1. **登录志愿者账户**
2. **进入 "服务证书" 页面**
3. **连接钱包**（如果还未连接）
4. **点击 "查看链上数据"**
5. **生成并下载证书**
   - 点击 "生成并下载证书" 按钮
   - 证书会自动下载为 PNG 图片
   - 包含区块链验证信息

---

## 🔧 常见问题

### Q1: Remix 部署时报 "Gas estimation failed"

**原因**：gas 价格或 gas limit 设置不当

**解决方案**：
1. 确保 Geth 启动时使用了 `--miner.gasprice 0`
2. 在 Remix 中手动设置 Gas Limit 为 5000000
3. 重新部署

### Q2: 后端报错 "交易失败: insufficient funds"

**原因**：管理员账户余额不足

**解决方案**：
1. 在 genesis.json 中为管理员地址预分配足够的 ETH
2. 或在 Geth 控制台中转账：
   ```javascript
   eth.sendTransaction({from: eth.coinbase, to: "管理员地址", value: web3.toWei(1000, "ether")})
   ```

### Q3: MetaMask 无法连接到私链

**原因**：RPC 端口或 CORS 配置问题

**解决方案**：
1. 确保 Geth 启动时包含 `--http.corsdomain "*"`
2. 检查防火墙是否阻止 8545 端口
3. 在 MetaMask 中重新添加网络

### Q4: EVM 版本不兼容（invalid opcode）

**原因**：合约编译的 EVM 版本高于私链支持的版本

**解决方案**：
1. 在 Remix 编译器设置中，将 EVM VERSION 改为 **`london`** 或 **`paris`**
2. 重新编译和部署合约
3. **不要使用** `shanghai` 除非私链配置了 Shanghai 硬分叉

### Q5: 志愿者查询链上数据返回 0

**可能原因**：
1. 钱包地址未绑定
2. 数据还未上链
3. 合约地址配置错误

**排查步骤**：
1. 检查数据库中 `volunteer.wallet_address` 是否正确
2. 确认管理员已执行过 "同步上链" 操作
3. 验证 `application.yml` 中的 `contract-address` 与部署的合约地址一致

### Q6: 前端连接 MetaMask 后没有反应

**解决方案**：
1. 确保 MetaMask 已切换到正确的网络（Geth Private Chain）
2. 刷新页面并重新连接
3. 检查浏览器控制台是否有 JavaScript 错误

---

## 📊 数据流程图

```
数据库 (volunteer.total_hours = 10.5)
    ↓
管理员点击"同步上链"
    ↓
后端调用 BlockchainService.updateTotalHours()
    ↓
Web3j 发送交易到 Geth RPC (http://127.0.0.1:8545)
    ↓
智能合约 VolunteerHours.updateTotalHours() 执行
    ↓
区块链存储: totalDuration[志愿者地址] = 1050 (10.5*100)
    ↓
返回交易哈希给管理员
    ↓
志愿者查询链上数据
    ↓
后端调用 BlockchainService.getTotalHours()
    ↓
智能合约返回: 1050 → 转换为 10.5 小时
    ↓
前端显示: 链上总时长 10.5 小时 ✅
```

---

## 🛡️ 安全建议

### 开发环境
- ✅ 可以使用 `--http.corsdomain "*"`
- ✅ 可以使用 `--allow-insecure-unlock`
- ✅ 私钥可以明文配置（仅限本地开发）

### 生产环境
- ❌ **禁止** 使用 `--http.corsdomain "*"`，指定具体域名
- ❌ **禁止** 使用 `--allow-insecure-unlock`
- ❌ **禁止** 明文存储私钥，使用环境变量或密钥管理服务
- ✅ 使用正式的以太坊网络（主网或测试网）
- ✅ 启用 HTTPS 和防火墙规则

---

## 📝 配置检查清单

部署前请确认：

- [ ] Geth 私链已启动并正常挖矿
- [ ] MetaMask 已连接到私链网络
- [ ] VolunteerHours 合约已成功部署
- [ ] application.yml 中配置了正确的合约地址
- [ ] application.yml 中配置了管理员私钥
- [ ] 数据库中志愿者已绑定钱包地址
- [ ] 后端服务已重启并无报错
- [ ] 前端应用已启动
- [ ] MetaMask 浏览器插件已安装

---

## 🎉 部署成功标志

当你能够完成以下操作时，说明部署成功：

1. ✅ 管理员可以在界面上同步志愿者数据到区块链
2. ✅ 系统返回交易哈希
3. ✅ 查询链上数据显示正确的时长
4. ✅ 链上数据与数据库数据一致
5. ✅ 志愿者可以连接钱包并查看自己的链上数据
6. ✅ 志愿者可以生成包含区块链验证信息的证书

恭喜！你的区块链志愿服务证书系统已成功部署！🎊
