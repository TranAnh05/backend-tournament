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
        private Integer currentScore;

        // Chia sẵn Đá chính và Dự bị
        private List<PlayerDto> startingPlayers;
        private List<PlayerDto> substitutePlayers;

        private List<PlayerDto> sentOffPlayers;
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
