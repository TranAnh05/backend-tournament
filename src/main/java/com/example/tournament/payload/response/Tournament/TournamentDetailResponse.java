package com.example.tournament.payload.response.Tournament;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentDetailResponse {
    private Long id;
    private String name;
    private String sportName;
    private VenueResponse venue; // Bọc toàn bộ thông tin địa điểm và sân
    private LocalDate startDate;
    private LocalDate endDate;
    private Float winPoints;
    private Float drawPoints;
    private Float lostPoints;
    private String format;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer minAthletes;
    private Integer maxAthletes;


}
