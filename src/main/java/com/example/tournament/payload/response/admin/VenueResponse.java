package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private String status;
    private List<CourtResponse> courts;
}
