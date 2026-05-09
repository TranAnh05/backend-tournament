package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.enums.TournamentFormat;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.Tournament.TournamentRequest;
import com.example.tournament.payload.response.admin.SportResponse;
import com.example.tournament.payload.response.admin.VenueResponse;
import com.example.tournament.payload.response.club.DisciplineResponse;
import com.example.tournament.payload.response.club.RegistrationResponse;
import com.example.tournament.payload.response.club.TournamentResponseClub;
import com.example.tournament.repository.*;
import com.example.tournament.security.userdetail.CustomUserDetails;
import jakarta.transaction.Transactional;
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
import com.example.tournament.payload.response.Tournament.VenueCourtResponse;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.entity.TournamentRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final ClubRepository                  clubRepository;
    private final DisciplineRepository             disciplineRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRepository             tournamentRepository;
    private final SportRepository                   sportRepository;
    private final VenueRepository       venueRepository;

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

    //kiet them phan nay
    // POST /tournaments/{tournamentId}/register
    public RegistrationResponse registerTournament(Long tournamentId, String homeKitColor, String awayKitColor, String financialProofUrl) {
        Club club = getMyClub();

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy giải đấu"));

        // Kiểm tra đã đăng ký chưa
        registrationRepository.findByTournamentIdAndClub(tournamentId, club).ifPresent(r -> {
            throw new AppException(HttpStatus.CONFLICT, "CLB đã đăng ký giải đấu này rồi");
        });

        TournamentRegistration reg = TournamentRegistration.builder()
                .tournament(tournament)
                .club(club)
                .status(RegistrationStatus.PENDING)
                .homeKitColor(homeKitColor)
                .awayKitColor(awayKitColor)
                .financialProofUrl(financialProofUrl)
                .build();

        TournamentRegistration saved = registrationRepository.save(reg);

        return RegistrationResponse.builder()
                .id(saved.getId())
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .clubId(club.getId())
                .status(saved.getStatus().name())
                .homeKitColor(saved.getHomeKitColor())
                .awayKitColor(saved.getAwayKitColor())
                .appliedAt(saved.getAppliedAt() != null ? saved.getAppliedAt().toString() : null)
                .reviewedAt(null)
                .build();
    }

    // DELETE /tournaments/{tournamentId}/withdraw
    public void withdrawTournament(Long tournamentId) {
        Club club = getMyClub();

        TournamentRegistration reg = registrationRepository.findByTournamentIdAndClub(tournamentId, club)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đăng ký"));

        if (reg.getStatus() == RegistrationStatus.WITHDRAWN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đã rút đơn trước đó rồi");
        }

        reg.setStatus(RegistrationStatus.WITHDRAWN);
        registrationRepository.save(reg);
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
                .maxAthletes(t.getMaxAthletes())
                .format(t.getFormat() != null ? t.getFormat().name() : null)
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .venue(VenueCourtResponse.builder()
                        .id(t.getVenue().getId())
                        .name(t.getVenue().getName())
                        .address(t.getVenue().getAddress())
                        .courts(courtResponses)
                        .build())
                .build();
    }

    @Transactional
    public TournamentDetailResponse createTournament(TournamentRequest request) {
        // 1. Tìm Sport và Venue (Sử dụng ResourceNotFoundException của Trung)
        Sport sport = sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", request.getSportId()));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.getVenueId()));

        // 2. Chuyển đổi DTO sang Entity
        Tournament tournament = new Tournament();
        tournament.setName(request.getName());
        tournament.setSport(sport);
        tournament.setVenue(venue);
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setMinAthletes(request.getMinAthletes());
        tournament.setMaxAthletes(request.getMaxAthletes());
        tournament.setWinPoints(request.getWinPoints());
        tournament.setDrawPoints(request.getDrawPoints());
        tournament.setLossPoints(request.getLostPoints());


        // Ép kiểu Enum cho Format nếu cần
        if (request.getFormat() != null) {
            tournament.setFormat(TournamentFormat.valueOf(request.getFormat()));
        }

        // 3. THIẾT LẬP TRẠNG THÁI MẶC ĐỊNH
        tournament.setStatus(TournamentStatus.DRAFT);

        // 4. Lưu vào Database
        Tournament savedTournament = tournamentRepository.save(tournament);

        // 5. Trả về Response DTO (Tận dụng hàm mapToResponse đã viết ở chức năng Detail)
        return mapToTournamentDetailResponse(savedTournament);
    }
    private TournamentDetailResponse mapToTournamentDetailResponse(Tournament t) {
        // 1. Map danh sách sân (Courts) từ Venue
        List<CourtResponse> courtResponses = new ArrayList<>();
        if (t.getVenue() != null && t.getVenue().getCourts() != null) {
            courtResponses = t.getVenue().getCourts().stream()
                    .map(c -> CourtResponse.builder()
                            .id(c.getId())
                            .name(c.getCourtName())
                            .build())
                    .collect(Collectors.toList());
        }

        // 2. Map thông tin Venue
        VenueCourtResponse venueResponse = null;
        if (t.getVenue() != null) {
            venueResponse = VenueCourtResponse.builder()
                    .id(t.getVenue().getId())
                    .name(t.getVenue().getName())
                    .address(t.getVenue().getAddress())
                    .courts(courtResponses)
                    .build();
        }

        // 3. Map tổng thể TournamentDetailResponse
        return TournamentDetailResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .sportName(t.getSport() != null ? t.getSport().getName() : null)
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .winPoints(t.getWinPoints())
                .drawPoints(t.getDrawPoints())
                .lostPoints(t.getLossPoints())
                .minAthletes(t.getMinAthletes())
                .maxAthletes(t.getMaxAthletes())
                .format(t.getFormat() != null ? t.getFormat().name() : null)
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .venue(venueResponse)
                .build();
    }


    public List<SportResponse> getAllSportsForSelect() {
        // Trả về List thuần túy thay vì Page để Frontend dễ dùng trong Select
        return sportRepository.findAll().stream()
                .map(sport -> SportResponse.builder()
                        .id(sport.getId())
                        .name(sport.getName())
                        .build()) // Tuyệt đối không map trường rules vào đây nếu không cần thiết
                .toList();
    }
    public List<VenueResponse> getAllVenuesForSelect() {
        return venueRepository.findAll().stream()
                .map(venues -> VenueResponse.builder()
                        .id(venues.getId())
                        .name(venues.getName())
                        .build())
                .collect(Collectors.toList());
    }

    // Sửa đổi kiểu trả về thành TournamentDetailResponse để đồng bộ với hàm Mapper
    @Transactional
    public TournamentDetailResponse updateTournament(Long id, TournamentRequest request) {
        // 1. Tìm giải đấu hiện tại
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // 2. Chỉ cho phép sửa nếu giải đấu đang ở trạng thái nháp (DRAFT)
        if (!t.getStatus().equals(TournamentStatus.DRAFT )) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ có thể sửa giải đấu khi ở trạng thái DRAFT");
        }

        // 3. Kiểm tra logic ngày tháng
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // 4. Tìm Sport & Venue mới từ request
        Sport sport = sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", request.getSportId()));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.getVenueId()));

        // 5. Cập nhật dữ liệu vào Entity
        t.setName(request.getName());
        t.setSport(sport);
        t.setVenue(venue);
        t.setStartDate(request.getStartDate());
        t.setEndDate(request.getEndDate());
        t.setMinAthletes(request.getMinAthletes());
        t.setMaxAthletes(request.getMaxAthletes());
        t.setWinPoints(request.getWinPoints());
        t.setDrawPoints(request.getDrawPoints());
        t.setLossPoints(request.getLostPoints());

        if (request.getFormat() != null) {
            t.setFormat(TournamentFormat.valueOf(request.getFormat()));
        }

        // 6. Lưu vào Database
        Tournament savedTournament = tournamentRepository.save(t);

        // 7. Trả về Response Detail (Sử dụng hàm mapper đã có sẵn để tránh lỗi lặp JSON)
        return mapToTournamentDetailResponse(savedTournament);
    }

}

