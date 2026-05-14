package com.example.tournament.payload.response.Tournament;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefereeSummaryResponse {
    private Long id;
    private String fullName;
}
