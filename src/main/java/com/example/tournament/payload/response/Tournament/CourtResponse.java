package com.example.tournament.payload.response.Tournament;

import lombok.Builder;
import lombok.Data;

// DTO cho Sân thi đấu (nằm trong Venue)
@Data
@Builder
public class CourtResponse {
    private Long id;
    private String name;
    private String courtName;
}