package com.example.tournament.payload.request.referee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeMatchStatusRequest {

    @NotBlank(message = "Trạng thái mục tiêu không được để trống")
    private String targetStatus; // Truyền vào: IN_PROGRESS, PAUSED, FINISHED, CANCELED

    private String note; // Ghi chú thêm (VD: "Trời mưa to", "Cầu thủ chấn thương")
}
