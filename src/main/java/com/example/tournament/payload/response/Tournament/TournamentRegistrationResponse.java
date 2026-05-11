package com.example.tournament.payload.response.Tournament;

import com.example.tournament.enums.RegistrationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRegistrationResponse {

    private Long id; // ID của đơn đăng ký

    // --- Thông tin CLB (Được trích xuất từ Entity Club) ---
    private Long clubId;
    private String clubName;
    private String clubLogo; // Rất cần thiết để Frontend hiển thị avatar của đội

    // --- Thông tin Đơn đăng ký ---
    private RegistrationStatus status;
    private String rejectReason;
    private String financialProofUrl;

    private String homeKitColor;
    private String awayKitColor;

    // Định dạng lại thời gian cho đẹp khi trả về JSON
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appliedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}