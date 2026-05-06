# Bulls and Cows API 文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **数据格式**: JSON
- **认证方式**: Bearer Token (JWT)

## 认证

### 注册用户

```
POST /api/auth/register
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 (3-20字符) |
| password | string | 是 | 密码 (6-20字符) |
| email | string | 是 | 邮箱 |

**请求示例**

```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

**响应示例**

```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

---

### 用户登录

```
POST /api/auth/login
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例**

```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应示例**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

---

### 刷新 Token

```
POST /api/auth/refresh
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 刷新令牌 |

**请求示例**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**响应示例**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 900
  }
}
```

---

### 退出登录

```
POST /api/auth/logout
Authorization: Bearer {token}
```

**响应示例**

```json
{
  "success": true,
  "message": "退出登录成功"
}
```

---

## 用户

### 获取当前用户信息

```
GET /api/users/me
Authorization: Bearer {token}
```

**响应示例**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": "",
    "role": "USER",
    "createdAt": "2026-05-06T10:00:00",
    "lastLoginAt": "2026-05-06T12:00:00"
  }
}
```

---

### 更新用户信息

```
PUT /api/users/me
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| email | string | 否 | 邮箱 |
| avatarUrl | string | 否 | 头像 URL |

**请求示例**

```json
{
  "nickname": "新昵称",
  "email": "new@example.com"
}
```

---

### 修改密码

```
PUT /api/users/me/password
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码 |

**请求示例**

```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass123"
}
```

**响应示例**

```json
{
  "success": true,
  "message": "密码修改成功"
}
```

---

## 游戏

### 开始新游戏

```
POST /api/game/start
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| allowDuplicates | boolean | 否 | 是否允许重复数字 (默认 false) |

**请求示例**

```json
{
  "allowDuplicates": false
}
```

**响应示例**

```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "allowDuplicates": false,
  "message": "新游戏已开始！请输入4位数字进行猜测。",
  "hint": "提示：4位数字各不相同，如 1234, 5678 等"
}
```

---

### 提交猜测

```
POST /api/game/guess
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gameId | string | 是 | 游戏 ID |
| guess | string | 是 | 猜测数字 (4位) |

**请求示例**

```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "guess": "1234"
}
```

**响应示例**

```json
{
  "success": false,
  "guess": "1234",
  "bulls": 1,
  "cows": 2,
  "result": "1A2B",
  "guessCount": 1,
  "message": "当前结果: 1A2B，继续加油！",
  "reasoning": [
    {
      "step": 1,
      "title": "逐位比较，统计公牛位置",
      "description": "比较 5678 和 1234 的每一位"
    },
    {
      "step": 2,
      "title": "统计非公牛位置的数字",
      "description": "秘密数字的非公牛位置: 567"
    },
    {
      "step": 3,
      "title": "统计奶牛（数字对但位置错）",
      "description": "检查猜测中的非公牛数字是否能在秘密数字的非公牛位置中找到"
    },
    {
      "step": 4,
      "title": "最终结果",
      "description": "公牛(Bulls): 1个\n奶牛(Cows): 2个\n最终答案: 1A2B"
    }
  ]
}
```

**胜利时响应**

```json
{
  "success": true,
  "guess": "5678",
  "bulls": 4,
  "cows": 0,
  "result": "4A0B",
  "guessCount": 6,
  "message": "恭喜！你用了 6 次猜出了正确答案！",
  "reasoning": [...]
}
```

---

### 查看答案

```
GET /api/game/{gameId}/answer
```

**响应示例**

```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "answer": "5678"
}
```

---

### 直接计算结果

```
POST /api/calculate
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| secret | string | 是 | 秘密数字 (4位) |
| guess | string | 是 | 猜测数字 (4位) |

**请求示例**

```json
{
  "secret": "1234",
  "guess": "5678"
}
```

**响应示例**

```json
{
  "secret": "1234",
  "guess": "5678",
  "bulls": 0,
  "cows": 0,
  "result": "0A0B",
  "reasoning": [...]
}
```

---

### 生成谜题

```
POST /api/puzzle/generate
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| allowDuplicates | boolean | 否 | 是否允许重复数字 |

