package com.example.tournament.service;


import com.example.tournament.entity.*;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.enums.StageStatus;
import com.example.tournament.enums.StageType;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.payload.response.Tournament.ClubStandingResponse;
import com.example.tournament.payload.response.Tournament.GroupStageResponse;
import com.example.tournament.repository.GroupStageRepository;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TournamentRegistrationRepository;
import com.example.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupDrawService {
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final GroupStageRepository groupStageRepository;
    private final StandingRepository standingRepository;

    @Transactional
    public void executeGroupDraw(Long tournamentId, int numberOfGroups) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

        // 1. Lấy danh sách đội đã duyệt
        List<Club> clubs = registrationRepository
                .findAllByTournamentIdAndStatus(tournamentId, RegistrationStatus.APPROVED)
                .stream().map(TournamentRegistration::getClub).collect(Collectors.toList());

        if (clubs.size() < numberOfGroups) {
            throw new RuntimeException("Số lượng đội (" + clubs.size() + ") không đủ để chia vào " + numberOfGroups + " bảng!");
        }

        // 2. Thuật toán xáo trộn ngẫu nhiên (Fisher-Yates)
        Collections.shuffle(clubs);

        // 3. Tạo các Bảng đấu (GroupStage) với type là GROUP
        List<GroupStage> groups = new ArrayList<>();
        char groupName = 'A';
        for (int i = 0; i < numberOfGroups; i++) {
            GroupStage group = GroupStage.builder()
                    .tournament(tournament)
                    .name("Bảng " + (char)(groupName + i))
                    .stageType(StageType.GROUP) // Áp dụng đúng thiết kế Entity của bạn
                    .status(StageStatus.ONGOING)
                    .build();
            groups.add(groupStageRepository.save(group));
        }

        // 4. Chia bài (Round-Robin) vào bảng Standing
        List<Standing> standings = new ArrayList<>();
        for (int i = 0; i < clubs.size(); i++) {
            GroupStage targetGroup = groups.get(i % numberOfGroups);

            Standing standing = Standing.builder()
                    .tournament(tournament)
                    .groupStage(targetGroup)
                    .club(clubs.get(i))
                    .matchesPlayed(0)    // Số trận đã đấu
                    .matchesWon(0)       // Thắng
                    .matchesDrawn(0)     // Hòa
                    .matchesLost(0)      // Thua
                    .scoresFor(0)        // Bàn thắng
                    .scoresAgainst(0)    // Bàn thua
                    .scoreDifference(0)  // Hiệu số
                    .totalPoints(0)      // Tổng điểm
                    .build();
            standings.add(standing);
        }

        tournament.setStatus(TournamentStatus.ONGOING);

        standingRepository.saveAll(standings);
    }

    public List<GroupStageResponse> getGroupsWithTeams(Long tournamentId) {
        // 1. Lấy tất cả các bảng đấu của giải này
        List<GroupStage> stages = groupStageRepository.findByTournamentIdAndStageType(tournamentId, StageType.GROUP);

        return stages.stream().map(stage -> {
            // 2. Với mỗi bảng, lấy danh sách đội từ bảng Standing
            List<Standing> standings = standingRepository.findByGroupStageId(stage.getId());

            List<ClubStandingResponse> teams = standings.stream().map(s ->
                    ClubStandingResponse.builder()
                            .clubId(s.getClub().getId())
                            .name(s.getClub().getName())
                            .logo(s.getClub().getLogoUrl())
                            // Giả sử dùng chính ID hoặc thứ tự trong DB làm hạt giống tạm thời
                            .seed(standings.indexOf(s) + 1)
                            .build()
            ).collect(Collectors.toList());

            return GroupStageResponse.builder()
                    .id(stage.getId())
                    .name(stage.getName())
                    .teams(teams)
                    .build();
        }).collect(Collectors.toList());
    }
}
