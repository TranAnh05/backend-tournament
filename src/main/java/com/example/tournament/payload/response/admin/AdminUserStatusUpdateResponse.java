package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserStatusUpdateResponse {
    private Long userId;
    private String fullName;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedByAdmin;
    private LocalDateTime changedAt;
}
