package com.example.tournament.service;

import com.example.tournament.entity.GroupStage;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Tournament;
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
}
