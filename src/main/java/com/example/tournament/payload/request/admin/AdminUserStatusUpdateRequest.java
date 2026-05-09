package com.example.tournament.payload.request.admin;

import com.example.tournament.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusUpdateRequest {
    @NotNull(message = "Trạng thái mới không được để trống")
    private UserStatus status;

    private String reason;
}
