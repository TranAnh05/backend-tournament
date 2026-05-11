package com.example.tournament.payload.response.athlete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AthleteProfileResponse {
    private Long athleteId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String identityNumber;
    private LocalDate dateOfBirth;
    private String portraitUrl;
    private Integer preferredNumber;
    private String preferredPosition;
    private String healthStatus;
    private String currentClubName;
}