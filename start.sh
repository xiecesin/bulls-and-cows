#!/bin/bash

# Bulls and Cows 启动脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    Bulls and Cows 猜数字游戏 启动脚本   ${NC}"
echo -e "${BLUE}========================================${NC}"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"

# 检查目录
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}错误: 找不到后端目录 $BACKEND_DIR${NC}"
    exit 1
fi

if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}错误: 找不到前端目录 $FRONTEND_DIR${NC}"
    exit 1
fi

# 启动后端
echo -e "\n${YELLOW}[1/2] 启动后端服务...${NC}"
cd "$BACKEND_DIR"

# 检查是否有 mvnw
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw spring-boot:run &
    BACKEND_PID=$!
    echo -e "${GREEN}后端服务启动中 (PID: $BACKEND_PID)${NC}"
else
    echo -e "${RED}错误: 找不到 mvnw 文件${NC}"
    exit 1
fi

# 等待后端启动
echo -e "${YELLOW}等待后端服务启动...${NC}"
sleep 8

# 检查后端是否启动成功
if ! kill -0 $BACKEND_PID 2>/dev/null; then
    echo -e "${RED}错误: 后端服务启动失败${NC}"
    exit 1
fi

# 启动前端
echo -e "\n${YELLOW}[2/2] 启动前端服务...${NC}"
cd "$FRONTEND_DIR"

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}正在安装前端依赖...${NC}"
    npm install
fi

npm run dev &
FRONTEND_PID=$!
echo -e "${GREEN}前端服务启动中 (PID: $FRONTEND_PID)${NC}"

# 等待前端启动
sleep 5

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}         所有服务启动成功！            ${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${BLUE}访问地址:${NC}"
echo -e "  - 前端: http://localhost:3000"
echo -e "  - 后端: http://localhost:8080"
echo -e "  - H2控制台: http://localhost:8080/h2-console"
echo -e "\n${YELLOW}进程ID:${NC}"
echo -e "  - 后端: $BACKEND_PID"
echo -e "  - 前端: $FRONTEND_PID"
echo -e "\n${YELLOW}停止服务: kill $BACKEND_PID $FRONTEND_PID${NC}"
echo -e "${YELLOW}或使用: ./stop.sh${NC}"

# 保存进程ID
echo "$BACKEND_PID $FRONTEND_PID" > "$SCRIPT_DIR/.pids"

echo -e "\n${GREEN}按 Ctrl+C 停止所有服务${NC}"

# 等待用户中断
wait
