package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.response.club.DisciplineResponse;
import com.example.tournament.payload.response.club.RegistrationResponse;
import com.example.tournament.payload.response.club.TournamentResponseClub;
import com.example.tournament.repository.*;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.response.Tournament.CourtResponse;
import com.example.tournament.payload.response.Tournament.TournamentDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentResponse;
import com.example.tournament.payload.response.Tournament.VenueResponse;
import com.example.tournament.repository.TournamentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final ClubRepository                  clubRepository;
    private final DisciplineRepository             disciplineRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRepository             tournamentRepository;

    private Club getMyClub() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User manager = userDetails.getUser();
        return clubRepository.findByManager(manager)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Ban chua co CLB nao"));
    }

    // GET /tournaments — Tất cả giải đấu
    public List<TournamentResponseClub> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(t -> TournamentResponseClub.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .sportId(t.getSport().getId())
                        .sportName(t.getSport().getName())
                        .venueId(t.getVenue().getId())
                        .venueName(t.getVenue().getName())
                        .startDate(t.getStartDate().toString())
                        .endDate(t.getEndDate().toString())
                        .winPoints(t.getWinPoints())
                        .drawPoints(t.getDrawPoints())
                        .lossPoints(t.getLossPoints())
                        .minAthletes(t.getMinAthletes())
                        .maxAthletes(t.getMaxAthletes())
                        .format(t.getFormat().name())
                        .status(t.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    // GET /clubs/me/disciplines
    public List<DisciplineResponse> getMyDisciplines() {
        Club club = getMyClub();
        return disciplineRepository.findByClub(club).stream()
                .map(d -> DisciplineResponse.builder()
                        .id(d.getId())
                        .disciplineType(d.getDisciplineType().name())
                        .reason(d.getReason())
                        .fineAmount(d.getFineAmount())
                        .suspensionDuration(d.getSuspensionDuration())
                        .status(d.getStatus().name())
                        .createdAt(d.getCreatedAt().toString())
                        .athleteName(d.getAthlete() != null ? d.getAthlete().getUser().getFullName() : null)
                        .tournamentName(d.getTournament().getName())
                        .build())
                .collect(Collectors.toList());
    }

    // GET /tournaments/registrations/my
    public List<RegistrationResponse> getMyRegistrations() {
        Club club = getMyClub();
        return registrationRepository.findByClub(club).stream()
                .map(r -> RegistrationResponse.builder()
                        .id(r.getId())
                        .tournamentId(r.getTournament().getId())
                        .tournamentName(r.getTournament().getName())
                        .clubId(club.getId())
                        .status(r.getStatus().name())
                        .homeKitColor(r.getHomeKitColor())
                        .awayKitColor(r.getAwayKitColor())
                        .appliedAt(r.getAppliedAt() != null ? r.getAppliedAt().toString() : null)
                        .reviewedAt(r.getReviewedAt() != null ? r.getReviewedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

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
              .winPoints(t.getWinPoints())
              .drawPoints(t.getDrawPoints())
              .lossPoints(t.getLossPoints())
              .minAthletes(t.getMinAthletes())
              .maxAthletes(t.getMaxAthletes())
              .format(t.getFormat() != null ? t.getFormat().name() : null)
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

