# Bulls and Cows 系统设计文档

## 1. 系统概述

Bulls and Cows 是一个基于经典猜数字游戏的教育平台，支持小学生练习、推理逻辑展示、算法代码展示和变形研究。

### 1.1 核心功能

- **练习游戏**：交互式猜数字练习
- **推理解析**：详细解题推理过程展示
- **算法展示**：多种编程语言的解法
- **变形研究**：经典题目的多种变形

### 1.2 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.2.0 |
| 安全框架 | Spring Security + JWT |
| 数据库 | H2 (开发) / PostgreSQL (生产) |
| ORM | Spring Data JPA |
| 前端框架 | Vue 3 |
| 构建工具 | Vite |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端 (Vue 3)                        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────────┐ │
│  │ 页面组件 │ │ 路由    │ │ 状态管理 │ │ API 请求       │ │
│  └─────────┘ └─────────┘ └─────────┘ └─────────────────┘ │
└────────────────────────────┬────────────────────────────┘
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────┐
│                    后端 (Spring Boot)                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │              Spring Security + JWT              │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐     │
│  │ 控制器层    │ │  服务层     │ │  数据访问层     │     │
│  │ Controller  │ │  Service   │ │  Repository    │     │
│  └─────────────┘ └─────────────┘ └─────────────────┘     │
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│                    数据库 (H2/PostgreSQL)               │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐   │
│  │  用户   │ │  练习记录 │ │  排名   │ │  会话      │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 3. 数据库设计

### 3.1 ER 图

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│    User      │       │  PracticeRecord  │       │   Ranking    │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ id (PK)      │──────<│ userId (FK)      │       │ id (PK)      │
│ username     │       │ id (PK)          │       │ userId (FK)  │
│ password     │       │ secretNumber      │       │ rankingType  │
│ email        │       │ guessCount       │       │ rankPosition │
│ nickname     │       │ timeSpentMs      │       │ bestScore    │
│ role         │       │ allowDuplicates  │       │ recordId     │
│ createdAt    │       │ gameResult       │       │ achievedAt   │
│ lastLoginAt  │       │ participatedRanking              │
└──────────────┘       │ createdAt        │       └──────────────┘
                       │ completedAt      │
                       └──────────────────┘

┌──────────────────┐
│   UserSession    │
├──────────────────┤
│ id (PK)          │
│ userId (FK)      │
│ refreshToken     │
│ deviceInfo       │
│ ipAddress        │
│ createdAt        │
│ expiresAt        │
│ revoked          │
└──────────────────┘
```

### 3.2 表结构

#### User 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| email | VARCHAR(100) | 邮箱 |
| nickname | VARCHAR(50) | 昵称 |
| avatar_url | VARCHAR(500) | 头像 URL |
| role | VARCHAR(20) | 角色：USER, ADMIN |
| enabled | BOOLEAN | 账户启用状态 |
| created_at | TIMESTAMP | 创建时间 |
| last_login_at | TIMESTAMP | 最后登录时间 |

#### PracticeRecord 练习记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID，外键 |
| secret_number | VARCHAR(10) | 目标数字 |
| guess_count | INT | 猜测次数 |
| time_spent_ms | BIGINT | 用时（毫秒） |
| allow_duplicates | BOOLEAN | 是否允许重复 |
| participated_ranking | BOOLEAN | 是否参与排名 |
| ranking_type | VARCHAR(20) | 排名类型 |
| game_result | VARCHAR(20) | 游戏结果：WIN, QUIT |
| created_at | TIMESTAMP | 创建时间 |
| completed_at | TIMESTAMP | 完成时间 |

#### Ranking 排名表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID，外键 |
| ranking_type | VARCHAR(20) | 排名类型 |
| rank_position | INT | 排名位置 |
| best_score | INT | 最佳成绩 |
| record_id | BIGINT | 来源记录 ID |
| achieved_at | TIMESTAMP | 达成时间 |

#### UserSession 会话表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID，外键 |
| refresh_token | VARCHAR(255) | 刷新令牌 |
| device_info | VARCHAR(200) | 设备信息 |
| ip_address | VARCHAR(50) | IP 地址 |
| created_at | TIMESTAMP | 创建时间 |
| expires_at | TIMESTAMP | 过期时间 |
| revoked | BOOLEAN | 是否撤销 |

## 4. 安全设计

### 4.1 JWT 认证流程

```
┌────────┐                    ┌─────────┐                    ┌────────┐
│ Client │                    │ Server  │                    │  DB    │
└───┬────┘                    └────┬────┘                    └───┬────┘
    │                               │                              │
    │  1. POST /api/auth/login     │                              │
    │  {username, password}       │                              │
    │────────────────────────────>│                              │
    │                               │                              │
    │                               │  2. 查询用户验证密码          │
    │                               │─────────────────────────────>│
    │                               │                              │
    │                               │  3. 生成 Token               │
    │                               │<─────────────────────────────│
    │                               │                              │
    │  4. 返回 {token, refreshToken}│                              │
    │<─────────────────────────────│                              │
    │                               │                              │
    │  5. 后续请求携带 Token        │                              │
    │  Authorization: Bearer xxx   │                              │
    │─────────────────────────────>│                              │
    │                               │                              │
    │                               │  6. JWT 验证过滤器            │
    │                               │  提取用户信息                 │
    │                               │                              │
    │  7. 返回受保护资源           │                              │
    │<─────────────────────────────│                              │
    │                               │                              │
