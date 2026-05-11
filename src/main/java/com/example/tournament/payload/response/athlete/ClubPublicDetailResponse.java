package com.example.tournament.payload.response.athlete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ClubPublicDetailResponse {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private String headquarters;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String managerName;
    private String homeVenueName;
    private int totalMembers;
    private List<ClubMemberPublicResponse> members;  // Danh sách thành viên công khai
}