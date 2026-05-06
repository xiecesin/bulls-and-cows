# Bulls and Cows 猜数字游戏

基于经典猜数字游戏的教育平台，支持小学生练习、推理逻辑展示、算法代码展示和变形研究。

## 项目简介

本项目是一个基于经典 Bulls and Cows（猜数字）游戏的教育平台：

- **练习游戏**：小学生可在 Web 端进行交互式猜数字练习
- **推理解析**：展示详细的解题推理过程
- **算法展示**：提供 Python/Java 多种解法
- **变形研究**：展示经典题目的多种变形及最新研究进展

## 技术栈

- **后端**：Spring Boot 3.2.0 + JDK 21
- **前端**：Vue 3 + Vite + Element Plus
- **架构**：前后端分离

## 项目结构

```
bulls-and-cows/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/bullscows/
│   │   ├── BullsCowsApplication.java
│   │   ├── controller/      # REST API 控制器
│   │   ├── service/          # 业务逻辑层
│   │   └── model/            # 数据模型
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── router/           # 路由配置
│   │   └── api/              # API 调用
│   └── package.json
└── README.md
```

## 快速开始

### 后端启动

```bash
cd backend

# 使用 Maven 启动
./mvnw spring-boot:run

# 或打包后运行
./mvnw package
java -jar target/bulls-and-cows-1.0.0.jar
```

后端服务将在 http://localhost:8080 启动

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:3000 启动

## 功能模块

### 1. 游戏练习
- 随机生成 4 位数字（可含重复）
- 实时显示 Bulls 和 Cows 数量
- 猜测历史记录

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

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/game/start | 开始新游戏 |
| POST | /api/game/guess | 提交猜测 |
| POST | /api/calculate | 直接计算结果 |
| GET | /api/algorithm/basic | 获取基础算法 |
| GET | /api/algorithms | 获取所有算法 |
| GET | /api/variants | 获取题目变形 |

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
