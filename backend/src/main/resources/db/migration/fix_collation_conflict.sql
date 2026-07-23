-- 修复字符集排序规则冲突
-- 统一使用 utf8mb4_general_ci 排序规则

-- 修改 comment 表的 liked_by 字段
ALTER TABLE comment MODIFY COLUMN liked_by TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 修改 comment 表的 target_type 字段
ALTER TABLE comment MODIFY COLUMN target_type VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;

-- 修改 comment 表的 content 字段
ALTER TABLE comment MODIFY COLUMN content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;

-- 检查其他可能有冲突的字段
-- 如果需要，可以统一修改整个表的字符集
-- ALTER TABLE comment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 修改 volunteer 表的相关字段（如果需要）
ALTER TABLE volunteer MODIFY COLUMN username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;

-- 修改 activity 表的 title 字段（如果需要）
ALTER TABLE activity MODIFY COLUMN title VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;

-- 修改 forum_post 表的 title 字段（如果需要）
ALTER TABLE forum_post MODIFY COLUMN title VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;
