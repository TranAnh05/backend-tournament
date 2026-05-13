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

        // 1. BẢO TOÀN THỨ TỰ HẠT GIỐNG TỪ FRONTEND TRUYỀN XUỐNG
        List<Club> clubsFromDb = clubRepository.findAllById(qualifiedClubIds);

        Map<Long, Club> clubMap = clubsFromDb.stream()
                .collect(Collectors.toMap(Club::getId, club -> club));

        // Tạo mảng đã được sắp xếp chuẩn xác
        List<Club> sortedQualifiedClubs = qualifiedClubIds.stream()
                .map(clubMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        int n = sortedQualifiedClubs.size();
        if (n < 2) {
            throw new RuntimeException("Cần ít nhất 2 đội để tạo vòng Knockout!");
        }

        // 2. Tính toán số lượng vé "Đặc cách" (Bye)
        int nextPowerOf2 = (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
        int numByes = nextPowerOf2 - n;

        // 3. Tạo GroupStage cho Vòng Knockout
        GroupStage knockoutStage = GroupStage.builder()
                .tournament(tournament)
                .name("Vòng Loại Trực Tiếp - " + nextPowerOf2 + " Đội")
                .stageType(StageType.KNOCKOUT)
                .status(StageStatus.ONGOING)
                .build();
        knockoutStage = groupStageRepository.save(knockoutStage);

        List<Match> firstRoundMatches = new ArrayList<>();
        int teamIndex = 0;

        // 4. Xử lý các đội được ĐẶC CÁCH (Bye)
        for (int i = 0; i < numByes; i++) {
            Club byeClub = sortedQualifiedClubs.get(teamIndex++);

            Match byeMatch = Match.builder()
                    .tournament(tournament)
                    .groupStage(knockoutStage)
                    .homeClub(byeClub)
                    .awayClub(null) // Đặc cách nên đối thủ là null
                    .status(MatchStatus.FINISHED) // Chuyển thẳng thành đã hoàn thành
                    .scheduledTime(LocalDateTime.now().plusDays(1))
                    .build();

            firstRoundMatches.add(byeMatch);
        }

        // 5. Xử lý các đội còn lại (Bắt chéo 2 đầu)
        int left = teamIndex;
        int right = sortedQualifiedClubs.size() - 1;

        while (left < right) {
            Club homeClub = sortedQualifiedClubs.get(left);
            Club awayClub = sortedQualifiedClubs.get(right);

            Match regularMatch = Match.builder()
                    .tournament(tournament)
                    .groupStage(knockoutStage)
                    .homeClub(homeClub)
                    .awayClub(awayClub)
                    .status(MatchStatus.SCHEDULED)
                    .scheduledTime(LocalDateTime.now().plusDays(1))
                    .build();

            firstRoundMatches.add(regularMatch);
            left++;
            right--;
        }

        // 6. Lưu toàn bộ trận đấu vào Database
        matchRepository.saveAll(firstRoundMatches);
    }
}
