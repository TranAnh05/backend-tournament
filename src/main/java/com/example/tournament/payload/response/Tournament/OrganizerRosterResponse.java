package com.example.tournament.payload.response.Tournament;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrganizerRosterResponse {
    private Long id; // ID của bản ghi roster

    // --- Thông tin Vận động viên (Từ Entity Athlete) ---
    private Long athleteId;
    private String athleteName;
    private String avatarUrl; // Ảnh đại diện VĐV

    // --- Thông tin thi đấu trong giải ---
    private Integer jerseyNumber; // Số áo thi đấu
    private String position;      // Vị trí (Tiền đạo, Hậu vệ...)
    private String role;          // RosterRole (PLAYER, CAPTAIN, COACH)
    private String status;        // RosterStatus (ELIGIBLE, SUSPENDED...)
}
