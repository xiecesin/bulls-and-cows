#!/bin/bash

# Bulls and Cows 停止脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PIDS_FILE="$SCRIPT_DIR/.pids"

if [ -f "$PIDS_FILE" ]; then
    echo "正在停止服务..."
    PIDS=$(cat "$PIDS_FILE")
    kill $PIDS 2>/dev/null
    rm -f "$PIDS_FILE"
    echo "服务已停止"
else
    echo "未找到进程ID文件，尝试查找并停止..."
    
    # 尝试查找并停止相关进程
    pkill -f "spring-boot:run" 2>/dev/null && echo "后端服务已停止"
    pkill -f "vite" 2>/dev/null && echo "前端服务已停止"
fi

echo "完成"
