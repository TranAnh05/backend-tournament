//clb
package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.JoinStatus;
import com.example.tournament.enums.RosterRole;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.request.club.*;
import com.example.tournament.payload.response.club.*;
import com.example.tournament.repository.*;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.tournament.payload.response.club.TournamentHistoryResponse;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TournamentRegistrationRepository;

import java.util.Map;
import java.util.stream.Collectors;
import com.example.tournament.entity.TournamentRoster;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.enums.RosterRole;
import com.example.tournament.enums.RosterStatus;
import com.example.tournament.payload.request.club.RosterRequest;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentRosterRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository        clubRepository;
    private final ClubMemberRepository  clubMemberRepository;
    private final AthleteRepository     athleteRepository;
    private final VenueRepository       venueRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final StandingRepository standingRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentRosterRepository rosterRepository;

    // ─── Helper: lấy User đang đăng nhập ────────────────────────
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser();
    }

    // ─── Helper: lấy CLB của manager hiện tại ───────────────────
    private Club getMyClub() {
        User manager = getCurrentUser();
        return clubRepository.findByManager(manager)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bạn chưa có CLB nào"));
    }

    // ─── Helper: map Club → ClubResponse ────────────────────────
    private ClubResponse toClubResponse(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .shortName(club.getShortName())
                .logoUrl(club.getLogoUrl())
                .headquarters(club.getHeadquarters())
                .homeVenueId(club.getHomeVenue() != null ? club.getHomeVenue().getId() : null)
                .homeVenueName(club.getHomeVenue() != null ? club.getHomeVenue().getName() : null)
                .contactEmail(club.getContactEmail())
                .contactPhone(club.getContactPhone())
                .status(club.getStatus().name())
                .managerName(club.getManager().getFullName())
                .build();
    }

    // ─── Helper: map ClubMember → ClubMemberResponse ────────────
    private ClubMemberResponse toMemberResponse(ClubMember cm) {
        Athlete ath = cm.getAthlete();
        User user = ath.getUser();
        return ClubMemberResponse.builder()
                .memberId(cm.getId())
                .athleteId(ath.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber()) // 👈 thêm dòng này
                .identityNumber(ath.getIdentityNumber())
                .dateOfBirth(ath.getDateOfBirth())
                .preferredNumber(ath.getPreferredNumber())
                .preferredPosition(ath.getPreferredPosition())
                .healthStatus(ath.getHealthStatus())
                .clubRole(cm.getClubRole())
                .joinStatus(cm.getJoinStatus())
                .joinedDate(cm.getJoinedDate())
                .leftDate(cm.getLeftDate())
                .build();
    }

    private TournamentHistoryResponse toTournamentHistory(TournamentRegistration reg, Club club) {
        Tournament tournament;
        try {
            tournament = reg.getTournament();
            tournament.getName(); // force init proxy — nếu tournament bị xóa sẽ throw ở đây
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return null; // bỏ qua registration này
        }

        Standing standing = standingRepository
                .findByTournamentIdAndClub(tournament.getId(), club)
                .orElse(null);

        Integer ranking = null;
        if (standing != null) {
            List<Standing> allStandings = standing.getGroupStage() != null
                    ? standingRepository.findByGroupStageIdOrderByTotalPointsDesc(standing.getGroupStage().getId())
                    : java.util.Collections.emptyList();
            for (int i = 0; i < allStandings.size(); i++) {
                if (allStandings.get(i).getClub().getId().equals(club.getId())) {
                    ranking = i + 1;
                    break;
                }
            }
        }

        return TournamentHistoryResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .season(tournament.getName())
                .registrationStatus(reg.getStatus().name())
                .matchesPlayed(standing != null ? standing.getMatchesPlayed() : 0)
                .matchesWon(standing != null ? standing.getMatchesWon() : 0)
                .matchesDrawn(standing != null ? standing.getMatchesDrawn() : 0)
                .matchesLost(standing != null ? standing.getMatchesLost() : 0)
                .totalPoints(standing != null ? standing.getTotalPoints() : 0)
                .ranking(ranking)
                .build();
    }
    // 1. Tạo hồ sơ CLB
    @Transactional
    public ClubResponse createClub(CreateClubRequest request) {
        User manager = getCurrentUser();

        // Kiểm tra manager đã có CLB chưa
        if (clubRepository.findByManager(manager).isPresent()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bạn đã là quản lý của một CLB khác");
        }
        // Kiểm tra tên CLB trùng
        if (clubRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên CLB đã tồn tại trong hệ thống");
        }

        // Lấy sân nhà nếu có
        Venue homeVenue = null;
        if (request.getHomeVenueId() != null) {
            homeVenue = venueRepository.findById(request.getHomeVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", "ID", request.getHomeVenueId()));
        }

        Club club = Club.builder()
                .manager(manager)
                .name(request.getName())
                .shortName(request.getShortName())
                .headquarters(request.getHeadquarters())
                .homeVenue(homeVenue)
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .build();

        return toClubResponse(clubRepository.save(club));
    }
    // 2. Xem thông tin CLB
    @Transactional(readOnly = true)
    public ClubResponse getMyClubInfo() {
        Club club = getMyClub();

        // Lấy danh sách thành viên APPROVED
        List<ClubMemberResponse> members = clubMemberRepository
                .findByClubAndJoinStatus(club, JoinStatus.APPROVED)
                .stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());

        // Lấy lịch sử giải đấu
        List<TournamentHistoryResponse> history = registrationRepository
                .findByClub(club)
                .stream()
                .map(reg -> toTournamentHistory(reg, club))
                .collect(Collectors.toList());

        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .shortName(club.getShortName())
                .logoUrl(club.getLogoUrl())
                .headquarters(club.getHeadquarters())
                .homeVenueId(club.getHomeVenue() != null ? club.getHomeVenue().getId() : null)
                .homeVenueName(club.getHomeVenue() != null ? club.getHomeVenue().getName() : null)
                .contactEmail(club.getContactEmail())
                .contactPhone(club.getContactPhone())
                .status(club.getStatus().name())
                .managerName(club.getManager().getFullName())
                .members(members)
                .tournamentHistory(history)
                .build();

    }
    // 3. Cập nhật hồ sơ CLB
    @Transactional
    public ClubResponse updateClub(UpdateClubRequest request) {
        Club club = getMyClub();

        // Kiểm tra tên mới có trùng với CLB khác không
        if (clubRepository.existsByNameAndIdNot(request.getName(), club.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên CLB đã tồn tại trong hệ thống");
        }

        // Lấy sân nhà mới nếu có
        Venue homeVenue = null;
        if (request.getHomeVenueId() != null) {
            homeVenue = venueRepository.findById(request.getHomeVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", "ID", request.getHomeVenueId()));
        }

        club.setName(request.getName());
        club.setShortName(request.getShortName());
        club.setHeadquarters(request.getHeadquarters());
        club.setHomeVenue(homeVenue);
        club.setContactEmail(request.getContactEmail());
        club.setContactPhone(request.getContactPhone());

        return toClubResponse(clubRepository.save(club));
    }
    // 4. Lấy danh sách thành viên theo trạng thái
    public List<ClubMemberResponse> getMembers(JoinStatus joinStatus) {
        Club club = getMyClub();
        return clubMemberRepository.findByClubAndJoinStatus(club, joinStatus)
                .stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }
    // 5. Phê duyệt / Từ chối VĐV vào CLB
    @Transactional
    public ClubMemberResponse approveMember(Long memberId, ApproveMemberRequest request) {
        Club club = getMyClub();

        ClubMember member = clubMemberRepository.findByIdAndClub(memberId, club)
                .orElseThrow(() -> new ResourceNotFoundException("ClubMember", "ID", memberId));

        if (member.getJoinStatus() != JoinStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hồ sơ này đã được xử lý trước đó");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            member.setJoinStatus(JoinStatus.APPROVED);
            member.setJoinedDate(LocalDateTime.now());
        } else {
            if (request.getRejectReason() == null || request.getRejectReason().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp lý do từ chối");
            }
            member.setJoinStatus(JoinStatus.REJECTED);
        }

        return toMemberResponse(clubMemberRepository.save(member));
    }

    // 6. Cập nhật hồ sơ VĐV (số áo, vị trí, thể trạng, vai trò)
    @Transactional
    public ClubMemberResponse updateAthlete(Long memberId, UpdateAthleteRequest request) {
        Club club = getMyClub();

        ClubMember member = clubMemberRepository.findByIdAndClub(memberId, club)
                .orElseThrow(() -> new ResourceNotFoundException("ClubMember", "ID", memberId));

        if (member.getJoinStatus() != JoinStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ cập nhật được VĐV đã được phê duyệt");
        }

        Athlete athlete = member.getAthlete();

        // Kiểm tra số áo trùng nếu muốn đổi
        if (request.getPreferredNumber() != null &&
                !request.getPreferredNumber().equals(athlete.getPreferredNumber())) {
            if (clubMemberRepository.existsJerseyNumberInClub(club, request.getPreferredNumber(), athlete.getId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Số áo " + request.getPreferredNumber() + " đã tồn tại trong CLB");
            }
            athlete.setPreferredNumber(request.getPreferredNumber());
        }

        if (request.getPreferredPosition() != null) {
            athlete.setPreferredPosition(request.getPreferredPosition());
        }

        if (request.getHealthStatus() != null) {
            athlete.setHealthStatus(request.getHealthStatus());
        }

        if (request.getClubRole() != null) {
            member.setClubRole(request.getClubRole());
        }

        athleteRepository.save(athlete);
        return toMemberResponse(clubMemberRepository.save(member));
    }

    // 7. Xóa VĐV khỏi CLB (chấm dứt hợp đồng)
    @Transactional
    public void removeMember(Long memberId) {
        Club club = getMyClub();

        ClubMember member = clubMemberRepository.findByIdAndClub(memberId, club)
                .orElseThrow(() -> new ResourceNotFoundException("ClubMember", "ID", memberId));

        if (member.getJoinStatus() != JoinStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa VĐV đang hoạt động");
        }

        member.setJoinStatus(JoinStatus.REMOVED);
        member.setLeftDate(LocalDateTime.now());
        clubMemberRepository.save(member);
    }


    // 8. Phân công vai trò nội bộ (CAPTAIN / HEAD_COACH)
    @Transactional
    public ClubMemberResponse assignRole(Long memberId, AssignRoleRequest request) {
        Club club = getMyClub();

        ClubMember member = clubMemberRepository.findByIdAndClub(memberId, club)
                .orElseThrow(() -> new ResourceNotFoundException("ClubMember", "ID", memberId));

        if (member.getJoinStatus() != JoinStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ phân công vai trò cho VĐV đã được duyệt");
        }

        ClubRole newRole = request.getClubRole();

        // Nếu phân công CAPTAIN hoặc HEAD_COACH → reset người đang giữ vai trò đó về MEMBER
        if (newRole == ClubRole.CAPTAIN || newRole == ClubRole.HEAD_COACH) {
            clubMemberRepository.findByClubAndClubRoleAndJoinStatus(club, newRole, JoinStatus.APPROVED)
                    .ifPresent(existing -> {
                        existing.setClubRole(ClubRole.MEMBER);
                        clubMemberRepository.save(existing);
                    });
        }

        member.setClubRole(newRole);
        return toMemberResponse(clubMemberRepository.save(member));
    }

    // Lấy danh sách roster hiện tại của CLB trong giải
    @Transactional(readOnly = true)
    public RosterResponse getMyRoster(Long tournamentId) {
        Club club = getMyClub();

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy giải đấu"));

        // Kiểm tra CLB có đăng ký giải này không
        registrationRepository.findByTournamentIdAndClub(tournamentId, club)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "CLB chưa đăng ký giải đấu này"));

        List<RosterResponse.RosterPlayerResponse> players = rosterRepository
                .findByTournamentAndClub(tournament, club)
                .stream()
                .map(r -> RosterResponse.RosterPlayerResponse.builder()
                        .rosterId(r.getId())
                        .athleteId(r.getAthlete().getId())
                        .fullName(r.getAthlete().getUser().getFullName())
                        .jerseyNumber(r.getJerseyNumber())
                        .position(r.getPosition())
                        .role(r.getRole().name())
                        .status(r.getStatus().name())
                        .healthStatus(r.getAthlete().getHealthStatus().name())
                        .build())
                .collect(Collectors.toList());

        return RosterResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .players(players)
                .build();
    }

    // Chốt danh sách thi đấu
    @Transactional
    public RosterResponse submitRoster(Long tournamentId, RosterRequest request) {
        Club club = getMyClub();

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy giải đấu"));

        // Kiểm tra CLB đã APPROVED vào giải chưa
        TournamentRegistration reg = registrationRepository
                .findByTournamentIdAndClub(tournamentId, club)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "CLB chưa đăng ký giải đấu này"));

        if (reg.getStatus() != RegistrationStatus.APPROVED) {
            throw new AppException(HttpStatus.FORBIDDEN, "CLB chưa được duyệt vào giải đấu này");
        }

        // Kiểm tra số lượng VĐV
        int count = request.getPlayers().size();
        if (count < tournament.getMinAthletes()) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Cần tối thiểu " + tournament.getMinAthletes() + " VĐV");
        }
        if (count > tournament.getMaxAthletes()) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Tối đa " + tournament.getMaxAthletes() + " VĐV");
        }

        // Xóa roster cũ (nếu có) rồi tạo mới
        List<TournamentRoster> existing = rosterRepository.findByTournamentAndClub(tournament, club);
        rosterRepository.deleteAll(existing);
        rosterRepository.flush();

        List<TournamentRoster> rosters = request.getPlayers().stream().map(p -> {
            // Kiểm tra VĐV thuộc CLB và đã APPROVED
            ClubMember member = clubMemberRepository
                    .findByClubAndAthlete_IdAndJoinStatus(club, p.getAthleteId(), JoinStatus.APPROVED)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                            "VĐV #" + p.getAthleteId() + " không thuộc CLB hoặc chưa được duyệt"));

            Athlete athlete = member.getAthlete();

            return TournamentRoster.builder()
                    .tournament(tournament)
                    .club(club)
                    .athlete(athlete)
                    .jerseyNumber(p.getJerseyNumber() != null ? p.getJerseyNumber() : athlete.getPreferredNumber())
                    .position(p.getPosition() != null ? p.getPosition() : athlete.getPreferredPosition())
                    .role(p.getRole() != null ? RosterRole.valueOf(p.getRole()) : RosterRole.PLAYER)
                    .status(RosterStatus.ELIGIBLE)
                    .build();
        }).collect(Collectors.toList());

        rosterRepository.saveAll(rosters);

        return getMyRoster(tournamentId);
    }
    // ── Lấy danh sách đăng ký giải đấu của CLB hiện tại ─────────────────────
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations() {
        Club club = getMyClub();
        return registrationRepository.findByClub(club)
                .stream()
                .filter(reg -> {
                    try {
                        reg.getTournament().getName(); // kiểm tra tournament còn tồn tại không
                        return true;
                    } catch (jakarta.persistence.EntityNotFoundException e) {
                        return false;
                    }
                })
                .map(reg -> RegistrationResponse.builder()
                        .id(reg.getId())
                        .tournamentId(reg.getTournament().getId())
                        .tournamentName(reg.getTournament().getName())
                        .clubId(club.getId())
                        .status(reg.getStatus().name())
                        .homeKitColor(reg.getHomeKitColor())
                        .awayKitColor(reg.getAwayKitColor())
                        .appliedAt(reg.getAppliedAt() != null ? reg.getAppliedAt().toString() : null)
                        .reviewedAt(reg.getReviewedAt() != null ? reg.getReviewedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Trả về map { athleteId → tên giải đang tham gia } cho các VĐV của CLB
     * đã bị khóa ở giải khác (không tính giải tournamentId đang đăng ký).
     * tournamentId = 0 nghĩa là đang đăng ký mới, chưa có id → lấy tất cả giải hiện có.
     */
    @Transactional(readOnly = true)
    public Map<Long, String> getLockedAthletes(Long tournamentId) {
        Club club = getMyClub();
        return rosterRepository
                .findByClubExcludingTournament(club, tournamentId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getAthlete().getId(),
                        r -> r.getTournament().getName(),
                        (a, b) -> a // nếu VĐV có trong nhiều giải, lấy giải đầu tiên
                ));
    }
}