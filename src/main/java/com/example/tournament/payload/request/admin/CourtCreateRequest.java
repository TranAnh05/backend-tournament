package com.example.tournament.payload.request.admin;

import com.example.tournament.enums.CommonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CourtCreateRequest {
    @NotBlank(message = "Tên sân con không được để trống")
    private String courtName;

    private CommonStatus status = CommonStatus.ACTIVE;

    @NotEmpty(message = "Phải chọn ít nhất một môn thể thao hỗ trợ cho sân")
    private List<Long> supportedSportIds;
}
