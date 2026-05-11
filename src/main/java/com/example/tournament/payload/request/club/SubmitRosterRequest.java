package com.example.tournament.payload.request.club;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitRosterRequest {

    private List<RosterItem> rosters;

    @Getter
    @Setter
    public static class RosterItem {
        private Long athleteId;
        private Integer jerseyNumber;
        private String position;
        private String role; // PLAYER | CAPTAIN
    }
}