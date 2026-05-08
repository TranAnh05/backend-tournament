package com.example.tournament.payload.response.club;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DisciplineResponse {
    private Long id;
    private String disciplineType;
    private String reason;
    private BigDecimal fineAmount;
    private Integer suspensionDuration;
    private String status;
    private String createdAt;
    private String athleteName;
    private String tournamentName;
}