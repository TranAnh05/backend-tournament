package com.example.tournament.controller.admin;

import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.admin.*;
import com.example.tournament.service.AdminDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService dashboardService;

    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<AdminActivityLogResponse>>> getRecentActivities() {

        List<AdminActivityLogResponse> activities = dashboardService.getRecentActivities();

        ApiResponse<List<AdminActivityLogResponse>> response = ApiResponse.<List<AdminActivityLogResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy nhật ký hoạt động thành công")
                .result(activities)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/live-matches")
    public ResponseEntity<ApiResponse<List<AdminLiveMatchResponse>>> getLiveMatches() {

        List<AdminLiveMatchResponse> matches = dashboardService.getLiveMatches();

        ApiResponse<List<AdminLiveMatchResponse>> response = ApiResponse.<List<AdminLiveMatchResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách trận đấu Live thành công")
                .result(matches)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<AdminDashboardChartsResponse>> getDashboardCharts(
            @Valid AdminDashboardFilterRequest request) {

        AdminDashboardChartsResponse chartsData = dashboardService.getDashboardCharts(request);

        ApiResponse<AdminDashboardChartsResponse> response = ApiResponse.<AdminDashboardChartsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy dữ liệu biểu đồ thành công")
                .result(chartsData)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<AdminDashboardKpiResponse>> getDashboardKpis(
            @Valid AdminDashboardFilterRequest request) {

        AdminDashboardKpiResponse kpiData = dashboardService.getDashboardKpis(request);

        ApiResponse<AdminDashboardKpiResponse> response = ApiResponse.<AdminDashboardKpiResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chỉ số KPI thành công")
                .result(kpiData)
                .build();

        return ResponseEntity.ok(response);
    }
}
