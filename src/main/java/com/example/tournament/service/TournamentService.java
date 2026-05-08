package com.example.tournament.service;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.response.Tournament.CourtResponse;
import com.example.tournament.payload.response.Tournament.TournamentDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentResponse;
import com.example.tournament.payload.response.Tournament.VenueResponse;
import com.example.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;

    public Page<TournamentResponse> getAllTournaments(Pageable pageable, RoleCode role, String name) {


        boolean isAdminOrOrganizer = (role == RoleCode.ADMIN || role == RoleCode.ORGANIZER);

        Page<Tournament> tournaments = tournamentRepository.findAllWithFilters(isAdminOrOrganizer,name, pageable);

        return tournaments.map(t -> TournamentResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .sportName(t.getSport().getName())
                .venueName(t.getVenue().getName())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .status(t.getStatus().name())
                .build());
    }
    public TournamentDetailResponse getTournamentById(Long id) {
        Tournament t = tournamentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // Map danh sách sân của Venue
        List<CourtResponse> courtResponses = t.getVenue().getCourts().stream()
                .map(c -> CourtResponse.builder()
                        .id(c.getId())
                        .name(c.getCourtName())
                        .build())
                .collect(Collectors.toList());

        // Map toàn bộ thông tin Tournament
        return TournamentDetailResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .sportName(t.getSport().getName())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .winPoints(t.getWinPoints())
                .drawPoints(t.getDrawPoints())
                .lostPoints(t.getLossPoints())
                .minAthletes(t.getMinAthletes())
                .format(t.getFormat() != null ? t.getFormat().name() : null)
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .venue(VenueResponse.builder()
                        .id(t.getVenue().getId())
                        .name(t.getVenue().getName())
                        .address(t.getVenue().getAddress())
                        .courts(courtResponses)
                        .build())
                .build();
    }
}
