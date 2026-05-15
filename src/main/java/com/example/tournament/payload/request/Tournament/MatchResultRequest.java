package com.example.tournament.payload.request.Tournament;

import lombok.Data;

@Data
public class MatchResultRequest {
    private Integer homeScore;
    private Integer awayScore;
}
