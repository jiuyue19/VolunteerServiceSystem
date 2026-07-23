package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminPermission {
    private Long id;
    private Long adminId;
    private Long permissionId;
    private LocalDateTime createTime;
}
