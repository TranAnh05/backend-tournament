package com.example.tournament.service;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.GroupStage;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.enums.StageStatus;
import com.example.tournament.enums.StageType;
import com.example.tournament.payload.response.Tournament.MatchKnockoutResponse;
import com.example.tournament.repository.ClubRepository;
import com.example.tournament.repository.GroupStageRepository;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnockoutService {

    // Inject các Repository cần thiết (Tournament, Match, GroupStage...)
    // ...

    private final TournamentRepository tournamentRepository;
    private final GroupStageRepository groupStageRepository;
    private final MatchRepository  matchRepository;
    private final ClubRepository clubRepository;


    @Transactional
    public void generateFirstKnockoutRound(Long tournamentId, List<Long> qualifiedClubIds) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

        // 1. Chuẩn bị danh sách đội và tính toán quy mô (Vòng 16, 8, hay 4...)
        List<Club> sortedQualifiedClubs = getSortedClubs(qualifiedClubIds);
        int n = sortedQualifiedClubs.size();
        int nextPowerOf2 = (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
        int totalRounds = (int) (Math.log(nextPowerOf2) / Math.log(2));

        // 2. Tạo Stage Knockout
        GroupStage knockoutStage = createKnockoutStage(tournament, nextPowerOf2);

        // 3. ✨ TRÁI TIM CỦA ARCHITECT: Tạo khung sơ đồ (Skeleton)
        // Chúng ta tạo từ trận Chung kết ngược về Vòng 1 để dễ dàng link next_match
        List<Match> allMatches = new ArrayList<>();
        List<Match> currentRoundMatches = new ArrayList<>();
        List<Match> nextRoundMatches = new ArrayList<>();

// --- Bước A: Tạo các trận đấu "khung" cho toàn giải ---
        for (int r = totalRounds; r >= 1; r--) {
            // Sửa công thức tại đây để số trận tăng dần khi lùi về Vòng 1
            int matchesInRound = (int) Math.pow(2, totalRounds - r);
            currentRoundMatches = new ArrayList<>();

            for (int i = 1; i <= matchesInRound; i++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .groupStage(knockoutStage)
                        .bracketPosition(i)
                        .status(MatchStatus.SCHEDULED)
                        // Tính toán thời gian: Vòng 1 đá trước, Chung kết đá sau
                        .scheduledTime(LocalDateTime.now().plusDays(r))
                        .build();

                if (!nextRoundMatches.isEmpty()) {
                    // Nối 2 trận vòng này vào 1 trận vòng sau
                    Match parentMatch = nextRoundMatches.get((i - 1) / 2);
                    match.setNextMatch(parentMatch);
                }
                currentRoundMatches.add(match);
            }
            allMatches.addAll(currentRoundMatches);
            nextRoundMatches = currentRoundMatches;
        }
        // Lưu toàn bộ khung trận đấu để có ID chính thức
        matchRepository.saveAll(allMatches);

        // --- Bước B: Điền đội vào Vòng 1 và xử lý ĐẶC CÁCH (Byes) ---
        // Lấy danh sách các trận của Vòng 1 (nằm ở cuối list allMatches)
        List<Match> firstRoundMatches = allMatches.stream()
                .filter(m -> m.getNextMatch() != null || totalRounds == 1) // Logic lọc vòng 1
                .limit(nextPowerOf2 / 2)
                .collect(Collectors.toList());

        fillTeamsIntoFirstRound(firstRoundMatches, sortedQualifiedClubs);
    }

    private List<Club> getSortedClubs(List<Long> qualifiedClubIds) {
        List<Club> clubsFromDb = clubRepository.findAllById(qualifiedClubIds);

        // Tạo Map để tìm kiếm nhanh theo ID
        Map<Long, Club> clubMap = clubsFromDb.stream()
                .collect(Collectors.toMap(Club::getId, club -> club));

        // Sắp xếp lại dựa trên danh sách ID gốc để đảm bảo thứ tự Seed 1, Seed 2...
        return qualifiedClubIds.stream()
                .map(clubMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
    private GroupStage createKnockoutStage(Tournament tournament, int nextPowerOf2) {
        GroupStage knockoutStage = GroupStage.builder()
                .tournament(tournament)
                .name("Vòng Loại Trực Tiếp - " + nextPowerOf2 + " Đội")
                .stageType(StageType.KNOCKOUT)
                .status(StageStatus.ONGOING)
                .build();

        return groupStageRepository.save(knockoutStage);
    }
    private void fillTeamsIntoFirstRound(List<Match> matches, List<Club> clubs) {
        int teamIdx = 0;
        int numByes = (int) Math.pow(2, Math.ceil(Math.log(clubs.size()) / Math.log(2))) - clubs.size();

        for (Match m : matches) {
            m.setHomeClub(clubs.get(teamIdx++));

            if (numByes > 0) {
                // Xử lý Đặc cách (Bye)
                m.setAwayClub(null);
                m.setStatus(MatchStatus.FINALIZED);
                m.setWinner(m.getHomeClub());
                m.setHomeScore(0); m.setAwayScore(0);
                numByes--;

                // ✨ Gọi thăng hạng ngay cho đội đặc cách
                promoteWinnerToNextRound(m);
            } else {
                m.setAwayClub(clubs.get(teamIdx++));
            }
        }
        matchRepository.saveAll(matches);
    }

    private void validateGroupStageFinalized(Long tournamentId) {
        boolean hasUnfinalizedMatches = matchRepository.hasUnfinalizedMatches(
                tournamentId,
                StageType.GROUP,
                MatchStatus.FINALIZED
        );

        if (hasUnfinalizedMatches) {
            throw new RuntimeException("Chưa thể bốc thăm Knockout! Toàn bộ các trận Vòng bảng phải được chốt kết quả (Trạng thái FINALIZED).");
        }
    }

    @Transactional // Đảm bảo tính nhất quán dữ liệu khi thăng hạng
    public void promoteWinnerToNextRound(Match currentMatch) {
        if (currentMatch.getWinner() == null) return;

        Match nextMatch = currentMatch.getNextMatch();

        if (nextMatch != null) {
            // Quy tắc: Vị trí lẻ vào Home, Vị trí chẵn vào Away của trận sau
            if (currentMatch.getBracketPosition() % 2 != 0) {
                nextMatch.setHomeClub(currentMatch.getWinner());
            } else {
                nextMatch.setAwayClub(currentMatch.getWinner());
            }
            matchRepository.save(nextMatch);
        }
    }

    public List<MatchKnockoutResponse> getKnockoutBracket(Long tournamentId) {
        List<Match> matches = matchRepository.findKnockoutMatchesByTournamentId(tournamentId);

        return matches.stream().map(this::convertToKnockoutResponse).collect(Collectors.toList());
    }

    private MatchKnockoutResponse convertToKnockoutResponse(Match m) {
        return MatchKnockoutResponse.builder()
                .id(m.getId())
                .bracketPosition(m.getBracketPosition())
                .nextMatchId(m.getNextMatch() != null ? m.getNextMatch().getId() : null)
                .status(m.getStatus().name())
                .homeClub(mapTeam(m.getHomeClub(), m.getHomeScore()))
                .awayClub(mapTeam(m.getAwayClub(), m.getAwayScore()))
                .winner(mapTeam(m.getWinner(), null))
                .build();
    }

    private MatchKnockoutResponse.TeamDto mapTeam(Club club, Integer score) {
        if (club == null) return null;
        return MatchKnockoutResponse.TeamDto.builder()
                .id(club.getId())
                .name(club.getName())
                .shortName(club.getShortName())
                .logoUrl(club.getLogoUrl())
                .score(score)
                .build();
    }
}
