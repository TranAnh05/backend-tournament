package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TournamentResponse {
    private Long id;
    private String name;
    private Long sportId;
    private String sportName;
    private Long venueId;
    private String venueName;
    private String startDate;
    private String endDate;
    private Float winPoints;
    private Float drawPoints;
    private Float lossPoints;
    private Integer minAthletes;
    private Integer maxAthletes;
    private String format;
    private String status;
}