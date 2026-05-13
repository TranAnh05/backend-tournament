package com.example.tournament.payload.response.Tournament;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupStageResponse {
    private Long id;
    private String name; // Bảng A, Bảng B...
    private List<ClubStandingResponse> teams;
}


