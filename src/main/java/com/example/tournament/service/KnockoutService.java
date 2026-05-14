package com.example.tournament.service;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.GroupStage;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.enums.StageStatus;
import com.example.tournament.enums.StageType;
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

        validateGroupStageFinalized(tournamentId);

        // 1. Lấy và bảo toàn thứ tự hạt giống
        List<Club> clubsFromDb = clubRepository.findAllById(qualifiedClubIds);
        Map<Long, Club> clubMap = clubsFromDb.stream()
                .collect(Collectors.toMap(Club::getId, club -> club));

        List<Club> sortedQualifiedClubs = qualifiedClubIds.stream()
                .map(clubMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        int n = sortedQualifiedClubs.size();
        int nextPowerOf2 = (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
        int numByes = nextPowerOf2 - n;

        // 2. Tạo GroupStage
        GroupStage knockoutStage = GroupStage.builder()
                .tournament(tournament)
                .name("Vòng Loại Trực Tiếp - " + nextPowerOf2 + " Đội")
                .stageType(StageType.KNOCKOUT)
                .status(StageStatus.ONGOING)
                .build();
        knockoutStage = groupStageRepository.save(knockoutStage);

        // ✨ KHAI BÁO CÁC BIẾN ĐỂ FIX LỖI "SYMBOL"
        List<Match> firstRoundMatches = new ArrayList<>();
        int teamIndex = 0;
        int currentPosition = 1; // Biến này để đánh số trận 1, 2, 3...

        // 3. Xử lý các đội được ĐẶC CÁCH (Bye)
        for (int i = 0; i < numByes; i++) {
            Club byeClub = sortedQualifiedClubs.get(teamIndex++);

            Match byeMatch = Match.builder()
                    .tournament(tournament)
                    .groupStage(knockoutStage)
                    .homeClub(byeClub)
                    .awayClub(null)
                    .bracketPosition(currentPosition++) // ✨ Fix lỗi currentPosition
                    .status(MatchStatus.FINALIZED)      // ✨ Tự động chốt
                    .winner(byeClub)                    // ✨ Xác định người thắng ngay
                    .homeScore(0).awayScore(0)
                    .scheduledTime(LocalDateTime.now().plusDays(1))
                    .build();

            firstRoundMatches.add(byeMatch);
        }

        // 4. Xử lý các đội còn lại (Bắt chéo)
        int left = teamIndex;
        int right = sortedQualifiedClubs.size() - 1;

        while (left < right) {
            // ✨ Khai báo regularMatch bên trong vòng lặp để fix lỗi resolve
            Match regularMatch = Match.builder()
                    .tournament(tournament)
                    .groupStage(knockoutStage)
                    .homeClub(sortedQualifiedClubs.get(left++))
                    .awayClub(sortedQualifiedClubs.get(right--))
                    .bracketPosition(currentPosition++) // ✨ Đánh số vị trí tiếp theo
                    .status(MatchStatus.SCHEDULED)
                    .scheduledTime(LocalDateTime.now().plusDays(1))
                    .build();

            firstRoundMatches.add(regularMatch);
        }

        // 5. Lưu và TỰ ĐỘNG THĂNG HẠNG (Fix lỗi allRoundMatches và savedMatches)
        List<Match> savedMatches = matchRepository.saveAll(firstRoundMatches);

        // Stream qua danh sách đã lưu, lọc ra các trận FINALIZED (đặc cách) để đẩy lên vòng sau
        savedMatches.stream()
                .filter(m -> m.getStatus() == MatchStatus.FINALIZED)
                .forEach(this::promoteWinnerToNextRound);
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

    public void promoteWinnerToNextRound(Match currentMatch) {
        if (currentMatch.getWinner() == null) return;

        // Tìm trận tiếp theo (Phải đảm bảo bạn đã tạo sẵn khung trận đấu vòng sau hoặc có logic link ID)
        Match nextMatch = currentMatch.getNextMatch();

        if (nextMatch != null) {
            // Logic nhánh đấu: Trận 1, 3, 5 -> Home; Trận 2, 4, 6 -> Away
            if (currentMatch.getBracketPosition() % 2 != 0) {
                nextMatch.setHomeClub(currentMatch.getWinner());
            } else {
                nextMatch.setAwayClub(currentMatch.getWinner());
            }
            matchRepository.save(nextMatch);
        }
    }
}
