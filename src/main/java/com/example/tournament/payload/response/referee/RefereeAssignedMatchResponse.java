package com.example.tournament.payload.response.referee;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefereeAssignedMatchResponse {
    private Long matchId;
    private String tournamentName;
    private LocalDateTime scheduledTime;

    private String location;

    private String matchStatus; // SCHEDULED, FINISHED

    private String refereeRole;

    private MatchClubDto homeTeam;
    private MatchClubDto awayTeam;

    @Data
    @Builder
    public static class MatchClubDto {
        private Long id;
        private String name;
        private String shortName;
        private String logoUrl;
        private Integer score;
    }
}
