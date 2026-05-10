package com.example.tournament.payload.request.referee;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ConfirmLineupRequest {
    @NotEmpty(message = "Danh sách VĐV xác nhận không được để trống")
    private List<Long> lineupIds;
}
