package com.example.tournament.payload.request.referee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MatchEventRequest {

    @NotBlank(message = "Loại sự kiện không được để trống")
    private String eventType; // Nhận vào Enum dưới dạng String: START_PERIOD, GOAL, YELLOW_CARD, SUBSTITUTION...

    @NotBlank(message = "Thời gian diễn ra sự kiện không được để trống")
    private String eventTime; // Thời gian thực tế trên sân (VD: "15:30", "45+2", "Set 1")

    private Long clubId;

    private Long primaryAthleteId;

    private Long secondaryAthleteId;

    private String description;
}
