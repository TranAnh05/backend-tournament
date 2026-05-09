package com.example.tournament.payload.request.club;

import lombok.Data;
import java.util.List;

@Data
public class RosterRequest {
    private List<RosterPlayerRequest> players;

    @Data
    public static class RosterPlayerRequest {
        private Long athleteId;
        private Integer jerseyNumber;
        private String position;
        private String role; // PLAYER hoặc CAPTAIN
    }
}