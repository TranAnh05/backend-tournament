package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminOrganizerDetailResponse {
    // Thông tin cơ bản
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String status;
    private LocalDateTime createdAt;

    // Hồ sơ mở rộng
    private LocalDateTime assignedAt;
    private String bio;
    private List<AdminAchievementDto> achievements;

    // Thống kê hoạt động
    private Long totalTournaments; // Tổng số giải đã tạo
    private Long totalParticipatingClubs; // Tổng số CLB từng tham gia

    // Danh sách 5 giải gần nhất
    private List<AdminTournamentShortResponse> recentTournaments;
}
