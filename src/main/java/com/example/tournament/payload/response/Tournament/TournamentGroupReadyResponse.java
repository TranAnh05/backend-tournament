package com.example.tournament.payload.response.Tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TournamentGroupReadyResponse {
    private Long id;
    private String name;
    private String sport;
    // Danh sách các đội đã được duyệt để chia bảng
    private List<ClubSimpleResponse> approvedClubs;
}
