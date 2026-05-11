package com.example.tournament.payload.response.athlete;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClubPublicResponse {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private String headquarters;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String managerName;
    private int totalMembers;        // Tổng số thành viên APPROVED
    private String homeVenueName;
}