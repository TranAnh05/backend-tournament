package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MatchEventResponse {
    private Long id;
    private String eventType;
    private String eventTime;
    private String primaryAthleteName;
    private Long clubId;
}

