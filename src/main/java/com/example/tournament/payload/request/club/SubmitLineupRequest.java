package com.example.tournament.payload.request.club;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitLineupRequest {
    private List<LineupItem> lineups;

    @Getter
    @Setter
    public static class LineupItem {
        private Long athleteId;
        private String lineupType;
        private Integer jerseyNumber;
        private String position;
    }
}