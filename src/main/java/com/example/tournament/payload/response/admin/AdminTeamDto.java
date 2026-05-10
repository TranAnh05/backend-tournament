package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminTeamDto {
    private Long id;
    private String name;
    private String logoUrl;
    private Integer score;
}
