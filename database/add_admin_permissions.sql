-- 添加管理员权限字段
-- 执行日期: 2025-01-10

USE volunteer_system;

-- 为admin表添加permissions字段，存储JSON格式的权限列表
ALTER TABLE admin ADD COLUMN permissions TEXT COMMENT '权限列表(JSON格式)';

-- 为超级管理员设置全部权限
UPDATE admin SET permissions = '["admins","volunteers","activities","categories","forum","post-categories","banners","comments","announcements","applications","checkins","replenish","goods","orders","blockchain","blockchain-query","certificate-library"]' WHERE role = 'SUPER_ADMIN';

-- 为普通管理员设置默认权限（除管理员管理外的所有权限）
UPDATE admin SET permissions = '["volunteers","activities","categories","forum","post-categories","banners","comments","announcements","applications","checkins","replenish","goods","orders","blockchain","blockchain-query","certificate-library"]' WHERE role = 'ADMIN';
