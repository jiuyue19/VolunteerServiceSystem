package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long volunteerId;
    private String targetType;
    private Long targetId;
    private String content;
    private LocalDateTime createdAt;
    private String userName;
    private String avatar;
    private String postTitle;
    private Long parentId;
    private Integer likes;  // 点赞数
    private Boolean liked;  // 当前用户是否已点赞
    private String likedBy; // 点赞用户ID列表，用逗号分隔
}