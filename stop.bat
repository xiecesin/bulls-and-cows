@echo off
chcp 65001 > nul
color 0C

echo 正在停止 Bulls and Cows 服务...

taskkill /FI "WINDOWTITLE eq Backend*" /F > nul 2>&1
taskkill /FI "WINDOWTITLE eq Frontend*" /F > nul 2>&1

echo 服务已停止
pause
