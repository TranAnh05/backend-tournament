package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long clubId;
    private String status;
    private String homeKitColor;
    private String awayKitColor;
    private String appliedAt;
    private String reviewedAt;
}