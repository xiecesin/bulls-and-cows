# Bulls and Cows 增强功能设计文档

**版本**: v1.1
**日期**: 2026-05-06
**状态**: 已确认

---

## 1. 整体架构

### 1.1 技术选型

| 组件 | 技术方案 |
|------|----------|
| 后端框架 | Spring Boot 3 + Spring Security + JWT |
| 数据库 | H2（MySQL兼容模式） |
| 前端框架 | Vue 3 + Vite + Element Plus + Pinia |
| 认证方式 | 用户名/密码 + 微信登录 + Github 登录 |
| 实时通信 | WebSocket（多人协作） |

### 1.2 项目模块划分

```
backend/
├── config/              # 配置（Security、跨域、OAuth2等）
├── controller/          # REST API 控制器
│   ├── AuthController.java      # 认证相关
│   ├── UserController.java      # 用户管理
│   ├── GameController.java     # 游戏逻辑（现有）
│   ├── PracticeController.java # 练习记录
│   ├── RankingController.java  # 排名系统
│   ├── CollaborationController.java # 多人协作
│   └── AdminController.java    # 管理员功能
├── service/             # 业务逻辑层
├── repository/          # 数据访问层（JPA）
├── model/               # 数据模型
└── entity/              # JPA 实体类

frontend/src/views/
├── auth/               # 登录/注册页面
├── game/               # 游戏相关（现有）
├── user/               # 用户中心
│   ├── Profile.vue           # 个人资料
│   └── PracticeHistory.vue  # 练习记录
├── ranking/            # 排名页面
├── collaboration/      # 多人协作
└── admin/              # 管理后台
    ├── Dashboard.vue          # 管理后台首页
    ├── UserManagement.vue     # 用户管理
    ├── RankingsManagement.vue # 排名管理
    └── SystemMonitor.vue      # 系统监控
```

---

## 2. 数据模型

### 2.1 数据库表结构

#### 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 加密密码（第三方登录为null） |
| nickname | VARCHAR(100) | 昵称 |
| avatar_url | VARCHAR(500) | 头像URL |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | USER / ADMIN |
| status | VARCHAR(20) | ACTIVE / BANNED |
| oauth_provider | VARCHAR(20) | LOCAL / WECHAT / GITHUB |
| oauth_id | VARCHAR(100) | 第三方用户ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 练习记录表 (practice_records)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| secret_number | VARCHAR(10) | 目标数字 |
| guess_count | INT | 猜测次数 |
| time_spent_ms | BIGINT | 用时（毫秒） |
| allow_duplicates | BOOLEAN | 是否允许重复 |
| participated_ranking | BOOLEAN | 是否参与排名 |
| ranking_type | VARCHAR(20) | GUESS_COUNT / TIME |
| game_result | VARCHAR(20) | WIN / QUIT |
| completed_at | TIMESTAMP | 完成时间 |
| created_at | TIMESTAMP | 创建时间 |

#### 排名表 (rankings)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| ranking_type | VARCHAR(20) | GUESS_COUNT / TIME |
| rank_position | INT | 排名位置 |
| best_score | INT | 最佳成绩 |
| record_id | BIGINT | 来源记录ID |
| achieved_at | TIMESTAMP | 达成时间 |

#### 多人协作房间表 (collaboration_rooms)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| room_code | VARCHAR(20) | 房间邀请码（唯一） |
| mode | VARCHAR(20) | TURN / RACE / HYBRID |
| secret_number | VARCHAR(10) | 目标数字 |
| status | VARCHAR(20) | WAITING / PLAYING / FINISHED |
| created_by | BIGINT | 创建者ID |
| created_at | TIMESTAMP | 创建时间 |
| finished_at | TIMESTAMP | 结束时间 |

#### 房间玩家表 (room_players)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| room_id | BIGINT | 房间ID |
| user_id | BIGINT | 用户ID |
| turn_order | INT | 轮流制顺序 |
| is_ready | BOOLEAN | 是否准备 |
| joined_at | TIMESTAMP | 加入时间 |

#### 房间猜测记录表 (room_guesses)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| room_id | BIGINT | 房间ID |
| user_id | BIGINT | 用户ID |
| guess_number | VARCHAR(10) | 猜测数字 |
| bulls | INT | Bull数量 |
| cows | INT | Cow数量 |
| is_winner | BOOLEAN | 是否获胜 |
| guessed_at | TIMESTAMP | 猜测时间 |

#### 在线用户会话表 (user_sessions)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| token_hash | VARCHAR(100) | Token哈希 |
| ip_address | VARCHAR(50) | IP地址 |
| user_agent | TEXT | 浏览器信息 |
| login_at | TIMESTAMP | 登录时间 |
| last_active_at | TIMESTAMP | 最后活跃时间 |
| expires_at | TIMESTAMP | 过期时间 |

---

## 3. 认证与安全

### 3.1 认证流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户认证流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐                 │
│  │  登录页面  │────▶│  JWT验证  │────▶│  获得Token │                 │
│  └──────────┘     └──────────┘     └──────────┘                 │
│       │                                    │                      │
│       │  ┌─────────────────────────────────┘                      │
│       ▼  ▼                                                        │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐                 │
│  │ 第三方登录 │────▶│ OAuth2   │────▶│ 绑定/创建 │                 │
│  │微信/Github│     │ 回调处理  │     │   账户    │                 │
│  └──────────┘     └──────────┘     └──────────┘                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 权限控制

