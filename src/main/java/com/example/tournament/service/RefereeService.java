package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.EventType;
import com.example.tournament.enums.LineupType;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.request.referee.ChangeMatchStatusRequest;
import com.example.tournament.payload.request.referee.ConfirmLineupRequest;
import com.example.tournament.payload.request.referee.MatchEventRequest;
import com.example.tournament.payload.request.referee.RefereeMatchRequest;
import com.example.tournament.payload.response.referee.MatchDetailResponse;
import com.example.tournament.payload.response.referee.RefereeAssignedMatchResponse;
import com.example.tournament.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MatchEventRepository matchEventRepository;
    private final PlayerStatisticRepository playerStatisticRepository;
    private final ClubRepository clubRepository;
    private final AthleteRepository athleteRepository;

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

        List<MatchEvent> events = matchEventRepository.findByMatchIdOrderByCreatedAtAsc(matchId);
        List<MatchDetailResponse.MatchEventDto> timeline = events.stream()
                .map(e -> MatchDetailResponse.MatchEventDto.builder()
                        .id(e.getId())
                        .eventType(e.getEventType().name())
                        .eventTime(e.getEventTime())
                        .description(e.getDescription())
                        .createdAt(e.getCreatedAt())
                        .clubId(e.getClub() != null ? e.getClub().getId() : null)
                        .primaryAthleteName(e.getPrimaryAthlete() != null ? e.getPrimaryAthlete().getUser().getFullName() : null)
                        .primaryAthleteNumber(e.getPrimaryAthlete() != null ? e.getPrimaryAthlete().getPreferredNumber() : null)
                        .secondaryAthleteName(e.getSecondaryAthlete() != null ? e.getSecondaryAthlete().getUser().getFullName() : null)
                        .secondaryAthleteNumber(e.getSecondaryAthlete() != null ? e.getSecondaryAthlete().getPreferredNumber() : null)
                        .build())
                .collect(Collectors.toList());

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
                .timeline(timeline)
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

    @Transactional
    public String changeMatchStatus(Long refereeId, Long matchId, ChangeMatchStatusRequest request) {
        if (!matchRefereeRepository.existsByMatchIdAndRefereeId(matchId, refereeId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác trên trận đấu này!");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new AppException("Không tìm thấy trận đấu"));

        String currentStatus = String.valueOf(match.getStatus());
        String targetStatus = request.getTargetStatus();

        // Kiem tra chuyen doi trang thai
        validateStateTransition(currentStatus, targetStatus);

        //  Kiểm tra điều kiện tiên quyết nếu muốn "Bắt đầu trận"
        if ("SCHEDULED".equals(currentStatus) && "IN_PROGRESS".equals(targetStatus)) {
            validatePreConditionsForStart(match);
        }

        // Đổi trạng thái
        match.setStatus(MatchStatus.valueOf(targetStatus));

        // Lưu vết sự kiện vào Event
        EventType type;
        String friendlyMessage = "";

        switch (targetStatus) {
            case "IN_PROGRESS":
                if ("SCHEDULED".equals(currentStatus)) {
                    type = EventType.MATCH_START;
                    friendlyMessage = "Bắt đầu trận đấu";
                } else {
                    type = EventType.MATCH_RESUME;
                    friendlyMessage = "Trận đấu được tiếp tục trở lại";
                }
                break;
            case "PAUSED":
                type = EventType.MATCH_PAUSE;
                friendlyMessage = "Trọng tài cho tạm dừng trận đấu";
                break;
            case "FINISHED":
                type = EventType.MATCH_END;
                friendlyMessage = "Trọng tài thổi còi kết thúc trận đấu";
                break;
            case "CANCELED":
                type = EventType.MATCH_CANCEL;
                friendlyMessage = "Trận đấu đã bị hủy bỏ";
                break;
            default:
                throw new AppException("Trạng thái mục tiêu không hợp lệ để ghi log.");
        }

        // Gộp note vào description
        String eventDescription = friendlyMessage;
        if (request.getNote() != null && !request.getNote().isBlank()) {
            eventDescription += " (Lý do: " + request.getNote() + ")";
        }

        String timeToLog = (request.getEventTime() != null && !request.getEventTime().isBlank())
                ? request.getEventTime()
                : "0";

        // Khởi tạo và lưu Event
        MatchEvent event = MatchEvent.builder()
                .match(match)
                .eventType(type)
                .eventTime(timeToLog)
                .description(eventDescription)
                .build();

        matchEventRepository.save(event);

        return "Đã chuyển trạng thái trận đấu sang: " + targetStatus;
    }

    private void validateStateTransition(String current, String target) {
        if ("FINISHED".equals(current) || "CANCELED".equals(current)) {
            throw new AppException("Trận đấu đã kết thúc hoặc bị hủy, không thể thay đổi trạng thái.");
        }
        if ("SCHEDULED".equals(current) && !List.of("IN_PROGRESS", "CANCELED").contains(target)) {
            throw new AppException("Trận đấu chưa bắt đầu chỉ có thể chuyển sang (Bắt đầu) hoặc (Hủy).");
        }
        if ("IN_PROGRESS".equals(current) && !List.of("PAUSED", "FINISHED").contains(target)) {
            throw new AppException("Trận đấu đang diễn ra chỉ có thể  (Tạm dừng) hoặc  (Kết thúc).");
        }
        if ("PAUSED".equals(current) && !List.of("IN_PROGRESS", "FINISHED").contains(target)) {
            throw new AppException("Trận đấu đang tạm dừng chỉ có thể (Tiếp tục) hoặc (Kết thúc).");
        }
    }

    private void validatePreConditionsForStart(Match match) {
        // Lấy số lượng tối thiểu từ luật
        int minPlayers = sportRuleRepository.findBySportId(match.getTournament().getSport().getId())
                .stream()
                .filter(r -> "MIN_STARTING_PLAYERS".equals(r.getRuleKey()))
                .map(r -> Integer.parseInt(r.getRuleValue()))
                .findFirst()
                .orElse(1);

        // Đếm số lượng VĐV ĐÁ CHÍNH đã được xác nhận của 2 đội
        long homeConfirmed = matchLineupRepository.countByMatchIdAndClubIdAndLineupTypeAndIsConfirmed(
                match.getId(), match.getHomeClub().getId(), LineupType.STARTING, true);

        long awayConfirmed = matchLineupRepository.countByMatchIdAndClubIdAndLineupTypeAndIsConfirmed(
                match.getId(), match.getAwayClub().getId(), LineupType.STARTING, true);

        if (homeConfirmed < minPlayers) {
            throw new AppException("Đội " + match.getHomeClub().getName() + " chưa đủ số lượng VĐV ra sân tối thiểu (" + minPlayers + "). Vui lòng duyệt thêm.");
        }
        if (awayConfirmed < minPlayers) {
            throw new AppException("Đội " + match.getAwayClub().getName() + " chưa đủ số lượng VĐV ra sân tối thiểu (" + minPlayers + "). Vui lòng duyệt thêm.");
        }
    }

    @Transactional
    public String recordMatchEvent(Long refereeId, Long matchId, MatchEventRequest request) {
        if (!matchRefereeRepository.existsByMatchIdAndRefereeId(matchId, refereeId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác trên trận đấu này!");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Trận đấu", "id", matchId));

        EventType eventType;
        try {
            eventType = EventType.valueOf(request.getEventType());
        } catch (IllegalArgumentException e) {
            throw new AppException("Loại sự kiện không hợp lệ: " + request.getEventType());
        }

        if (eventType == EventType.RESUME_MATCH) {
            if (!MatchStatus.PAUSED.equals(match.getStatus())) {
                throw new AppException("Chỉ có thể tiếp tục khi trận đấu đang tạm dừng.");
            }
            match.setStatus(MatchStatus.IN_PROGRESS);

        } else if (eventType == EventType.PAUSE_MATCH) {
            if (!MatchStatus.IN_PROGRESS.equals(match.getStatus())) {
                throw new AppException("Chỉ có thể tạm dừng khi trận đấu đang diễn ra.");
            }
            match.setStatus(MatchStatus.PAUSED);

        } else {
            if (!MatchStatus.IN_PROGRESS.equals(match.getStatus())) {
                throw new AppException("Chỉ có thể ghi nhận sự kiện khi trận đấu đang diễn ra.");
            }
        }

        Club club = request.getClubId() != null
                ? clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new ResourceNotFoundException("Câu lạc bộ", "id", request.getClubId()))
                : null;

        Athlete primaryAthlete = request.getPrimaryAthleteId() != null
                ? athleteRepository.findById(request.getPrimaryAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Vận động viên", "id", request.getPrimaryAthleteId()))
                : null;

        Athlete secondaryAthlete = request.getSecondaryAthleteId() != null
                ? athleteRepository.findById(request.getSecondaryAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Vận động viên", "id", request.getSecondaryAthleteId()))
                : null;

        MatchEvent event = MatchEvent.builder()
                .match(match)
                .club(club)
                .primaryAthlete(primaryAthlete)
                .secondaryAthlete(secondaryAthlete)
                .eventType(eventType)
                .eventTime(request.getEventTime())
                .description(request.getDescription())
                .build();
        matchEventRepository.save(event);

        processEventSideEffects(match, club, primaryAthlete, secondaryAthlete, eventType);

        matchRepository.save(match);

        return "Đã ghi nhận sự kiện: " + eventType.name() + " thành công.";
    }

    private void processEventSideEffects(Match match, Club club, Athlete primary, Athlete secondary, EventType type) {
        // --- XỬ LÝ ĐIỂM SỐ TRẬN ĐẤU ---
        if (List.of(EventType.GOAL, EventType.PT_1, EventType.PT_2, EventType.PT_3, EventType.OWN_GOAL).contains(type)) {
            if (club == null) throw new AppException("Sự kiện ghi điểm bắt buộc phải gửi kèm ClubID");

            // Tính số điểm cần cộng
            int pointsToAdd = 1; // Mặc định GOAL, POINT_1, OWN_GOAL là 1 điểm
            if (type == EventType.PT_2) pointsToAdd = 2;
            if (type == EventType.PT_3) pointsToAdd = 3;

            boolean isHomeClub = club.getId().equals(match.getHomeClub().getId());

            // Xử lý Phản lưới nhà (Điểm cộng cho đội ĐỐI PHƯƠNG)
            if (type == EventType.OWN_GOAL) {
                if (isHomeClub) match.setAwayScore(match.getAwayScore() + pointsToAdd);
                else match.setHomeScore(match.getHomeScore() + pointsToAdd);
            } else {
                // Bình thường (Điểm cộng cho CHÍNH ĐỘI MÌNH)
                if (isHomeClub) match.setHomeScore(match.getHomeScore() + pointsToAdd);
                else match.setAwayScore(match.getAwayScore() + pointsToAdd);
            }
        }

        // --- XỬ LÝ THỐNG KÊ CÁ NHÂN VĐV ---
        if (primary != null && club != null) {
            // Tìm bản ghi thống kê cũ, nếu chưa có thì tạo mới
            PlayerStatistic stats = playerStatisticRepository
                    .findByTournamentIdAndAthleteId(match.getTournament().getId(), primary.getId())
                    .orElseGet(() -> PlayerStatistic.builder()
                            .tournament(match.getTournament())
                            .athlete(primary)
                            .club(club)
                            .matchesPlayed(0)
                            .scores(0).assists(0).fouls(0).mvpCount(0)
                            .build());

            // Cộng bàn thắng
            if (List.of(EventType.GOAL, EventType.PT_1, EventType.PT_2, EventType.PT_3).contains(type)) {
                int pointsToAdd = (type == EventType.PT_3) ? 3 : ((type == EventType.PT_2) ? 2 : 1);
                stats.setScores(stats.getScores() + pointsToAdd);
            }
            // Cộng thẻ phạt / Phạm lỗi
            else if (List.of(EventType.YELLOW_CARD, EventType.RED_CARD, EventType.FOUL, EventType.TECHNICAL_FOUL).contains(type)) {
                stats.setFouls(stats.getFouls() + 1);
            }

            playerStatisticRepository.save(stats);
        }

        // --- XỬ LÝ KIẾN TẠO  ---
        if (secondary != null && type == EventType.GOAL) {
            PlayerStatistic secondaryStats = playerStatisticRepository
                    .findByTournamentIdAndAthleteId(match.getTournament().getId(), secondary.getId())
                    .orElseGet(() -> PlayerStatistic.builder()
                            .tournament(match.getTournament())
                            .athlete(secondary)
                            .club(club)
                            .scores(0).assists(0).fouls(0).matchesPlayed(0).mvpCount(0)
                            .build());

            secondaryStats.setAssists(secondaryStats.getAssists() + 1);
            playerStatisticRepository.save(secondaryStats);
        }

        // --- XỬ LÝ HOÁN ĐỔI ĐỘI HÌNH ---
        if (type == EventType.SUBSTITUTION) {
            if (primary == null || secondary == null) {
                throw new AppException("Sự kiện thay người bắt buộc phải có cầu thủ rời sân và cầu thủ vào sân.");
            }

            // Tìm bản ghi của người RỜI SÂN
            MatchLineup leavingPlayer = matchLineupRepository
                    .findByMatchIdAndAthleteId(match.getId(), primary.getId())
                    .orElseThrow(() -> new AppException("Không tìm thấy cầu thủ rời sân (ID: " + primary.getId() + ") trong đội hình trận này."));

            // Tìm bản ghi của người VÀO SÂN
            MatchLineup enteringPlayer = matchLineupRepository
                    .findByMatchIdAndAthleteId(match.getId(), secondary.getId())
                    .orElseThrow(() -> new AppException("Không tìm thấy cầu thủ dự bị (ID: " + secondary.getId() + ") trong đội hình trận này."));

            // Thực hiện HOÁN ĐỔI trạng thái
            leavingPlayer.setLineupType(LineupType.SUBSTITUTE); // Đẩy ra ghế dự bị
            enteringPlayer.setLineupType(LineupType.STARTING);  // Đưa vào đá chính

            matchLineupRepository.saveAll(List.of(leavingPlayer, enteringPlayer));
        }
    }
}
