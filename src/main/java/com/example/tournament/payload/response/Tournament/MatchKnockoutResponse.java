package com.example.tournament.payload.response.Tournament;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchKnockoutResponse {
    private Long id;
    private Integer bracketPosition;
    private Long nextMatchId;
    private String status;
    private TeamDto homeClub;
    private TeamDto awayClub;
    private TeamDto winner;

    @Data
    @Builder
    public static class TeamDto {
        private Long id;
        private String name;
        private String shortName;
        private String logoUrl;
        private Integer score;
    }

}