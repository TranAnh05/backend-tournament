package com.example.tournament.service;


import com.example.tournament.entity.Match;
import com.example.tournament.payload.response.Tournament.ClubSummaryResponse;
import com.example.tournament.payload.response.Tournament.OrganizerMatchResponse;
import com.example.tournament.payload.response.club.MatchResponse;
import com.example.tournament.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerMatchService {

    private final MatchRepository matchRepository;

    public List<OrganizerMatchResponse> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);

        return matches.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
    }

    private OrganizerMatchResponse mapToMatchResponse(Match match) {
        // Mapping thông tin đội nhà
        ClubSummaryResponse home = match.getHomeClub() != null ? ClubSummaryResponse.builder()
                .id(match.getHomeClub().getId())
                .name(match.getHomeClub().getName())
                .logo(match.getHomeClub().getLogoUrl())
                .build() : null;

        // Mapping thông tin đội khách
        ClubSummaryResponse away = match.getAwayClub() != null ? ClubSummaryResponse.builder()
                .id(match.getAwayClub().getId())
                .name(match.getAwayClub().getName())
                .logo(match.getAwayClub().getLogoUrl())
                .build() : null;

        return OrganizerMatchResponse.builder()
                .id(match.getId())
                .scheduledTime(match.getScheduledTime())
                .status(match.getStatus())
                .groupStageName(match.getGroupStage() != null ? match.getGroupStage().getName() : "N/A")
                .homeClub(home)
                .awayClub(away)
                .build();
    }
}
