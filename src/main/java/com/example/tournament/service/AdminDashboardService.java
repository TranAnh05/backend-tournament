package com.example.tournament.service;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.UserStatusLog;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.enums.UserStatus;
import com.example.tournament.payload.response.admin.*;
import com.example.tournament.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final UserStatusLogRepository userStatusLogRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public List<AdminActivityLogResponse> getRecentActivities() {
        Pageable topTen = PageRequest.of(0, 10);
        List<UserStatusLog> logs = userStatusLogRepository.findRecentActivities(topTen);

        return logs.stream().map(log -> {
            String action = log.getNewStatus() == UserStatus.ACTIVE ? "được mở khóa" : "bị khóa";
            String targetUser = log.getUser().getFullName();
            String adminUser = log.getChangedBy().getFullName();

            String message = String.format("Tài khoản BTC '%s' vừa %s bởi Admin '%s'. Lý do: %s",
                    targetUser, action, adminUser, log.getReason());

            return AdminActivityLogResponse.builder()
                    .type("USER_STATUS_CHANGED")
                    .message(message)
                    .createdAt(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminLiveMatchResponse> getLiveMatches() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Match> matches = matchRepository.findLiveAndUpcomingMatches(startOfDay, endOfDay);

        return matches.stream().map(m -> {
            // Logic tính số phút đang đá
            Integer liveMinute = null;
            if ("IN_PROGRESS".equals(m.getStatus().toString())) {
                long minutesPassed = Duration.between(m.getScheduledTime(), LocalDateTime.now()).toMinutes();
                liveMinute = minutesPassed > 0 ? (int) minutesPassed : 1;
            }

            String venueLocation = m.getTournament().getVenue().getName();
            if (m.getCourt() != null) {
                venueLocation = m.getCourt().getVenue().getName() + " - " + m.getCourt().getCourtName();
            }

            String homeName = m.getHomeClub().getShortName() != null ? m.getHomeClub().getShortName() : m.getHomeClub().getName();
            String awayName = m.getAwayClub().getShortName() != null ? m.getAwayClub().getShortName() : m.getAwayClub().getName();

            return AdminLiveMatchResponse.builder()
                    .matchId(m.getId())
                    .tournamentName(m.getTournament().getName())
                    .sportName(m.getTournament().getSport().getName())
                    .startTime(m.getScheduledTime())
                    .venueName(venueLocation)
                    .status(m.getStatus().toString())
                    .liveMinute(liveMinute)
                    .homeTeam(AdminTeamDto.builder()
                            .id(m.getHomeClub().getId())
                            .name(homeName)
                            .logoUrl(m.getHomeClub().getLogoUrl())
                            .score(m.getHomeScore())
                            .build())
                    .awayTeam(AdminTeamDto.builder()
                            .id(m.getAwayClub().getId())
                            .name(awayName)
                            .logoUrl(m.getAwayClub().getLogoUrl())
                            .score(m.getAwayScore())
                            .build())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDashboardChartsResponse getDashboardCharts(AdminDashboardFilterRequest request) {

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        return AdminDashboardChartsResponse.builder()
                .tournamentsBySport(tournamentRepository.countTournamentsBySport(startDateTime, endDateTime))
                .venueUsage(tournamentRepository.countVenueUsage(startDateTime, endDateTime))
                .activityTrends(tournamentRepository.countActivityTrends(startDateTime, endDateTime))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardKpiResponse getDashboardKpis(AdminDashboardFilterRequest request) {
        // Xác định khung thời gian HIỆN TẠI
        LocalDateTime currentStart = request.getStartDate().atStartOfDay();
        LocalDateTime currentEnd = request.getEndDate().atTime(23, 59, 59);

        // Tính toán khung thời gian KỲ TRƯỚC
        long daysBetween = ChronoUnit.DAYS.between(currentStart, currentEnd) + 1;
        LocalDateTime previousStart = currentStart.minusDays(daysBetween);
        LocalDateTime previousEnd = currentEnd.minusDays(daysBetween);

        // THỰC THI ĐA LUỒNG - Bắn 8 query cùng lúc xuống Database
        CompletableFuture<Long> currentTournaments = CompletableFuture.supplyAsync(() -> tournamentRepository.countByCreatedAtBetween(currentStart, currentEnd));
        CompletableFuture<Long> prevTournaments = CompletableFuture.supplyAsync(() -> tournamentRepository.countByCreatedAtBetween(previousStart, previousEnd));

        CompletableFuture<Long> currentUsers = CompletableFuture.supplyAsync(() -> userRepository.countByCreatedAtBetween(currentStart, currentEnd));
        CompletableFuture<Long> prevUsers = CompletableFuture.supplyAsync(() -> userRepository.countByCreatedAtBetween(previousStart, previousEnd));

        CompletableFuture<Long> currentClubs = CompletableFuture.supplyAsync(() -> clubRepository.countByCreatedAtBetween(currentStart, currentEnd));
        CompletableFuture<Long> prevClubs = CompletableFuture.supplyAsync(() -> clubRepository.countByCreatedAtBetween(previousStart, previousEnd));

        CompletableFuture<Long> currentMatches = CompletableFuture.supplyAsync(() -> matchRepository.countByStatusAndUpdatedAtBetween(MatchStatus.FINISHED, currentStart, currentEnd));
        CompletableFuture<Long> prevMatches = CompletableFuture.supplyAsync(() -> matchRepository.countByStatusAndUpdatedAtBetween(MatchStatus.FINISHED, previousStart, previousEnd));

        // Chờ tất cả các luồng chạy xong
        CompletableFuture.allOf(
                currentTournaments, prevTournaments,
                currentUsers, prevUsers,
                currentClubs, prevClubs,
                currentMatches, prevMatches
        ).join();

        // Đóng gói kết quả
        return AdminDashboardKpiResponse.builder()
                .totalTournaments(buildKpiMetric(currentTournaments.join(), prevTournaments.join()))
                .totalUsers(buildKpiMetric(currentUsers.join(), prevUsers.join()))
                .totalClubs(buildKpiMetric(currentClubs.join(), prevClubs.join()))
                .totalMatchesPlayed(buildKpiMetric(currentMatches.join(), prevMatches.join()))
                .build();
    }

    // Hàm tiện ích tính Tỷ lệ tăng trưởng  và xử lý lỗi chia cho 0
    private AdminKpiMetricDto buildKpiMetric(Long current, Long previous) {
        Double trend = 0.0;
        if (previous > 0) {
            trend = ((double) (current - previous) / previous) * 100;
        } else if (current > 0) {
            trend = 100.0; // Nếu kỳ trước bằng 0 mà kỳ này có, coi như tăng trưởng 100%
        }

        // Làm tròn 1 chữ số thập phân (VD: 12.5)
        BigDecimal bd = new BigDecimal(trend).setScale(1, RoundingMode.HALF_UP);

        return AdminKpiMetricDto.builder()
                .value(current)
                .trend(bd.doubleValue())
                .build();
    }
}
