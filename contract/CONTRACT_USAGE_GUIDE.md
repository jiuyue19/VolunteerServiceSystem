# VolunteerHours.sol 智能合约使用指南

## 📋 合约概述

**VolunteerHours.sol** 是一个基于以太坊的智能合约，用于管理志愿者服务时长记录。

### 核心特性

✅ **精确的时长记录**：所有时长以分钟为单位存储，精确到小数点后2位（0.01分钟）  
✅ **活动名称记录**：每条记录都包含志愿活动名称  
✅ **区分普通与补录**：明确区分志愿活动和补录活动  
✅ **批量操作支持**：支持批量添加活动记录  
✅ **自动统计汇总**：自动计算个人总时长  

---

## 🔢 时长存储格式

### 存储规则
- **存储单位**：分钟（乘以100保留2位小数）
- **转换公式**：
  ```
  存储值 = 实际分钟数 × 100
  实际分钟数 = 存储值 ÷ 100
  实际小时数 = 存储值 ÷ 6000
  ```

### 示例
| 实际时长 | 分钟数 | 存储值 |
|---------|--------|--------|
| 1小时30分 | 90.00分钟 | 9000 |
| 2小时15分 | 135.00分钟 | 13500 |
| 0小时45分 | 45.00分钟 | 4500 |
| 1小时30分30秒 | 90.50分钟 | 9050 |

---

## 📝 合约函数说明

### 1. addActivity - 添加单个志愿活动

```solidity
function addActivity(
    string memory activityName,  // 志愿活动名称
    address volunteer,            // 志愿者钱包地址
    uint256 minutes              // 时长（分钟×100）
) public onlyAdmin
```

**功能**：添加一条志愿活动记录  
**权限**：仅管理员  
**效果**：
- 记录活动名称和时长
- 累加到志愿者总时长
- 触发 `RecordAdded` 和 `DurationUpdated` 事件

**示例**（Remix）：
```javascript
// 添加 "社区清洁" 活动，时长1.5小时（90分钟）
addActivity("社区清洁", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb", 9000)
```

---

### 2. batchAddActivities - 批量添加志愿活动

```solidity
function batchAddActivities(
    string[] memory activityNames,  // 活动名称数组
    address[] memory volunteers,     // 志愿者地址数组
    uint256[] memory minutesArray   // 时长数组
) public onlyAdmin
```

**功能**：批量添加多条志愿活动记录  
**权限**：仅管理员  
**要求**：三个数组长度必须相等

**示例**（Remix）：
```javascript
// 批量添加3条记录
batchAddActivities(
    ["社区清洁", "敬老院服务", "义务支教"],
    [
        "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
        "0x8626f6940E2eb28930eFb4CeF49B2d1F2C9C1199",
        "0xdD870fA1b7C4700F2BD7f44238821C26f7392148"
    ],
    [9000, 12000, 6000]  // 1.5小时, 2小时, 1小时
)
```

---

### 3. addReplenishActivity - 添加单个补录活动

```solidity
function addReplenishActivity(
    string memory activityName,
    address volunteer,
    uint256 minutes
) public onlyAdmin
```

**功能**：添加一条补录活动记录  
**权限**：仅管理员  
**效果**：
- 记录补录活动名称和时长
- 累加到志愿者总时长和补录时长
- 触发 `DurationReplenished`、`RecordAdded` 和 `DurationUpdated` 事件

**示例**（Remix）：
```javascript
// 补录 "往期志愿活动" 2小时
addReplenishActivity("往期志愿活动", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb", 12000)
```

---

### 4. batchAddReplenishActivities - 批量添加补录活动

```solidity
function batchAddReplenishActivities(
    string[] memory activityNames,
    address[] memory volunteers,
    uint256[] memory minutesArray
) public onlyAdmin
```

**功能**：批量添加多条补录活动记录  
**权限**：仅管理员

**示例**（Remix）：
```javascript
// 批量补录3条记录
batchAddReplenishActivities(
    ["往期活动1", "往期活动2", "往期活动3"],
    [
        "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
        "0x8626f6940E2eb28930eFb4CeF49B2d1F2C9C1199",
        "0xdD870fA1b7C4700F2BD7f44238821C26f7392148"
    ],
    [6000, 9000, 12000]
)
```

---

## 🔍 查询函数

### getTotalMinutes - 查询总时长（分钟）

```solidity
function getTotalMinutes(address volunteer) public view returns (uint256)
```

