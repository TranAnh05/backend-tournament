package com.example.tournament.payload.response.organier;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OverallStandingResponse {
    private Long tournamentId;
    private String tournamentName;
    private List<ClubOverallStandingDto> rankings;

    @Data
    @Builder
    public static class ClubOverallStandingDto {
        private Integer overallRank;     // Thứ hạng tổng thể toàn giải
        private String clubName;
        private String logoUrl;
        private String highestStageName; // Vòng đấu cao nhất đạt tới (VD: Chung kết, Bán kết, Bảng A)

        // Thống kê cộng dồn từ tất cả các giai đoạn
        private Integer totalMatches;
        private Integer totalWon;
        private Integer totalDrawn;
        private Integer totalLost;
        private Integer totalGoalsScored;   // Tổng bàn thắng (scores_for)
        private Integer totalGoalsAgainst;  // Tổng bàn thua (scores_against)
        private Integer totalDifference;    // Tổng hiệu số
        private Integer totalPoints;        // Tổng điểm tích lũy
    }
}
