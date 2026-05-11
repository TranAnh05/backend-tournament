package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MatchResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private String groupStageName;
    private Long homeClubId;
    private String homeClubName;
    private String homeClubShortName;
    private Long awayClubId;
    private String awayClubName;
    private String awayClubShortName;
    private String scheduledTime;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private List<MatchEventResponse> events;
    private Boolean hasLineup;
}
