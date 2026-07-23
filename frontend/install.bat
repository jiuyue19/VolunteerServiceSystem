@echo off
chcp 65001 >nul
cd /d %~dp0
echo 正在清理旧文件...
if exist node_modules rd /s /q node_modules
if exist package-lock.json del /f package-lock.json
if exist pnpm-lock.yaml del /f pnpm-lock.yaml

echo 正在使用 npm 和淘宝镜像安装依赖...
npm install --registry=https://registry.npmmirror.com

echo 安装完成！
pause