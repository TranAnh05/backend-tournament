package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminActivityLogResponse {
    private String type;
    private String message;
    private LocalDateTime createdAt;
}
