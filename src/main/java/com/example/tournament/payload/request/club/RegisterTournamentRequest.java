package com.example.tournament.payload.request.club;

import lombok.Data;

@Data
public class RegisterTournamentRequest {
    private String homeKitColor;
    private String awayKitColor;
    private String financialProofUrl;
}