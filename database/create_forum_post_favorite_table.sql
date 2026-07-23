-- 创建帖子收藏表
CREATE TABLE IF NOT EXISTS `forum_post_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `volunteer_id` bigint NOT NULL COMMENT '志愿者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_volunteer` (`post_id`, `volunteer_id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_post_id` (`post_id`),
  CONSTRAINT `fk_favorite_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `volunteer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子收藏表';
