-- 为comment表添加喜欢数字段
ALTER TABLE comment 
ADD COLUMN likes INT DEFAULT 0 COMMENT '喜欢数',
ADD COLUMN liked_by TEXT COMMENT '点赞用户ID列表，用逗号分隔';

-- 为likes字段添加索引以优化排序查询
CREATE INDEX idx_comment_likes ON comment(likes);
