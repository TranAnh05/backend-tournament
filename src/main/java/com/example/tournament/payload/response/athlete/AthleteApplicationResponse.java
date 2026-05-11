package com.example.tournament.payload.response.athlete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AthleteApplicationResponse {
    private Long memberId;
    private Long clubId;
    private String clubName;
    private String clubShortName;
    private String joinStatus;          // PENDING | APPROVED | REJECTED | LEFT | REMOVED
    private String clubRole;            // MEMBER | CAPTAIN | HEAD_COACH
    private LocalDateTime appliedAt;    // created_at
    private LocalDateTime joinedDate;
    private LocalDateTime leftDate;
}