package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.LineupType;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.payload.request.referee.ConfirmLineupRequest;
import com.example.tournament.payload.request.referee.RefereeMatchRequest;
import com.example.tournament.payload.response.referee.MatchDetailResponse;
import com.example.tournament.payload.response.referee.RefereeAssignedMatchResponse;
import com.example.tournament.repository.MatchLineupRepository;
import com.example.tournament.repository.MatchRefereeRepository;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.SportRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefereeService {
    private final MatchRefereeRepository matchRefereeRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final SportRuleRepository sportRuleRepository;
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public List<RefereeAssignedMatchResponse> getAssignedMatches(Long refereeId, RefereeMatchRequest request) {

        List<MatchStatus> statuses;
        if ("PAST".equalsIgnoreCase(request.getTimeframe())) {
            statuses = Arrays.asList(MatchStatus.FINISHED, MatchStatus.CANCELED);
        } else {
            statuses = Arrays.asList(MatchStatus.SCHEDULED, MatchStatus.IN_PROGRESS);
        }

        // 2. Kéo dữ liệu từ Database
        List<MatchReferee> assignments = matchRefereeRepository
                .findAssignedMatchesByRefereeAndStatus(refereeId, statuses);

        return assignments.stream().map(mr -> {
            Match m = mr.getMatch();

            String location = m.getTournament().getVenue().getName();
            if (m.getCourt() != null) {
                location += " - " + m.getCourt().getCourtName();
            }

            // Phòng trường hợp trận đấu chờ bốc thăm chưa có CLB
            String homeName = m.getHomeClub() != null ? m.getHomeClub().getName() : "Chưa xác định";
            String awayName = m.getAwayClub() != null ? m.getAwayClub().getName() : "Chưa xác định";

            return RefereeAssignedMatchResponse.builder()
                    .matchId(m.getId())
                    .tournamentName(m.getTournament().getName())
                    .scheduledTime(m.getScheduledTime())
                    .location(location)
                    .matchStatus(m.getStatus().toString())
                    .refereeRole(mr.getRoleInMatch().toString())
                    .homeTeam(RefereeAssignedMatchResponse.MatchClubDto.builder()
                            .id(m.getHomeClub() != null ? m.getHomeClub().getId() : null)
                            .name(homeName)
                            .shortName(m.getHomeClub() != null ? m.getHomeClub().getShortName() : null)
                            .logoUrl(m.getHomeClub() != null ? m.getHomeClub().getLogoUrl() : null)
                            .score(m.getHomeScore())
                            .build())
                    .awayTeam(RefereeAssignedMatchResponse.MatchClubDto.builder()
                            .id(m.getAwayClub() != null ? m.getAwayClub().getId() : null)
                            .name(awayName)
                            .shortName(m.getAwayClub() != null ? m.getAwayClub().getShortName() : null)
                            .logoUrl(m.getAwayClub() != null ? m.getAwayClub().getLogoUrl() : null)
                            .score(m.getAwayScore())
                            .build())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchDetailResponse getMatchDetail(Long refereeId, Long matchId) {
        if (!matchRefereeRepository.existsByMatchIdAndRefereeId(matchId, refereeId)) {
            throw new RuntimeException("Bạn không có quyền truy cập biên bản trận đấu này!");
        }

        Match match = matchRepository.findMatchDetailById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin trận đấu"));

        Map<String, String> rulesMap = sportRuleRepository.findBySportId(match.getTournament().getSport().getId())
                .stream()
                .collect(Collectors.toMap(SportRule::getRuleKey, SportRule::getRuleValue));

        // Lấy toàn bộ danh sách đội hình và phân loại
        List<MatchLineup> allLineups = matchLineupRepository.findLineupsByMatchId(matchId);

        // Xây dựng thông tin địa điểm
        String location = match.getTournament().getVenue().getName();
        if (match.getCourt() != null) {
            location += " - " + match.getCourt().getCourtName();
        }

        return MatchDetailResponse.builder()
                .matchId(match.getId())
                .tournamentName(match.getTournament().getName())
                .sportName(match.getTournament().getSport().getName())
                .scheduledTime(match.getScheduledTime())
                .location(location)
                .status(String.valueOf(match.getStatus()))
                .sportRules(rulesMap)
                .homeTeam(buildTeamLineup(match.getHomeClub(), match.getHomeScore(), allLineups))
                .awayTeam(buildTeamLineup(match.getAwayClub(), match.getAwayScore(), allLineups))
                .build();
    }

    // Hàm bổ trợ để lọc và xây dựng đội hình cho từng CLB
    private MatchDetailResponse.TeamLineupDto buildTeamLineup(Club club, Integer score, List<MatchLineup> allLineups) {
        if (club == null) return null;

        // Lọc danh sách VĐV thuộc về CLB này
        List<MatchLineup> clubLineups = allLineups.stream()
                .filter(ml -> ml.getClub().getId().equals(club.getId()))
                .collect(Collectors.toList());

        return MatchDetailResponse.TeamLineupDto.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .logoUrl(club.getLogoUrl())
                .currentScore(score)
                .startingPlayers(mapToPlayerDto(clubLineups, LineupType.STARTING))
                .substitutePlayers(mapToPlayerDto(clubLineups, LineupType.SUBSTITUTE))
                .build();
    }

    private List<MatchDetailResponse.PlayerDto> mapToPlayerDto(List<MatchLineup> lineups, LineupType type) {
        return lineups.stream()
                .filter(ml -> ml.getLineupType().equals(type))
                .map(ml -> MatchDetailResponse.PlayerDto.builder()
                        .lineupId(ml.getId())
                        .athleteId(ml.getAthlete().getId())
                        .fullName(ml.getAthlete().getUser().getFullName()) // Lấy từ bảng users
                        .identityNumber(ml.getAthlete().getIdentityNumber())
                        .portraitUrl(ml.getAthlete().getPortraitUrl())
                        .jerseyNumber(ml.getJerseyNumber())
                        .position(ml.getPosition())
                        .isConfirmed(ml.getIsConfirmed())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public String confirmLineups(Long refereeId, Long matchId, ConfirmLineupRequest request) {
        if (!matchRefereeRepository.existsByMatchIdAndRefereeId(matchId, refereeId)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên trận đấu này!");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu"));

        if (MatchStatus.FINISHED == match.getStatus() || MatchStatus.CANCELED == match.getStatus()) {
            throw new RuntimeException("Trận đấu đã kết thúc hoặc bị hủy, không thể xác nhận đội hình");
        }

        int updatedCount = matchLineupRepository.bulkConfirmLineups(matchId, request.getLineupIds());

        if (updatedCount == 0) {
            throw new RuntimeException("Không có vận động viên nào được cập nhật. Vui lòng kiểm tra lại danh sách ID.");
        }

        return "Đã xác nhận thành công " + updatedCount + " vận động viên.";
    }
}
