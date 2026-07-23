@echo off
chcp 65001 >nul
title 🚀 一键修复 pnpm 安装问题（含 EISDIR 错误） by ChatGPT

echo ==========================================================
echo 💡 一键清理旧依赖 + 关闭 symlink + 使用阿里镜像重新安装
echo ==========================================================

:: 进入脚本所在目录（防止路径错误）
cd /d %~dp0

:: 1️⃣ 删除 node_modules
if exist node_modules (
    echo 🧹 正在删除 node_modules 文件夹...
    rd /s /q node_modules
) else (
    echo ✅ 未发现 node_modules，跳过删除。
)

:: 2️⃣ 删除 pnpm-lock.yaml
if exist pnpm-lock.yaml (
    echo 🧾 正在删除 pnpm-lock.yaml...
    del /f pnpm-lock.yaml
) else (
    echo ✅ 未发现 pnpm-lock.yaml，跳过删除。
)

:: 3️⃣ 清理 pnpm 缓存
echo 🧼 正在清理 pnpm 缓存...
pnpm store prune

:: 4️⃣ 设置国内镜像源（npmmirror）
echo 🌐 正在配置阿里镜像源...
pnpm config set registry https://registry.npmmirror.com

:: 5️⃣ 设置 pnpm 使用传统依赖模式（防止 symlink 报错）
echo 🧩 正在设置 node-linker=hoisted（关闭符号链接模式）...
pnpm config set node-linker hoisted

:: 6️⃣ 强制重新安装依赖
echo 📦 正在通过阿里镜像重新安装依赖（请稍候）...
pnpm install --registry=https://registry.npmmirror.com --force

echo ==========================================================
echo 🎉 依赖安装已完成！(已禁用 symlink，兼容 Windows)
echo ==========================================================
pause
