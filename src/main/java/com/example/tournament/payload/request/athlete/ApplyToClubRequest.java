package com.example.tournament.payload.request.athlete;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyToClubRequest {

    @NotNull(message = "Club ID không được để trống")
    private Long clubId;
}