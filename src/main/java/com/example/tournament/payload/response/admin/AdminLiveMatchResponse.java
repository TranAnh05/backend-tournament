package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminLiveMatchResponse {
    private Long matchId;
    private String tournamentName;
    private String sportName;
    private LocalDateTime startTime;
    private String venueName;
    private String status;
    private Integer liveMinute; // Tính toán động ra số phút đang đá

    private AdminTeamDto homeTeam;
    private AdminTeamDto awayTeam;
}