| 接口 | 普通用户 | 管理员 |
|------|---------|--------|
| 查看排名（Top 3 + 自己） | ✅ | ✅ |
| 查看完整排名 | ❌ | ✅ |
| 查看自己的练习记录 | ✅ | ✅ |
| 删除自己的练习记录 | ✅ | ✅ |
| 修改自己的密码/昵称 | ✅ | ✅ |
| 查看所有用户列表 | ❌ | ✅ |
| 管理任意用户 | ❌ | ✅ |
| 查看系统监控 | ❌ | ✅ |

### 3.3 安全策略

- 密码使用 BCrypt 加密
- JWT Token 有效期 7 天（可配置）
- Refresh Token 有效期 30 天
- 失败登录锁定（5次/15分钟）
- SQL 注入/XSS 防护

### 3.4 API 接口设计

#### 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 退出登录 |
| POST | /api/auth/refresh | 刷新Token |
| GET | /api/auth/oauth/{provider} | 发起OAuth登录 |
| GET | /api/auth/oauth/callback | OAuth回调 |

#### 用户相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/me | 获取当前用户信息 |
| PUT | /api/users/me | 更新个人信息 |
| PUT | /api/users/me/password | 修改密码 |
| GET | /api/users/me/records | 获取练习记录 |
| DELETE | /api/users/me/records/{id} | 删除练习记录 |

#### 排名相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/rankings/guess-count | 猜测次数排名 |
| GET | /api/rankings/time | 用时排名 |
| GET | /api/rankings/all | 完整排名（仅管理员） |

#### 管理员相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/users | 用户列表 |
| PUT | /api/admin/users/{id} | 更新用户 |
| DELETE | /api/admin/users/{id} | 删除用户 |
| PUT | /api/admin/users/{id}/password | 重置用户密码 |
| GET | /api/admin/sessions | 在线用户会话 |
| GET | /api/admin/stats | 系统运行统计 |

---

## 4. 前端页面设计

### 4.1 页面结构

```
frontend/src/views/
├── auth/
│   ├── Login.vue          # 登录页
│   └── Register.vue       # 注册页
├── user/
│   ├── Profile.vue        # 个人中心
│   └── PracticeHistory.vue # 练习记录
├── ranking/
│   └── Ranking.vue        # 排名页面
├── collaboration/
│   ├── RoomList.vue       # 房间列表
│   ├── CreateRoom.vue      # 创建房间
│   └── GameRoom.vue       # 协作游戏
└── admin/
    ├── Dashboard.vue       # 管理后台首页
    ├── UserManagement.vue  # 用户管理
    └── SystemMonitor.vue   # 系统监控
```

### 4.2 排名展示

- 头像 + 昵称 + 详细数据
- 前3名特殊图标标识
- 用户可查看前3名及自己的完整排名

---

## 5. 多人协作游戏

### 5.1 房间模式

| 模式 | 规则 | 适用场景 |
|------|------|----------|
| 轮流制 (TURN) | 玩家按顺序轮流猜测 | 团队协作 |
| 竞赛制 (RACE) | 同时独立猜测，谁先猜出谁赢 | 竞速挑战 |
| 混合制 (HYBRID) | 先到先得 + 轮流验证 | 综合体验 |

### 5.2 功能流程

1. 创建房间 → 选择模式 → 生成邀请码
2. 分享邀请码 → 其他玩家加入
3. 房主开始游戏 → 游戏进行
4. 显示结果 → 结算

---

## 6. 排名系统

### 6.1 排名类型

| 排名类型 | 计算方式 |
|----------|----------|
| 猜测次数排名 | 最少次数最优 |
| 用时排名 | 最短用时最优 |

### 6.2 排名徽章

| 排名 | 徽章 | 称号 |
|------|------|------|
| 🥇 第1名 | 金牌 | 王者 |
| 🥈 第2名 | 银牌 | 大师 |
| 🥉 第3名 | 铜牌 | 高手 |

### 6.3 参与规则

- 游戏结束后可选择参与排名
- 每个用户各排名最多一条记录
- 可选择不参与排名（私人练习）

---

## 7. 系统监控

### 7.1 监控指标

| 类别 | 指标 |
|------|------|
| 用户统计 | 在线用户数、今日新增、总用户数 |
| 业务统计 | 今日练习次数、参与排名数、房间数 |
| 系统资源 | CPU、内存、磁盘、运行时间 |
| 性能指标 | 响应时间、请求成功率、连接数 |

### 7.2 在线用户会话

- 用户名、IP地址、登录时间、最后活跃时间
- 管理员可踢出用户

---

## 8. 实施计划

### 阶段一：用户认证系统
1. 添加 Spring Security + JWT 依赖
2. 创建用户实体和 Repository
3. 实现注册、登录、Token 管理
4. 前端登录/注册页面

### 阶段二：用户中心与练习记录
1. 个人资料页面
2. 练习记录 CRUD
3. 关联练习记录到用户

### 阶段三：管理员功能
1. 用户管理页面
2. 用户增删改查 API
3. 密码重置

### 阶段四：排名系统
1. 排名数据模型
2. 排名计算逻辑
3. 排名展示页面（含徽章）

### 阶段五：多人协作
1. WebSocket 配置
2. 房间系统
3. 实时游戏同步

### 阶段六：系统监控
1. 系统统计 API
2. 在线会话管理
3. 资源监控页面

---

## 9. 第三方登录配置

### 9.1 微信登录
- 需要微信公众号
- 配置 AppID 和 AppSecret

### 9.2 Github 登录
- 在 GitHub OAuth Apps 注册
- 配置 Client ID 和 Client Secret
