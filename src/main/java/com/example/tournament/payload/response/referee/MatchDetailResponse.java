package com.example.tournament.payload.response.referee;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MatchDetailResponse {

    // Thông tin chung trận đấu
    private Long matchId;
    private String tournamentName;
    private String sportName;
    private LocalDateTime scheduledTime;
    private String location;
    private String status;

    // Cấu hình luật thi đấu
    private Map<String, String> sportRules;

    // THÊM MỚI: Danh sách chi tiết điểm số của từng Hiệp/Set (Dùng vẽ bảng tỷ số nhỏ)
    private List<PeriodScoreDto> periodScores;

    // Danh sách đội hình 2 bên
    private TeamLineupDto homeTeam;
    private TeamLineupDto awayTeam;

    private List<MatchEventDto> timeline;

    @Data
    @Builder
    public static class TeamLineupDto {
        private Long clubId;
        private String clubName;
        private String logoUrl;

        // CẬP NHẬT Ý NGHĨA:
        // - Với Bóng đá/Futsal: Đây là TỔNG BÀN THẮNG.
        // - Với Cầu lông/Bóng bàn: Đây là TỶ SỐ SET (Số Set đã thắng, VD: 1 hoặc 2)
        private Integer matchScore;

        // THÊM MỚI: Điểm của hiệp/set ĐANG DIỄN RA
        // Khi tạo Set mới (nhận event START_PERIOD), biến này tự động bắt đầu từ 0
        private Integer currentPeriodScore;

        // Chia sẵn Đá chính và Dự bị
        private List<PlayerDto> startingPlayers;
        private List<PlayerDto> substitutePlayers;
        private List<PlayerDto> sentOffPlayers;
    }

    // THÊM MỚI OBJECT: Lưu lịch sử điểm số
    @Data
    @Builder
    public static class PeriodScoreDto {
        private String periodName;    // Sử dụng String để linh hoạt (VD: "Set 1", "Hiệp 1")
        private Integer homeScore;    // Điểm đội nhà trong hiệp/set này (VD: 21)
        private Integer awayScore;    // Điểm đội khách trong hiệp/set này (VD: 15)
        private Boolean isFinished;   // Trạng thái (True khi có sự kiện END_PERIOD)
    }

    @Data
    @Builder
    public static class PlayerDto {
        private Long lineupId;
        private Long athleteId;
        private String fullName;
        private String identityNumber;// Đối chiếu CCCD ngoài sân
        private String portraitUrl;   // Đối chiếu khuôn mặt
        private Integer jerseyNumber; // Số áo
        private String position;      // Vị trí
        private Boolean isConfirmed;  // Trạng thái đã duyệt chưa
    }

    @Data
    @Builder
    public static class MatchEventDto {
        private Long id;
        private String eventType;
        private String eventTime;
        private String description;
        private LocalDateTime createdAt;

        private String primaryAthleteName;
        private Integer primaryAthleteNumber;
        private String secondaryAthleteName;
        private Integer secondaryAthleteNumber;
        private Long clubId;
    }
}
