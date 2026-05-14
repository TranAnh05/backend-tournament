package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.LineupType;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.request.club.SubmitLineupRequest;
import com.example.tournament.payload.response.club.MatchEventResponse;
import com.example.tournament.payload.response.club.MatchResponse;
import com.example.tournament.repository.*;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository        matchRepository;
    private final MatchLineupRepository  matchLineupRepository;
    private final ClubRepository         clubRepository;
    private final AthleteRepository      athleteRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        if (userDetails == null) throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
        return userDetails.getUser();
    }

    private Club getMyClub() {
        User manager = getCurrentUser();
        return clubRepository.findByManager(manager)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Ban chua co CLB nao"));
    }

    private Club currentClubCache = null;

    private Club getMyClubSafe() {
        try { return getMyClub(); } catch (Exception e) { return null; }
    }

    private MatchResponse toResponse(Match m) {
        if (currentClubCache == null) currentClubCache = getMyClubSafe();
        final Club club = currentClubCache;

        List<MatchEventResponse> events = m.getEvents().stream()
                .filter(e -> Boolean.FALSE.equals(e.getIsDeleted()))
                .map(e -> MatchEventResponse.builder()
                        .id(e.getId())
                        .eventType(e.getEventType().name())
                        .eventTime(e.getEventTime())
                        .primaryAthleteName(
                                e.getPrimaryAthlete() != null
                                        ? e.getPrimaryAthlete().getUser().getFullName()
                                        : null)
                        .clubId(e.getClub() != null ? e.getClub().getId() : null)
                        .build())
                .collect(Collectors.toList());

        // Kiểm tra CLB đã nộp đội hình cho trận này chưa
        boolean hasLineup = club != null && matchLineupRepository.findByMatch(m)
                .stream().anyMatch(l -> l.getClub().getId().equals(club.getId()));

        return MatchResponse.builder()
                .id(m.getId())
                .tournamentId(m.getTournament().getId())
                .tournamentName(m.getTournament().getName())
                .groupStageName(m.getGroupStage().getName())
                .homeClubId(m.getHomeClub() != null ? m.getHomeClub().getId() : null)
                .homeClubName(m.getHomeClub() != null ? m.getHomeClub().getName() : "TBD")
                .homeClubShortName(m.getHomeClub() != null ? m.getHomeClub().getShortName() : "TBD")
                .awayClubId(m.getAwayClub() != null ? m.getAwayClub().getId() : null)
                .awayClubName(m.getAwayClub() != null ? m.getAwayClub().getName() : "TBD")
                .awayClubShortName(m.getAwayClub() != null ? m.getAwayClub().getShortName() : "TBD")
                .scheduledTime(m.getScheduledTime().toString())
                .status(m.getStatus().name())
                .homeScore(m.getHomeScore())
                .awayScore(m.getAwayScore())
                .events(events)
                .hasLineup(hasLineup)
                .build();
    }

    public List<MatchResponse> getMyMatches() {
        currentClubCache = null; // reset cache mỗi request
        Club club = getMyClub();
        currentClubCache = club;
        return matchRepository.findByClub(club).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitLineup(Long matchId, SubmitLineupRequest request) {
        Club club = getMyClub();

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay tran dau"));

        boolean isParticipant = (match.getHomeClub() != null && match.getHomeClub().getId().equals(club.getId()))
                || (match.getAwayClub() != null && match.getAwayClub().getId().equals(club.getId()));

        if (!isParticipant) {
            throw new AppException(HttpStatus.FORBIDDEN, "CLB cua ban khong tham gia tran dau nay");
        }

        List<MatchLineup> oldLineups = matchLineupRepository.findByMatch(match).stream()
                .filter(l -> l.getClub().getId().equals(club.getId()))
                .collect(Collectors.toList());
        matchLineupRepository.deleteAll(oldLineups);
        matchLineupRepository.deleteAll(oldLineups);
        matchLineupRepository.flush(); // ← thêm dòng này

        List<MatchLineup> newLineups = request.getLineups().stream().map(item -> {
            Athlete athlete = athleteRepository.findById(item.getAthleteId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay VDV: " + item.getAthleteId()));

            return MatchLineup.builder()
                    .match(match)
                    .club(club)
                    .athlete(athlete)
                    .lineupType(LineupType.valueOf(item.getLineupType()))
                    .jerseyNumber(item.getJerseyNumber())
                    .position(item.getPosition())
                    .build();
        }).collect(Collectors.toList());

        matchLineupRepository.saveAll(newLineups);
    }


}