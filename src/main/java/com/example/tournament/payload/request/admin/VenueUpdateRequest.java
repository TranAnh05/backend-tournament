package com.example.tournament.payload.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class VenueUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @Valid
    private List<CourtUpdateRequest> courts;
}