```

### 4.2 Token 结构

**Access Token**
- 有效期：15 分钟
- 包含：用户 ID、用户名、角色

**Refresh Token**
- 有效期：7 天
- 存储：数据库
- 用途：续期 Access Token

### 4.3 安全配置

- 密码 BCrypt 加密（强度 12）
- CORS 跨域配置
- CSRF 禁用（使用 JWT）
- XSS 防护头
- 请求限流

## 5. API 设计

### 5.1 认证相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/register | 用户注册 | 否 |
| POST | /api/auth/login | 用户登录 | 否 |
| POST | /api/auth/refresh | 刷新 Token | 否 |
| POST | /api/auth/logout | 退出登录 | 是 |

### 5.2 用户相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/users/me | 获取当前用户 | 是 |
| PUT | /api/users/me | 更新用户信息 | 是 |
| PUT | /api/users/me/password | 修改密码 | 是 |

### 5.3 游戏相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/game/start | 开始新游戏 | 否 |
| POST | /api/game/guess | 提交猜测 | 否 |
| GET | /api/game/{id}/answer | 查看答案 | 否 |
| POST | /api/calculate | 直接计算结果 | 否 |
| POST | /api/puzzle/generate | 生成谜题 | 否 |
| POST | /api/puzzle/verify | 验证谜题答案 | 否 |

### 5.4 记录相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/record/save | 保存练习记录 | 是 |
| GET | /api/record/list | 获取记录列表 | 是 |
| GET | /api/record/stats | 获取统计数据 | 是 |

### 5.5 排行榜相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/ranking | 获取排行榜 | 否 |
| GET | /api/ranking/me | 获取我的排名 | 是 |

## 6. 前端架构

### 6.1 目录结构

```
frontend/src/
├── api/              # API 接口模块
│   ├── index.js      # Axios 配置
│   ├── auth.js       # 认证接口
│   └── record.js     # 记录接口
├── components/       # 公共组件
├── router/          # 路由配置
│   └── index.js      # 路由守卫
├── stores/           # Pinia 状态管理
│   └── user.js       # 用户状态
├── views/            # 页面组件
│   ├── Home.vue      # 首页
│   ├── Game.vue      # 游戏页
│   ├── Puzzle.vue    # 谜题页
│   ├── Reasoning.vue # 推理页
│   ├── Algorithm.vue # 算法页
│   ├── Variants.vue  # 变形页
│   ├── Profile.vue   # 个人中心
│   ├── Records.vue   # 练习记录
│   ├── Ranking.vue   # 排行榜
│   └── auth/         # 认证页面
│       ├── Login.vue
│       └── Register.vue
├── App.vue           # 根组件
└── main.js          # 入口文件
```

### 6.2 状态管理

使用 Pinia 管理用户状态：

```javascript
// stores/user.js
{
  userId: null,
  username: '',
  nickname: '',
  email: '',
  role: 'USER',
  isLoggedIn: false,
  isAdmin: false
}
```

### 6.3 路由守卫

- `guestOnly`: 仅未登录用户可访问（登录页、注册页）
- `requiresAuth`: 需要登录才能访问（个人中心、记录页）
- `requiresAdmin`: 需要管理员权限

## 7. 游戏规则与算法

### 7.1 Bulls and Cows 规则

- 系统生成 4 位随机数字
- 玩家猜测 4 位数字
- 系统返回 Bulls（位置和数字都对）和 Cows（数字对但位置错）
- 4 Bulls 表示胜利

### 7.2 核心算法

```java
public static String calculateHint(String secret, String guess) {
    int bulls = 0;
    int cows = 0;
    int[] count = new int[10];

    // 第一遍：统计 Bulls
    for (int i = 0; i < 4; i++) {
        if (secret.charAt(i) == guess.charAt(i)) {
            bulls++;
        } else {
            count[secret.charAt(i) - '0']++;
        }
    }

    // 第二遍：统计 Cows
    for (int i = 0; i < 4; i++) {
        if (secret.charAt(i) != guess.charAt(i) &&
            count[guess.charAt(i) - '0'] > 0) {
            cows++;
            count[guess.charAt(i) - '0']--;
        }
    }

    return bulls + "A" + cows + "B";
}
```

## 8. 扩展设计

### 8.1 多人协作模式

- 创建房间
- 邀请好友
- 轮流猜测
- 协作推理

### 8.2 成就系统

- 首次胜利
- 连胜记录
- 速度成就
- 次数成就

### 8.3 社交功能

- 好友系统
- 挑战赛
- 分享成绩
