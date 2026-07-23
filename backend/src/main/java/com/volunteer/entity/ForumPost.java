package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForumPost {
    private Long id;
    private Long categoryId;
    private Long volunteerId;
    private String title;
    private String content;
    private Integer views;
    private String status;
    private LocalDateTime createdAt;
    private String author;  // 真实姓名（保留用于兼容）
    private String username;  // 用户名
    private String avatar;  // 头像
    private String category;
    private String imageUrl;
    private String reviewReason;
    private Long sharedFromPostId;  // 转发来源帖子ID
    private Long sharedFromVolunteerId;  // 原帖作者ID
    private String sharedFromAuthor;  // 原帖作者名称
    private LocalDateTime sharedAt;  // 转发时间
}