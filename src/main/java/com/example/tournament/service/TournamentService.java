package com.example.tournament.service;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.payload.response.Tournament.TournamentResponse;
import com.example.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}
