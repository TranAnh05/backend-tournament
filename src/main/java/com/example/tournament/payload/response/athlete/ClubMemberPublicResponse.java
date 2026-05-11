package com.example.tournament.payload.response.athlete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClubMemberPublicResponse {
    private Long athleteId;
    private String fullName;
    private Integer preferredNumber;
    private String preferredPosition;
    private String healthStatus;     // FIT | INJURED
    private String clubRole;         // MEMBER | CAPTAIN | HEAD_COACH
}