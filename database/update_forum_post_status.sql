-- 更新论坛帖子状态字段值
-- 将 published 改为 approved（已通过）
-- 保持 pending（待审核）不变
-- 保持 rejected（已驳回）不变

-- 更新所有 published 状态为 approved
UPDATE forum_post 
SET status = 'approved' 
WHERE status = 'published';

-- 验证更新结果
SELECT status, COUNT(*) as count 
FROM forum_post 
GROUP BY status;
