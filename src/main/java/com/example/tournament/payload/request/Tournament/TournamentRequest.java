package com.example.tournament.payload.request.Tournament;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TournamentRequest {
    @NotBlank(message = "Tên giải đấu không được để trống")
    private String name;

    @NotNull(message = "Môn thi đấu không được để trống")
    private Long sportId;

    @NotNull(message = "Địa điểm không được để trống")
    private Long venueId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minAthletes;
    private Integer maxAthletes;
    private Float winPoints = 3.0f;  // Giá trị mặc định
    private Float drawPoints = 1.0f;
    private Float lostPoints = 0.0f;
    private String format;
}