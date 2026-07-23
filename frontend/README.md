# 志愿者服务系统前端

## 项目简介

基于 Vue 3 + Vite + Element Plus 的志愿者服务管理系统前端，支持管理员和志愿者两种角色。

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- Vite - 下一代前端构建工具
- Element Plus - Vue 3 组件库
- Vue Router - 路由管理
- Pinia - 状态管理
- Axios - HTTP 客户端
- Ethers.js - 以太坊 JavaScript 库

## 项目结构

```
frontend/
├── src/
│   ├── api/                    # API 接口
│   │   ├── auth.js            # 认证接口
│   │   ├── activity.js        # 活动接口
│   │   └── blockchain.js      # 区块链接口
│   ├── router/                # 路由配置
│   │   └── index.js
│   ├── utils/                 # 工具函数
│   │   └── request.js         # Axios 封装
│   ├── views/                 # 页面组件
│   │   ├── Login.vue          # 登录页
│   │   ├── Register.vue       # 注册页
│   │   ├── admin/             # 管理员页面
│   │   │   ├── Layout.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Activities.vue
│   │   │   ├── Applications.vue
│   │   │   └── Blockchain.vue
│   │   └── volunteer/         # 志愿者页面
│   │       ├── Layout.vue
│   │       ├── Home.vue
│   │       ├── Activities.vue
│   │       ├── MyApplications.vue
│   │       └── Certificate.vue
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
├── index.html
├── vite.config.js             # Vite 配置
└── package.json
```

## 功能模块

### 管理员端

1. **数据统计** - 查看系统整体数据
2. **活动管理** - 创建、编辑、删除活动
3. **报名审核** - 审核志愿者报名申请
4. **区块链管理** - 同步数据到区块链、查询链上数据

### 志愿者端

1. **首页** - 查看个人统计和最新活动
2. **活动中心** - 浏览和报名活动
3. **我的申请** - 查看申请状态、签到签退
4. **服务证书** - 绑定钱包、查看链上数据、生成证书

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 生产构建

```bash
npm run build
```

## 环境配置

### 后端 API 地址

在 `vite.config.js` 中配置代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### MetaMask 钱包

志愿者需要安装 MetaMask 浏览器插件才能使用区块链相关功能：

1. 安装 [MetaMask](https://metamask.io/)
2. 创建或导入钱包
3. 在系统中绑定钱包地址

## 主要页面说明

### 登录页面 (`/login`)

- 支持管理员和志愿者两种登录方式
- 使用 Tab 切换登录类型
- 登录成功后跳转到对应的首页

### 注册页面 (`/register`)

- 志愿者注册
- 表单验证（用户名、密码、邮箱等）
- 注册成功后跳转到登录页

### 管理员 - 活动管理

- 查看所有活动列表
- 创建新活动（标题、内容、时间、地址、人数、时长、积分）
- 编辑活动信息

### 管理员 - 区块链管理

- 输入志愿者ID同步总时长到区块链
- 查询志愿者的链上数据
- 显示交易哈希

### 志愿者 - 活动中心

- 浏览所有可报名的活动
- 查看活动详情（时间、地点、时长、积分）
- 一键报名活动

### 志愿者 - 我的申请

- 查看所有报名申请的状态
- 审核通过后可以签到和签退
- 签退后自动获得积分

### 志愿者 - 服务证书

- 绑定 MetaMask 钱包
- 查看链上服务时长数据
- 生成服务证书（基于区块链数据）

## API 接口说明

### 认证接口

```javascript
// 管理员登录
POST /api/auth/admin/login
Body: { username, password }

// 志愿者登录
POST /api/auth/volunteer/login
Body: { username, password }

// 志愿者注册
POST /api/auth/volunteer/register
Body: { username, password, realName, gender, phone, email }
```

### 活动接口

```javascript
// 获取活动列表
GET /api/activity/list

// 获取活动详情
GET /api/activity/detail/:id

// 创建活动
POST /api/activity/create

// 申请活动
POST /api/activity/apply
Body: { activityId, volunteerId }

// 签到
POST /api/checkin/in
Body: { activityId, volunteerId }

// 签退
POST /api/checkin/out
Body: { activityId, volunteerId }
```

### 区块链接口

```javascript
// 同步总时长到链上
POST /api/blockchain/sync-hours
Body: { volunteerId }

// 查询链上数据
GET /api/blockchain/total-hours/:volunteerId
```

## 注意事项

1. **Token 管理**
   - 登录成功后 Token 存储在 localStorage
   - 请求拦截器自动添加 Authorization 头
   - Token 过期自动跳转到登录页

2. **路由守卫**
   - 未登录用户访问受保护页面会跳转到登录页
   - 登录后根据用户类型跳转到对应首页

3. **区块链功能**
   - 需要安装 MetaMask 插件
   - 需要连接到正确的网络（测试网或主网）
   - 确保钱包有足够的 Gas 费用

4. **开发建议**
   - 使用 Vue DevTools 调试
   - 使用 Element Plus 组件库文档
   - 遵循 Vue 3 Composition API 规范

## 常见问题

### Q: 登录后页面空白？
A: 检查后端服务是否启动，查看浏览器控制台错误信息。

### Q: 无法连接 MetaMask？
A: 确保已安装 MetaMask 插件，并且允许网站访问钱包。

### Q: API 请求失败？
A: 检查 Vite 代理配置，确保后端地址正确。

### Q: 页面样式错乱？
A: 确保 Element Plus 样式已正确引入。

## 后续开发

- [ ] 添加状态管理（Pinia）
- [ ] 完善错误处理
- [ ] 添加加载动画
- [ ] 优化移动端适配
- [ ] 添加国际化支持
- [ ] 完善表单验证
- [ ] 添加单元测试

## 许可证

MIT License