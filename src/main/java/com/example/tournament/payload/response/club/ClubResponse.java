package com.example.tournament.payload.response.club;

import com.example.tournament.enums.CommonStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ClubResponse {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private String headquarters;
    private Long homeVenueId;
    private String homeVenueName;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String managerName;

    private List<ClubMemberResponse> members;
    private List<TournamentHistoryResponse> tournamentHistory;
}