package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminKpiMetricDto {
    private Long value;
    private Double trend; // Tỷ lệ tăng trưởng %
}
