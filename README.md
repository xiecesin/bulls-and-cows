# Bulls and Cows 猜数字游戏

基于经典猜数字游戏的教育平台，支持小学生练习、推理逻辑展示、算法代码展示和变形研究。

## 项目简介

本项目是一个基于经典 Bulls and Cows（猜数字）游戏的教育平台：

- **练习游戏**：小学生可在 Web 端进行交互式猜数字练习
- **推理解析**：展示详细的解题推理过程
- **算法展示**：提供 Python/Java 多种解法
- **变形研究**：展示经典题目的多种变形及最新研究进展
- **用户系统**：注册登录、练习记录、排行榜

## 技术栈

- **后端**：Spring Boot 3.2.0 + JDK 21 + Spring Security + JWT
- **前端**：Vue 3 + Vite + Element Plus + Pinia
- **数据库**：H2 (开发) / PostgreSQL (生产)
- **架构**：前后端分离 REST API

## 快速开始

### 一键启动（推荐）

```bash
# macOS / Linux
./start.sh

# Windows
start.bat
```

### 手动启动

**后端启动**

```bash
cd backend
./mvnw spring-boot:run
```

**前端启动（新开终端）**

```bash
cd frontend
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端 | http://localhost:8080 |
| H2 控制台 | http://localhost:8080/h2-console |

### 默认账户

| 类型 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | testuser | password123 |
| 管理员 | admin | admin123 |

## 项目结构

```
bulls-and-cows/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/bullscows/
│   │   ├── BullsCowsApplication.java
│   │   ├── config/         # 配置类
│   │   ├── controller/     # REST API 控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── repository/     # 数据访问层
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   └── model/         # 领域模型
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── views/         # 页面组件
│   │   ├── stores/        # Pinia 状态
│   │   ├── router/        # 路由配置
│   │   └── components/    # 通用组件
│   └── package.json
├── docs/                   # 文档目录
│   ├── getting-started.md # 快速开始
│   ├── deployment.md      # 部署指南
│   ├── architecture.md    # 系统设计
│   └── api.md             # API 文档
├── start.sh               # 启动脚本 (Linux/Mac)
├── stop.sh                # 停止脚本 (Linux/Mac)
├── start.bat              # 启动脚本 (Windows)
└── stop.bat              # 停止脚本 (Windows)
```

## 功能模块

### 1. 游戏练习
- 随机生成 4 位数字（可含重复）
- 实时显示 Bulls 和 Cows 数量
- 猜测历史记录
- 用时统计

### 2. 推理解析
- 逐位对比分析
- 推理过程详解
- 可视化匹配状态

### 3. 算法展示
- Java 基础算法
- Java 优化算法
- Python 实现
- 哈希表算法

### 4. 变形研究
- 经典 4 位数版本
- N 位通用版本
- 颜色序列版
- 多人协作版
- AI 智能猜测
- Mastermind 反向版

### 5. 用户中心
- 用户注册/登录
- 个人资料管理
- 练习记录查看
- 统计数据展示

### 6. 排行榜
- 猜测次数榜
- 速度排行榜
- 我的排名展示

## 完整文档

详细的开发、部署、设计文档请查看 `docs/` 目录：

- [快速开始指南](./docs/getting-started.md) - 环境配置、启动方式、常见问题
- [部署文档](./docs/deployment.md) - 生产环境部署、Docker、Nginx、HTTPS
- [系统设计](./docs/architecture.md) - 架构设计、数据库设计、安全设计
- [API 文档](./docs/api.md) - 完整接口文档、请求响应示例

## 游戏规则

- 系统随机生成一个 4 位数字（可能有重复数字）
- 玩家每次猜测一个 4 位数字
- 系统返回 Bulls（A）和 Cows（B）的数量：
  - **Bulls**：数字和位置都对了
  - **Cows**：数字对了但位置不对
- 猜对所有数字（4A0B）即为胜利

## 示例

```
秘密数字: 1123
猜测数字: 0111

结果: 1A1B

解释：
- 第1位：1 vs 0 → 不匹配
- 第2位：1 vs 1 → Bull
- 第3位：2 vs 1 → Cow
- 第4位：3 vs 1 → Cow
```

## License

MIT License
