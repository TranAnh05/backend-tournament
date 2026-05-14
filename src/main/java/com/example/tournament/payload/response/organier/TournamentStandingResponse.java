package com.example.tournament.payload.response.organier;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TournamentStandingResponse {
    private Long tournamentId;
    private String tournamentName;
    private List<GroupStandingDto> groups;

    @Data
    @Builder
    public static class GroupStandingDto {
        private Long groupId;
        private String groupName;
        private List<ClubStandingDto> standings;
    }

    @Data
    @Builder
    public static class ClubStandingDto {
        private Integer rank; // Thứ hạng
        private Long clubId;
        private String clubName;
        private String shortName;
        private String logoUrl;

        // Các chỉ số chuyên môn
        private Integer matchesPlayed;
        private Integer won;
        private Integer drawn;
        private Integer lost;
        private Integer scoresFor;       // Bàn thắng ghi được
        private Integer scoresAgainst;   // Bàn thua
        private Integer scoreDifference; // Hiệu số (+/-)
        private Integer totalPoints;     // Tổng điểm
    }
}