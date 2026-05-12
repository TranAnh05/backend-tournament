package com.example.tournament.payload.response.Tournament;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
public class RegistrationDetailResponse {
    private Long id;
    private String status;
    private String rejectReason;
    private String financialProofUrl;
    private String homeKitColor;
    private String awayKitColor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appliedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    // --- Thông tin chi tiết CLB (Trích xuất từ Entity Club) ---
    private Long clubId;
    private String clubName;
    private String clubLogo;
    private String repName;     // Tên người đại diện
    private String phone;       // Số điện thoại liên hệ
    private String email;       // Email liên hệ
    private Integer membersCount; // Số lượng thành viên hiện tại

    private List<OrganizerRosterResponse> roster;
}
