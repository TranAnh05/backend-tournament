package com.example.tournament.payload.response.Tournament;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Data
@Builder
public class TournamentResponse {
    private Long id;
    private String name;
    private String sportName;
    private String venueName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String format;
    private String status;

}
