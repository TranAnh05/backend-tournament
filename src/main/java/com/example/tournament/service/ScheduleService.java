package com.example.tournament.service;


import com.example.tournament.entity.*;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.enums.StageType;
import com.example.tournament.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final TournamentRepository tournamentRepository;
    private final GroupStageRepository groupStageRepository;
    private final StandingRepository standingRepository;
    private final MatchRepository matchRepository;
    private final CourtRepository courtRepository;

    @Transactional
    public void generateGroupStageSchedule(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

        List<GroupStage> groups = groupStageRepository.findByTournamentIdAndStageType(tournamentId, StageType.GROUP);

        if (groups.isEmpty()) {
            throw new RuntimeException("Giải đấu chưa được bốc thăm vòng bảng!");
        }
        List<Court> validCourts = courtRepository.findValidCourtsForTournament(
                tournament.getSport().getId(),
                tournament.getVenue().getId()
        );

        List<Match> newMatches = new ArrayList<>();

        // Cố định ngày khai mạc vòng bảng (Ví dụ: 7 ngày kể từ lúc bốc thăm)
        LocalDateTime groupStageStartDate = LocalDateTime.now().plusDays(7);
        int courtIndex = 0;
        for (GroupStage group : groups) {
            if (matchRepository.existsByGroupStageId(group.getId())) {
                continue;
            }

            List<Club> clubsInGroup = standingRepository.findByGroupStageId(group.getId())
                    .stream()
                    .map(Standing::getClub)
                    .collect(Collectors.toList());

            // THUẬT TOÁN CIRCLE METHOD
            List<Club> teams = new ArrayList<>(clubsInGroup);

            // Xử lý trường hợp bảng có số đội LẺ (VD: 3 đội) -> Thêm 1 đội "ảo" (Dummy).
            // Ai bắt cặp với đội ảo này nghĩa là được nghỉ ở vòng đó.
            if (teams.size() % 2 != 0) {
                teams.add(null);
            }

            int numTeams = teams.size();
            int numRounds = numTeams - 1; // Tổng số vòng đấu
            int matchesPerRound = numTeams / 2; // Số trận mỗi vòng

            // Chạy từng Vòng đấu (Matchday)
            for (int round = 0; round < numRounds; round++) {

                // ✨ PHÂN BỔ THỜI GIAN: 1 ngày đá, 1 ngày nghỉ
                // Vòng 0: + 0 ngày (Ngày khai mạc)
                // Vòng 1: + 2 ngày (Đã qua 1 ngày đá vòng 0 + 1 ngày nghỉ)
                // Vòng 2: + 4 ngày...
                LocalDateTime matchDate = groupStageStartDate.plusDays(round * 2);

                // Bắt cặp cho vòng đấu hiện tại
                for (int match = 0; match < matchesPerRound; match++) {
                    Club home = teams.get(match);
                    Club away = teams.get(numTeams - 1 - match);

                    // Chỉ tạo trận đấu nếu cả 2 đội đều là thật (Không phải đội Dummy)
                    if (home != null && away != null) {
                        Court assignedCourt = null;
                        if (!validCourts.isEmpty()) {
                            assignedCourt = validCourts.get(courtIndex % validCourts.size());
                            courtIndex++; // Tăng chỉ số để trận sau lấy sân tiếp theo
                        }
                        Match newMatch = Match.builder()
                                .tournament(tournament)
                                .groupStage(group)
                                .homeClub(home)
                                .awayClub(away)
                                .status(MatchStatus.SCHEDULED)
                                .scheduledTime(matchDate) // Gắn chính xác ngày đã phân bổ
                                .court(assignedCourt)
                                .build();

                        newMatches.add(newMatch);
                    }
                }

                // ✨ TRÁI TIM CỦA THUẬT TOÁN: Xoay mảng chuẩn bị cho vòng sau
                // Giữ nguyên phần tử 0 (teams.get(0)).
                // Lấy phần tử cuối cùng nhét lên vị trí số 1. Các phần tử khác tự động lùi về sau.
                teams.add(1, teams.remove(teams.size() - 1));
            }
        }

        if (newMatches.isEmpty()) {
            throw new RuntimeException("Tất cả các bảng đấu đã có lịch thi đấu!");
        }

        matchRepository.saveAll(newMatches);
    }


}
