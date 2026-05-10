package com.example.tournament.payload.response.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AdminDashboardFilterRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Long sportId;

    // Nếu Frontend không truyền ngày, tự động lấy khoảng thời gian 30 ngày gần nhất
    public LocalDate getStartDate() {
        return startDate != null ? startDate : LocalDate.now().minusDays(30);
    }

    public LocalDate getEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
