# 志愿者服务系统后端

## 项目简介

基于Spring Boot 3.2的志愿者服务管理系统后端，集成区块链技术实现志愿服务时长上链存证。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- MyBatis 3.0.3
- MySQL 5.7+
- Spring Security
- JWT认证
- Web3j (区块链交互)

## 项目结构

```
backend/
├── src/main/java/com/volunteer/
│   ├── VolunteerSystemApplication.java  # 主启动类
│   ├── common/                          # 通用类
│   │   └── Result.java                  # 统一响应结果
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   └── BlockchainConfig.java        # 区块链配置
│   ├── controller/                      # 控制器层
│   │   ├── AuthController.java          # 认证接口
│   │   ├── ActivityController.java      # 活动管理接口
│   │   └── CheckinController.java       # 签到签退接口
│   ├── entity/                          # 实体类
│   │   ├── Admin.java                   # 管理员
│   │   ├── Volunteer.java               # 志愿者
│   │   ├── Activity.java                # 活动
│   │   ├── ActivityApplication.java     # 活动报名
│   │   ├── ActivityCheckin.java         # 活动打卡
│   │   └── ...                          # 其他实体
│   ├── mapper/                          # MyBatis Mapper接口
│   │   ├── AdminMapper.java
│   │   ├── VolunteerMapper.java
│   │   └── ...
│   ├── service/                         # 服务层
│   │   ├── AuthService.java             # 认证服务
│   │   ├── ActivityService.java         # 活动服务
│   │   ├── CheckinService.java          # 签到服务
│   │   └── BlockchainService.java       # 区块链服务
│   └── utils/                           # 工具类
│       └── JwtUtil.java                 # JWT工具
└── src/main/resources/
    ├── application.yml                  # 配置文件
    └── mapper/                          # MyBatis XML映射文件
        └── AdminMapper.xml

```

## 核心功能模块

### 1. 认证模块
- 管理员登录
- 志愿者注册/登录
- JWT Token生成与验证

### 2. 活动管理模块
- 活动创建、修改、查询
- 活动报名申请
- 报名审核

### 3. 签到签退模块
- 活动签到（开始前15分钟至开始时间）
- 活动签退（结束前15分钟后）
- 自动计算服务时长和积分

### 4. 区块链模块
- 志愿服务时长上链
- 链上数据查询

## 配置说明

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_system
    username: root
    password: root

jwt:
  secret: your-secret-key
  expiration: 86400000

blockchain:
  contract-address: 0x...
  rpc-url: http://127.0.0.1:8545
  admin-private-key: your-private-key
```

## 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE volunteer_system CHARACTER SET utf8mb4;
```

2. 执行建表脚本：
```bash
mysql -u root -p volunteer_system < volunteer_system建表.sql
```

## 运行项目

### 开发环境

```bash
mvn spring-boot:run
```

### 生产环境

```bash
mvn clean package
java -jar target/volunteer-system-1.0.0.jar
```

## API接口

### 认证接口

- POST `/api/auth/admin/login` - 管理员登录
- POST `/api/auth/volunteer/login` - 志愿者登录
- POST `/api/auth/volunteer/register` - 志愿者注册

### 活动接口

- GET `/api/activity/list` - 获取活动列表
- GET `/api/activity/detail/{id}` - 获取活动详情
- POST `/api/activity/create` - 创建活动
- PUT `/api/activity/update` - 更新活动
- POST `/api/activity/apply` - 申请活动
- POST `/api/activity/apply/review` - 审核申请

### 签到接口

- POST `/api/checkin/in` - 签到
- POST `/api/checkin/out` - 签退

## 注意事项

1. 首次运行需要配置数据库连接
2. 区块链功能需要部署智能合约并配置合约地址
3. JWT密钥需要修改为安全的随机字符串
4. 生产环境需要配置HTTPS

## 后续扩展

- [ ] 完善其他实体的Mapper XML
- [ ] 添加更多业务Service
- [ ] 实现文件上传功能
- [ ] 添加数据统计接口
- [ ] 完善异常处理
- [ ] 添加日志记录
- [ ] 实现补录审核功能
- [ ] 完善区块链交互逻辑