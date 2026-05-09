package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class RosterResponse {
    private Long tournamentId;
    private String tournamentName;
    private List<RosterPlayerResponse> players;

    @Getter
    @Setter
    @Builder
    public static class RosterPlayerResponse {
        private Long rosterId;
        private Long athleteId;
        private String fullName;
        private Integer jerseyNumber;
        private String position;
        private String role;
        private String status;
        private String healthStatus;
    }
}