# Bulls and Cows 快速启动指南

## 环境要求

### 后端
- JDK 21+
- Maven 3.8+

### 前端
- Node.js 18+
- npm 9+

## 快速启动

### 方式一：使用启动脚本（推荐）

```bash
# macOS / Linux
./start.sh

# Windows
start.bat
```

### 方式二：手动启动

**1. 启动后端**

```bash
cd backend

# 如果是首次运行，安装依赖
./mvnw dependency:resolve

# 启动后端服务
./mvnw spring-boot:run

# 或打包后运行
./mvnw package
java -jar target/bulls-and-cows-1.0.0.jar
```

**2. 启动前端（新开终端窗口）**

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3000 | Web 应用入口 |
| 后端 API | http://localhost:8080 | REST API 服务 |
| H2 控制台 | http://localhost:8080/h2-console | 数据库管理 |
| API 文档 | http://localhost:8080/swagger-ui.html | 接口文档（需启用） |

## 登录信息

### 默认用户
- 用户名: `testuser`
- 密码: `password123`

### 管理员
- 用户名: `admin`
- 密码: `admin123`

## 验证服务状态

```bash
curl http://localhost:8080/api/health
```

正常响应：
```json
{
  "status": "UP",
  "service": "Bulls and Cows API",
  "version": "1.0.0"
}
```

## 停止服务

```bash
# 使用脚本停止
./stop.sh    # macOS/Linux
stop.bat     # Windows

# 或手动停止
# 找到进程并 kill
lsof -i :8080  # 后端端口
lsof -i :3000  # 前端端口
kill <PID>
```

## 常见问题

### 1. 端口被占用

```bash
# Linux/Mac 查看端口占用
lsof -i :8080

# Windows 查看端口占用
netstat -ano | findstr :8080
```

修改端口：在 `application.properties` 中修改 `server.port`

### 2. 前端无法连接后端

检查后端 CORS 配置和前端 API 地址是否匹配。

### 3. 数据库初始化失败

H2 数据库会在首次运行时自动创建，重启项目时可能需要清理 `data.mv.db` 文件。
