package com.example.tournament.payload.request.club;

import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.HealthStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAthleteRequest {

    @Min(value = 1, message = "Số áo tối thiểu là 1")
    @Max(value = 99, message = "Số áo tối đa là 99")
    private Integer preferredNumber;

    @Size(max = 100)
    private String preferredPosition;

    private HealthStatus healthStatus;

    private ClubRole clubRole;
}