**返回**：志愿者的总服务时长（分钟，乘以100）

**示例**：
```javascript
// 返回 9000 = 90分钟 = 1.5小时
getTotalMinutes("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
```

---

### getTotalHours - 查询总时长（小时）

```solidity
function getTotalHours(address volunteer) public view returns (uint256)
```

**返回**：志愿者的总服务时长（小时，除以60进行换算）

**注意**：此函数返回的是换算后的值，仍需除以100得到实际小时数

---

### getReplenishMinutes / getReplenishHours - 查询补录时长

```solidity
function getReplenishMinutes(address volunteer) public view returns (uint256)
function getReplenishHours(address volunteer) public view returns (uint256)
```

**功能**：查询志愿者的补录时长

---

### getRecord - 查询指定活动记录

```solidity
function getRecord(address volunteer, uint256 index) 
    public view returns (
        string memory activityName,
        uint256 minutes,
        uint256 timestamp,
        bool isReplenish
    )
```

**功能**：查询志愿者指定索引的活动记录

**示例**：
```javascript
// 查询第0条记录
getRecord("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb", 0)
```

---

### getAllRecords - 查询所有活动记录

```solidity
function getAllRecords(address volunteer) 
    public view returns (ActivityRecord[] memory)
```

**功能**：获取志愿者的所有活动记录

---

## 🎯 使用场景

### 场景1：添加新的志愿活动
```
1. 志愿者完成 "社区清洁" 活动，服务2小时30分（150分钟）
2. 管理员调用：addActivity("社区清洁", 志愿者地址, 15000)
3. 合约自动累加到志愿者总时长
```

### 场景2：批量录入多个志愿者的活动
```
1. 多个志愿者参加同一活动或不同活动
2. 管理员准备活动名称、地址、时长数组
3. 调用 batchAddActivities() 批量录入
4. 节省Gas费用，提高效率
```

### 场景3：补录历史活动
```
1. 志愿者有历史未上链的活动记录
2. 管理员调用 addReplenishActivity() 进行补录
3. 补录时长单独统计，也计入总时长
```

---

## ⚠️ 重要注意事项

### 1. 时长换算
后端在调用合约时必须进行换算：
```java
// 数据库存储：2.5小时
BigDecimal hours = new BigDecimal("2.5");

// 转换为分钟并乘以100
BigDecimal minutes = hours.multiply(new BigDecimal("60")).multiply(new BigDecimal("100"));
// minutes = 15000

// 调用合约
contract.addActivity("活动名称", "0x...", minutes.toBigInteger());
```

### 2. 精度问题
- Solidity 不支持浮点数，所有计算都是整数
- 后端需要在展示时除以100还原小数
- 前端显示：`9050 / 100 = 90.50分钟 = 1.51小时`

### 3. Gas 优化
- 批量操作比多次单个操作更节省Gas
- 建议累积多条记录后批量上链

### 4. 活动名称
- 活动名称不能为空字符串
- 建议使用简洁明确的活动名称
- 中文名称完全支持

---

## 🔧 Remix 部署步骤

1. 打开 [Remix IDE](https://remix.ethereum.org/)
2. 创建新文件 `VolunteerHours.sol`
3. 粘贴合约代码
4. 编译器选择 `0.8.0` 或更高版本
5. 部署合约
6. 复制合约地址到 `application.yml`

---

## 📊 事件监听

合约触发的事件：

### DurationUpdated
```solidity
event DurationUpdated(
    address indexed volunteer,
    uint256 totalMinutes,
    uint256 timestamp
);
```
**触发时机**：志愿者总时长更新时

### DurationReplenished
```solidity
event DurationReplenished(
    address indexed volunteer,
    uint256 replenishDuration,
    uint256 timestamp,
    string recordHash
);
```
**触发时机**：补录活动添加时

### RecordAdded
```solidity
event RecordAdded(
    address indexed volunteer,
    uint256 duration,
    bool isReplenish,
    string recordHash,
    uint256 timestamp
);
```
**触发时机**：任何活动记录添加时

---

## 📖 总结

此合约完全满足您的需求：
✅ 记录志愿活动名称、志愿者地址、志愿时长  
✅ 支持补录志愿活动  
✅ 批量添加功能  
✅ 自动统计总时长和补录时长  
✅ 精确到小数点后2位（0.01分钟）  
✅ 所有时长按 1小时=60分钟 计算  

合约已重新编写，请在 Remix 中重新部署并更新 `application.yml` 中的合约地址！
