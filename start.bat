@echo off
chcp 65001 > nul
color 0B

echo ========================================
echo     Bulls and Cows 猜数字游戏 启动脚本
echo ========================================

set SCRIPT_DIR=%~dp0
set BACKEND_DIR=%SCRIPT_DIR%backend
set FRONTEND_DIR=%SCRIPT_DIR%frontend

cd /d "%BACKEND_DIR%"
echo [1/2] 启动后端服务...
start "Backend" cmd /c ".\mvnw.cmd spring-boot:run"
echo 后端服务启动中，请等待...

timeout /t 10 /nobreak > nul

cd /d "%FRONTEND_DIR%"
echo [2/2] 启动前端服务...
if not exist "node_modules" (
    echo 正在安装前端依赖...
    call npm install
)
start "Frontend" cmd /c "npm run dev"

echo.
echo ========================================
echo         所有服务启动成功！
echo ========================================
echo.
echo 访问地址:
echo   - 前端: http://localhost:3000
echo   - 后端: http://localhost:8080
echo   - H2控制台: http://localhost:8080/h2-console
echo.
echo 按任意键打开浏览器...
pause > nul
start http://localhost:3000

echo.
echo 已打开浏览器，请在浏览器中访问 http://localhost:3000
echo 按 Ctrl+C 停止所有服务，或关闭对应的命令窗口
pause
