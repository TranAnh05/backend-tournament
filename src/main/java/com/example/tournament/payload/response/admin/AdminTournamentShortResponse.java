package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AdminTournamentShortResponse {
    private Long id;
    private String name;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