**响应示例**

```json
{
  "puzzleId": "uuid",
  "history": [
    {"guess": "1234", "bulls": 1, "cows": 2, "hint": "1A2B"},
    {"guess": "5678", "bulls": 0, "cows": 3, "hint": "0A3B"}
  ],
  "allowDuplicates": false,
  "hint": "根据以下记录，推断出秘密数字"
}
```

---

### 验证谜题答案

```
POST /api/puzzle/verify
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| puzzleId | string | 是 | 谜题 ID |
| answer | string | 是 | 用户答案 |

**响应示例**

```json
{
  "correct": true,
  "yourAnswer": "4321",
  "secret": "4321",
  "message": "回答正确！"
}
```

---

## 练习记录

### 保存练习记录

```
POST /api/record/save
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| secretNumber | string | 是 | 目标数字 |
| guessCount | integer | 是 | 猜测次数 |
| timeSpentMs | integer | 否 | 用时(毫秒) |
| allowDuplicates | boolean | 是 | 是否允许重复 |
| gameResult | string | 是 | WIN 或 QUIT |

**请求示例**

```json
{
  "secretNumber": "5678",
  "guessCount": 6,
  "timeSpentMs": 45000,
  "allowDuplicates": false,
  "gameResult": "WIN"
}
```

**响应示例**

```json
{
  "success": true,
  "recordId": 1,
  "message": "记录保存成功"
}
```

---

### 获取练习记录列表

```
GET /api/record/list?page=0&size=10
Authorization: Bearer {token}
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码 (默认 0) |
| size | integer | 否 | 每页大小 (默认 10) |

**响应示例**

```json
{
  "records": [
    {
      "id": 1,
      "secretNumber": "5678",
      "guessCount": 6,
      "timeSpentMs": 45000,
      "allowDuplicates": false,
      "gameResult": "WIN",
      "createdAt": "2026-05-06T12:00:00"
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "currentPage": 0
}
```

---

### 获取用户统计数据

```
GET /api/record/stats
Authorization: Bearer {token}
```

**响应示例**

```json
{
  "totalGames": 50,
  "winGames": 42,
  "quitGames": 8,
  "winRate": 84.0,
  "avgGuessCount": 5.32,
  "bestGuessCount": 3,
  "todayCount": 3
}
```

---

## 排行榜

### 获取排行榜

```
GET /api/ranking?type=GUESS_COUNT&limit=20
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 否 | 排名类型：GUESS_COUNT, TIME (默认 GUESS_COUNT) |
| limit | integer | 否 | 返回数量 (默认 20) |

**响应示例**

```json
{
  "rankings": [
    {
      "rank": 1,
      "userId": 1,
      "username": "player1",
      "score": 4,
      "achievedAt": "2026-05-06T10:00:00"
    },
    {
      "rank": 2,
      "userId": 2,
      "username": "player2",
      "score": 5,
      "achievedAt": "2026-05-06T11:00:00"
    }
  ],
  "type": "GUESS_COUNT"
}
```

---

### 获取我的排名

```
GET /api/ranking/me?type=GUESS_COUNT
Authorization: Bearer {token}
```

**响应示例**

```json
{
  "ranked": true,
  "rank": 3,
  "score": 6,
  "totalPlayers": 50,
  "achievedAt": "2026-05-06T12:00:00"
}
```

**未上榜时响应**

```json
{
  "ranked": false,
  "message": "你还没有上榜，继续加油！"
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 (如用户名已存在) |
| 500 | 服务器内部错误 |

**错误响应格式**

```json
{
  "error": true,
  "message": "错误描述",
  "code": "ERROR_CODE"
}
```
