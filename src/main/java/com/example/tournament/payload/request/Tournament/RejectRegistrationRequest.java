package com.example.tournament.payload.request.Tournament;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectRegistrationRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
