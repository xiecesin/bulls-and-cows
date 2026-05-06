# Bulls and Cows 部署文档

## 服务器环境要求

### 最低配置
- CPU: 2 核
- 内存: 2 GB
- 磁盘: 10 GB

### 推荐配置
- CPU: 4 核
- 内存: 4 GB
- 磁盘: 20 GB

### 软件依赖
- JDK 21 (生产环境建议使用 JDK 21 LTS)
- Node.js 18+ (构建前端时需要)
- Nginx (用于反向代理和静态资源服务)

## 生产环境部署

### 1. 后端部署

#### 构建 JAR 包

```bash
cd backend
./mvnw clean package -DskipTests
```

#### 运行服务

```bash
# 基本运行
java -jar target/bulls-and-cows-1.0.0.jar

# 带环境变量运行
java -jar target/bulls-and-cows-1.0.0.jar \
  --server.port=8080 \
  --spring.datasource.url=jdbc:h2:file:./data/bullscows

# 使用 Systemd 管理服务
```

创建服务文件 `/etc/systemd/system/bullscows.service`:

```ini
[Unit]
Description=Bulls and Cows Backend Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/bullscows/backend
ExecStart=/usr/bin/java -jar bulls-and-cows-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl enable bullscows
sudo systemctl start bullscows
sudo systemctl status bullscows
```

### 2. 前端部署

#### 构建生产版本

```bash
cd frontend
npm install
npm run build
```

构建产物在 `dist` 目录。

#### 使用 Nginx 部署

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /opt/bullscows/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

#### Docker 部署（可选）

创建 `Dockerfile`:

```dockerfile
# 后端
FROM eclipse-temurin:21-jdk-alpine AS backend
WORKDIR /app
COPY backend/target/bulls-and-cows-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile
# 前端
FROM node:18-alpine AS builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

使用 Docker Compose：

```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

## HTTPS 配置

### 使用 Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

### Nginx HTTPS 配置

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    location / {
        root /opt/bullscows/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 性能优化

### JVM 调优

```bash
java -jar -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     bulls-and-cows-1.0.0.jar
```

### 数据库优化

- 生产环境建议使用 PostgreSQL 或 MySQL 替代 H2
- 配置连接池大小
- 启用查询缓存

### 前端优化

- 启用 Gzip 压缩
- 配置静态资源缓存
- 使用 CDN 加速

## 监控与日志

### 日志配置

在 `application.properties` 中：

```properties
logging.file.name=logs/bullscows.log
logging.file.max-size=100MB
logging.file.max-history=30
```

### 健康检查

```bash
curl http://localhost:8080/api/health
```

## 备份策略

### 数据库备份

```bash
# H2 数据库备份
cp data/bullscows.mv.db backups/bullscows-$(date +%Y%m%d).mv.db

# PostgreSQL 备份
pg_dump -U bullscows bullscows > backup-$(date +%Y%m%d).sql
```

### 定时备份

使用 crontab：

```bash
0 2 * * * /opt/bullscows/scripts/backup.sh
```
