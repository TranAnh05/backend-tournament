package com.example.tournament.payload.request.referee;

import lombok.Data;

@Data
public class RefereeMatchRequest {
    // UPCOMING hoac PAST
    private String timeframe = "UPCOMING";
}
