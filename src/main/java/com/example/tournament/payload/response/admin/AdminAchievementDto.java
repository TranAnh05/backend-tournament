package com.example.tournament.payload.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAchievementDto {
    private Integer year;
    private String title;
    private String organization;
}
