package com.example.tournament.payload.response.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private String email;
    private String fullName;
    private List<String> roles;
}
