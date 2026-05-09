package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TournamentHistoryResponse {
    private Long tournamentId;
    private String tournamentName;
    private String season;       // VD: "Mùa Hè 2026"
    private String registrationStatus; // PENDING, APPROVED, WITHDRAWN...
    private Integer matchesPlayed;
    private Integer matchesWon;
    private Integer matchesDrawn;
    private Integer matchesLost;
    private Integer totalPoints;
    private Integer ranking;     // xếp hạng trong bảng
}