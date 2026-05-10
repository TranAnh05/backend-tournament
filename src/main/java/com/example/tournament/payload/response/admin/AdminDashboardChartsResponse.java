package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardChartsResponse {
    private List<AdminChartDataProjection> tournamentsBySport;
    private List<AdminChartDataProjection> venueUsage;
    private List<AdminActivityTrendProjection> activityTrends;
}
