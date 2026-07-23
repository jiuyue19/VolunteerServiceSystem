-- Add shared post fields to forum_post table
-- Run this SQL script to add support for post sharing/forwarding

ALTER TABLE forum_post
ADD COLUMN shared_from_post_id BIGINT NULL COMMENT '转发来源帖子ID',
ADD COLUMN shared_from_volunteer_id BIGINT NULL COMMENT '原帖作者ID',
ADD COLUMN shared_at DATETIME NULL COMMENT '转发时间';

-- Add index for better query performance
CREATE INDEX idx_shared_from_post ON forum_post(shared_from_post_id);
CREATE INDEX idx_shared_from_volunteer ON forum_post(shared_from_volunteer_id);

-- Add comment to table
ALTER TABLE forum_post COMMENT = '论坛帖子表（支持转发功能）';
