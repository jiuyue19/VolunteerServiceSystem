package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Permission {
    private Long id;
    private String permissionKey;
    private String permissionName;
    private String permissionType;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
}
