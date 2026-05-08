package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourtResponse {
    private Long id;
    private String courtName;
    private String status;
    private List<SportBasicResponse> supportedSports;
}
