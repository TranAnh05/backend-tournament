package com.example.tournament.payload.response.club;

import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.HealthStatus;
import com.example.tournament.enums.JoinStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ClubMemberResponse {
    private Long memberId;
    private Long athleteId;
    private Long userId;
    private String fullName;
    private String email;
    private String identityNumber;
    private LocalDate dateOfBirth;
    private Integer preferredNumber;
    private String preferredPosition;
    private HealthStatus healthStatus;
    private ClubRole clubRole;
    private JoinStatus joinStatus;
    private LocalDateTime joinedDate;
    private LocalDateTime leftDate;
}