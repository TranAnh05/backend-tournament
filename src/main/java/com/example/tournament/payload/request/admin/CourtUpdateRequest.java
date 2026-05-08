package com.example.tournament.payload.request.admin;

import com.example.tournament.enums.CommonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CourtUpdateRequest {
    private Long id;

    @NotBlank
    private String courtName;
    private CommonStatus status;
    @NotEmpty
    private List<Long> supportedSportIds;
}