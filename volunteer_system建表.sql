-- ===========================================
-- 志愿者服务系统数据库设计
-- 数据库：volunteer_system
-- MySQL 5.7+
-- ===========================================

DROP DATABASE IF EXISTS volunteer_system;
CREATE DATABASE volunteer_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE volunteer_system;

-- 1. 管理员信息表
CREATE TABLE admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '管理员账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    name VARCHAR(50) COMMENT '管理员姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(30) DEFAULT 'ADMIN' COMMENT '角色标识：SUPER_ADMIN / ADMIN',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='管理员信息表';

-- 2. 志愿者信息表
CREATE TABLE volunteer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '志愿者ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    gender CHAR(2) COMMENT '性别',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    id_card VARCHAR(30) COMMENT '身份证号',
    total_hours DECIMAL(10,2) DEFAULT 0 COMMENT '累计志愿时长（小时）',
    points INT DEFAULT 0 COMMENT '积分余额',
    certification_status TINYINT DEFAULT 0 COMMENT '认证状态：0未认证 1审核中 2已通过 3已拒绝',
    wallet_address VARCHAR(100) COMMENT '区块链钱包地址（MetaMask）',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿者信息表';

-- 3. 活动分类表
CREATE TABLE activity_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动分类表';

-- 4. 志愿活动表
CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    content TEXT COMMENT '活动内容',
    address VARCHAR(255) COMMENT '活动地址',
    start_time DATETIME COMMENT '活动开始时间',
    end_time DATETIME COMMENT '活动结束时间',
    target_number INT DEFAULT 0 COMMENT '目标报名人数',
    current_number INT DEFAULT 0 COMMENT '当前报名人数',
    service_hours DECIMAL(10,2) DEFAULT 0 COMMENT '完成活动可获得的志愿时长（小时）',
    reward_points INT DEFAULT 0 COMMENT '完成活动可获得积分',
    status VARCHAR(20) DEFAULT '未开始' COMMENT '活动状态',
    cover_image VARCHAR(255) COMMENT '封面图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES activity_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿活动表';

-- 5. 活动报名申请表
CREATE TABLE activity_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名申请ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    status VARCHAR(20) DEFAULT '待审核' COMMENT '报名状态',
    reject_reason VARCHAR(255) COMMENT '拒绝原因',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动报名申请表';

-- 6. 志愿活动打卡记录表
CREATE TABLE activity_checkin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打卡记录ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    checkin_time DATETIME NOT NULL COMMENT '签到时间',
    checkout_time DATETIME COMMENT '签退时间',
    service_hours DECIMAL(10,2) DEFAULT 0 COMMENT '本次活动实际服务时长（小时）',
    earned_points INT DEFAULT 0 COMMENT '本次活动实际获得积分',
    status TINYINT DEFAULT 0 COMMENT '状态：0未签到 1已签到 2已签退 3无效',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效记录',
    is_counted TINYINT DEFAULT 0 COMMENT '是否已计入总时长',
    remark VARCHAR(255) COMMENT '备注（异常说明等）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿活动打卡记录表';

-- 7. 志愿时长补录表
CREATE TABLE service_hour_replenish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '补录ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    hours DECIMAL(10,2) NOT NULL COMMENT '补录时长（小时）',
    earned_points INT DEFAULT 0 COMMENT '补录积分',
    reason VARCHAR(255) COMMENT '补录原因',
    status VARCHAR(20) DEFAULT '待审核' COMMENT '审核状态',
    admin_id BIGINT COMMENT '审核管理员ID',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    audit_time DATETIME COMMENT '审核时间',
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id),
    FOREIGN KEY (admin_id) REFERENCES admin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿时长补录表';

-- 8. 信息动态表
CREATE TABLE news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    views INT DEFAULT 0 COMMENT '浏览量',
    admin_id BIGINT COMMENT '发布管理员ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    FOREIGN KEY (admin_id) REFERENCES admin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='信息动态表';

-- 9. 社区帖子分类表
CREATE TABLE forum_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='论坛分类表';

-- 10. 帖子表
CREATE TABLE forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    volunteer_id BIGINT NOT NULL COMMENT '发帖人ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT COMMENT '帖子内容',
    views INT DEFAULT 0 COMMENT '浏览量',
    status VARCHAR(20) DEFAULT '待审核' COMMENT '审核状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (category_id) REFERENCES forum_category(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='帖子表';

-- 11. 评论表
CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    volunteer_id BIGINT NOT NULL COMMENT '评论者ID',
    target_type VARCHAR(20) NOT NULL COMMENT '评论目标类型：活动/帖子',
    target_id BIGINT NOT NULL COMMENT '评论目标ID',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='评论表';

-- 12. 积分商品表
CREATE TABLE goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(255) COMMENT '商品描述',
    points_required INT NOT NULL COMMENT '兑换所需积分',
    stock INT DEFAULT 0 COMMENT '库存数量',
    image LONGTEXT COMMENT '商品图片(Base64编码或URL)',
    status INT DEFAULT 1 COMMENT '商品状态：1上架 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='积分商品表';

-- 13. 兑换订单表
CREATE TABLE exchange_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    quantity INT DEFAULT 1 COMMENT '兑换数量',
    total_points INT NOT NULL COMMENT '总积分消耗',
    address VARCHAR(255) COMMENT '收货地址',
    status VARCHAR(20) DEFAULT '待发货' COMMENT '订单状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (goods_id) REFERENCES goods(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='兑换订单表';

-- 14. 收藏表
CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uq_favorite (volunteer_id, activity_id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id),
    FOREIGN KEY (activity_id) REFERENCES activity(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动收藏表';

-- 15. 公告信息表
CREATE TABLE announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    admin_id BIGINT COMMENT '发布管理员ID',
    FOREIGN KEY (admin_id) REFERENCES admin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告信息表';

-- 16. 轮播图表
CREATE TABLE carousel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    activity_id BIGINT COMMENT '关联活动ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='轮播图表';

-- 17. 志愿服务证书表
CREATE TABLE service_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    total_hours DECIMAL(10,2) NOT NULL COMMENT '累计服务时长（小时）',
    issue_date DATE NOT NULL COMMENT '发证日期',
    certificate_hash VARCHAR(255) COMMENT '证书区块链哈希值',
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿服务证书表';

-- 18. 志愿活动参与人员表
CREATE TABLE activity_participation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    volunteer_id BIGINT NOT NULL COMMENT '志愿者ID',
    checkin_id BIGINT COMMENT '关联打卡记录ID',
    actual_hours DECIMAL(10,2) DEFAULT 0 COMMENT '最终确认服务时长',
    earned_points INT DEFAULT 0 COMMENT '最终积分',
    confirm_status TINYINT DEFAULT 2 COMMENT '确认状态：0未确认 1待审核 2已通过 3已拒绝',
    confirm_admin BIGINT COMMENT '审核管理员ID',
    confirm_time DATETIME COMMENT '审核时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_activity_volunteer (activity_id, volunteer_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id),
    FOREIGN KEY (checkin_id) REFERENCES activity_checkin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='志愿活动参与人员表（链下统计存储）';
