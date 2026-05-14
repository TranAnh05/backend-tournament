package com.example.tournament.service;

import com.example.tournament.entity.GroupStage;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Tournament;
import com.example.tournament.payload.response.organier.OverallStandingResponse;
import com.example.tournament.payload.response.organier.TournamentLookupResponse;
import com.example.tournament.payload.response.organier.TournamentStandingResponse;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandingService {

    private final StandingRepository standingRepository;
    private final TournamentRepository tournamentRepository;

    @Transactional(readOnly = true)
    public TournamentStandingResponse getTournamentStandings(Long tournamentId) {
        // 1. Kiểm tra giải đấu tồn tại
        var tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

        // 2. Lấy danh sách Standing thô từ Database (Đã tối ưu JOIN FETCH)
        List<Standing> rawStandings = standingRepository.findStandingsWithDetailsByTournamentId(tournamentId);

        // 3. Nhóm dữ liệu theo GroupStage (Bảng đấu)
        Map<GroupStage, List<Standing>> groupedData = rawStandings.stream()
                .collect(Collectors.groupingBy(Standing::getGroupStage));

        // 4. Nhào nặn dữ liệu sang DTO và thực hiện sắp xếp
        List<TournamentStandingResponse.GroupStandingDto> groups = new ArrayList<>();

        groupedData.forEach((group, standings) -> {
            // Định nghĩa thuật toán sắp xếp Waterfall
            Comparator<Standing> standingComparator = Comparator
                    .comparing(Standing::getTotalPoints, Comparator.reverseOrder())     // 1. Điểm cao nhất
                    .thenComparing(Standing::getScoreDifference, Comparator.reverseOrder()) // 2. Hiệu số cao nhất
                    .thenComparing(Standing::getScoresFor, Comparator.reverseOrder())       // 3. Ghi nhiều bàn nhất
                    .thenComparing(s -> s.getClub().getName()); // 4. Tên theo A-Z nếu bằng hết

            // Thực hiện sort
            standings.sort(standingComparator);

            // Chuyển sang DTO và tính Rank
            List<TournamentStandingResponse.ClubStandingDto> clubDtos = new ArrayList<>();
            for (int i = 0; i < standings.size(); i++) {
                Standing s = standings.get(i);
                clubDtos.add(mapToClubStandingDto(s, i + 1)); // i + 1 chính là Rank
            }

            groups.add(TournamentStandingResponse.GroupStandingDto.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .standings(clubDtos)
                    .build());
        });

        // Sắp xếp lại danh sách các Bảng đấu theo ID hoặc Tên (A, B, C...)
        groups.sort(Comparator.comparing(TournamentStandingResponse.GroupStandingDto::getGroupName));

        return TournamentStandingResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .groups(groups)
                .build();
    }

    private TournamentStandingResponse.ClubStandingDto mapToClubStandingDto(Standing s, int rank) {
        return TournamentStandingResponse.ClubStandingDto.builder()
                .rank(rank)
                .clubId(s.getClub().getId())
                .clubName(s.getClub().getName())
                .shortName(s.getClub().getShortName())
                .logoUrl(s.getClub().getLogoUrl())
                .matchesPlayed(s.getMatchesPlayed())
                .won(s.getMatchesWon())
                .drawn(s.getMatchesDrawn())
                .lost(s.getMatchesLost())
                .scoresFor(s.getScoresFor())
                .scoresAgainst(s.getScoresAgainst())
                .scoreDifference(s.getScoreDifference())
                .totalPoints(s.getTotalPoints())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TournamentLookupResponse> getTournamentsForStandingsLookup(Long organizerId) {
        List<Tournament> tournaments = tournamentRepository.findTournamentsForStandingsLookup(organizerId);

        return tournaments.stream()
                .map(t -> TournamentLookupResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .status(t.getStatus().toString())
                        .sportName(t.getSport().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OverallStandingResponse getOverallStandings(Long tournamentId) {
        // 1. Kiểm tra giải đấu
        var tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Giải đấu không tồn tại!"));

        // 2. Lấy dữ liệu gộp từ Repository (Object[] từ câu Query SUM/GROUP BY)
        List<Object[]> aggregatedData = standingRepository.aggregateOverallStandingsByTournamentId(tournamentId);

        // 3. Map từ Object[] sang List DTO trung gian và thực hiện Logic Sắp xếp
        AtomicInteger rankGenerator = new AtomicInteger(1);

        List<OverallStandingResponse.ClubOverallStandingDto> sortedRankings = aggregatedData.stream()
                .map(this::mapToDto)
                .sorted(getOverallComparator()) // Thuật toán sắp xếp "Thác nước"
                .peek(dto -> dto.setOverallRank(rankGenerator.getAndIncrement())) // Gán thứ hạng sau khi sort
                .collect(Collectors.toList());

        return OverallStandingResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .rankings(sortedRankings)
                .build();
    }

    /**
     * THUẬT TOÁN SẮP XẾP CHUNG CUỘC (Waterfall Logic)
     * Ưu tiên 1: Vòng đấu xa nhất (Dựa vào tên vòng hoặc số lượng vòng tham gia)
     * Ưu tiên 2: Tổng điểm tích lũy
     * Ưu tiên 3: Tổng hiệu số
     * Ưu tiên 4: Tổng bàn thắng
     */
    private Comparator<OverallStandingResponse.ClubOverallStandingDto> getOverallComparator() {
        return (c1, c2) -> {
            // 1. So sánh cấp độ vòng đấu (Dựa trên trọng số tên vòng hoặc logic nghiệp vụ)
            // Lưu ý: Đội vào Chung kết luôn đứng trên đội bị loại ở Bán kết
            int stageCompare = compareStageLevel(c1.getHighestStageName(), c2.getHighestStageName());
            if (stageCompare != 0) return stageCompare;

            // 2. So sánh tổng điểm
            int pointsCompare = c2.getTotalPoints().compareTo(c1.getTotalPoints());
            if (pointsCompare != 0) return pointsCompare;

            // 3. So sánh hiệu số
            int diffCompare = c2.getTotalDifference().compareTo(c1.getTotalDifference());
            if (diffCompare != 0) return diffCompare;

            // 4. So sánh tổng bàn thắng
            return c2.getTotalGoalsScored().compareTo(c1.getTotalGoalsScored());
        };
    }

    /**
     * Logic phân cấp Vòng đấu (Nghiệp vụ quan trọng)
     */
    private int compareStageLevel(String stage1, String stage2) {
        return getStageWeight(stage2) - getStageWeight(stage1);
    }

    private int getStageWeight(String stageName) {
        String name = stageName.toLowerCase();
        if (name.contains("chung kết")) return 100;
        if (name.contains("bán kết")) return 80;
        if (name.contains("tứ kết")) return 60;
        if (name.contains("vòng 16")) return 40;
        return 20; // Vòng bảng
    }

    private OverallStandingResponse.ClubOverallStandingDto mapToDto(Object[] row) {
        return OverallStandingResponse.ClubOverallStandingDto.builder()
                .clubName(row[1] != null ? row[1].toString() : "N/A")
                .logoUrl(row[2] != null ? row[2].toString() : null)
                .highestStageName(row[3].toString())
                .totalMatches(((Number) row[4]).intValue())
                .totalWon(((Number) row[5]).intValue())
                .totalDrawn(((Number) row[6]).intValue())
                .totalLost(((Number) row[7]).intValue())
                .totalGoalsScored(((Number) row[8]).intValue())
                .totalGoalsAgainst(((Number) row[9]).intValue())
                .totalDifference(((Number) row[10]).intValue())
                .totalPoints(((Number) row[11]).intValue()) // Index cuối cùng là 11
                .build();
    }
}
