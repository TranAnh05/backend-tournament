package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardKpiResponse {
    private AdminKpiMetricDto totalTournaments;
    private AdminKpiMetricDto totalUsers;
    private AdminKpiMetricDto totalClubs;
    private AdminKpiMetricDto totalMatchesPlayed;
}
