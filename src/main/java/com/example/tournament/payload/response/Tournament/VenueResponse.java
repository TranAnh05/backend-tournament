package com.example.tournament.payload.response.Tournament;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private List<CourtResponse> courts; // Hiển thị danh sách sân của địa điểm đó
}
