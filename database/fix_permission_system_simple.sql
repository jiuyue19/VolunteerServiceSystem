-- ===========================================
-- 权限系统修复（简化版）：只创建权限定义表
-- ===========================================
USE volunteer_system;

-- 1. 检查并删除旧的permission表（如果存在）
DROP TABLE IF EXISTS permission;

-- 2. 创建权限表（仅作为权限定义参考）
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_key VARCHAR(50) NOT NULL UNIQUE COMMENT '权限标识（如：dashboard, volunteers, activities）',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称（如：数据统计、志愿者管理）',
    permission_type VARCHAR(20) DEFAULT 'MENU' COMMENT '权限类型：MENU菜单 / BUTTON按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID（0表示顶级菜单）',
    path VARCHAR(100) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    description VARCHAR(255) COMMENT '权限描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限定义表';

-- 3. 初始化基础权限数据（使用连字符命名，与前端保持一致）
INSERT INTO permission (permission_key, permission_name, permission_type, parent_id, path, icon, sort_order, description) VALUES
-- 顶级菜单
('dashboard', '数据统计', 'MENU', 0, '/admin/dashboard', 'DataAnalysis', 1, '数据统计看板'),
('user-management', '用户管理', 'MENU', 0, NULL, 'User', 2, '用户管理模块'),
('activity-management', '活动管理', 'MENU', 0, NULL, 'Calendar', 3, '活动管理模块'),
('audit-management', '志愿审核', 'MENU', 0, NULL, 'Document', 4, '志愿审核模块'),
('order-management', '订单管理', 'MENU', 0, NULL, 'ShoppingCart', 5, '订单管理模块'),
('blockchain-management', '区块链管理', 'MENU', 0, NULL, 'Link', 6, '区块链管理模块'),

-- 用户管理子菜单
('admins', '管理员管理', 'MENU', 2, '/admin/admins', NULL, 21, '管理员列表'),
('volunteers', '志愿者管理', 'MENU', 2, '/admin/volunteers', NULL, 22, '志愿者列表'),

-- 活动管理子菜单
('activities', '志愿活动', 'MENU', 3, '/admin/activities', NULL, 31, '志愿活动管理'),
('categories', '活动分类', 'MENU', 3, '/admin/categories', NULL, 32, '活动分类管理'),
('forum', '论坛帖子', 'MENU', 3, '/admin/forum', NULL, 33, '论坛帖子管理'),
('post-categories', '帖子分类', 'MENU', 3, '/admin/post-categories', NULL, 34, '帖子分类管理'),
('banners', '轮播图管理', 'MENU', 3, '/admin/banners', NULL, 35, '轮播图信息管理'),
('comments', '评论管理', 'MENU', 3, '/admin/comments', NULL, 36, '评论信息管理'),
('announcements', '系统公告', 'MENU', 3, '/admin/announcements', NULL, 37, '系统公告管理'),

-- 志愿审核子菜单
('applications', '报名申请', 'MENU', 4, '/admin/applications', NULL, 41, '报名申请审核'),
('checkins', '打卡记录', 'MENU', 4, '/admin/checkins', NULL, 42, '打卡记录管理'),
('replenish', '补录申请', 'MENU', 4, '/admin/replenish', NULL, 43, '补录申请审核'),

-- 订单管理子菜单
('goods', '积分商品', 'MENU', 5, '/admin/goods', NULL, 51, '积分商品管理'),
('orders', '兑换订单', 'MENU', 5, '/admin/orders', NULL, 52, '兑换订单管理'),

-- 区块链管理子菜单
('blockchain-query', '区块链服务查询', 'MENU', 6, '/admin/blockchain-query', NULL, 61, '区块链服务查询'),
('blockchain', '区块链证书', 'MENU', 6, '/admin/blockchain', NULL, 62, '区块链证书管理'),
('certificate-library', '证书库管理', 'MENU', 6, '/admin/certificate-library', NULL, 63, '证书库管理'),

-- 权限管理菜单（仅超级管理员可见）
('permission-management', '权限管理', 'MENU', 0, '/admin/permissions', 'Lock', 7, '管理员权限配置');

-- 4. 查看创建结果
SELECT '权限表创建成功' AS status;
SELECT COUNT(*) AS permission_count FROM permission;

-- 5. 查看admin表的permissions字段示例
SELECT '===== admin表结构 =====' AS info;
SHOW COLUMNS FROM admin LIKE 'permissions';

-- 注意事项：
-- 1. admin表的permissions字段存储JSON格式的权限数组，例如：["volunteers", "activities", "forum"]
-- 2. 超级管理员（SUPER_ADMIN）自动拥有所有权限，permissions字段可以为NULL或空
-- 3. 普通管理员的权限通过超级管理员在管理员管理页面编辑permissions字段
-- 4. permission表仅作为权限定义参考，实际权限存储在admin.permissions字段中



