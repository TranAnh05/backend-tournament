package com.example.tournament.service;

import com.example.tournament.entity.Athlete;
import com.example.tournament.entity.Club;
import com.example.tournament.entity.ClubMember;
import com.example.tournament.entity.User;
import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.CommonStatus;
import com.example.tournament.enums.JoinStatus;
import com.example.tournament.payload.request.athlete.ApplyToClubRequest;
import com.example.tournament.payload.response.athlete.*;
import com.example.tournament.repository.AthleteRepository;
import com.example.tournament.repository.ClubMemberRepository;
import com.example.tournament.repository.ClubRepository;
import com.example.tournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AthleteService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;

    // ── Helper: lấy User đang đăng nhập ─────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    // ── Helper: lấy Athlete từ User đang đăng nhập ──────────────────────────
    private Athlete getCurrentAthlete() {
        User user = getCurrentUser();
        return athleteRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Hồ sơ vận động viên không tồn tại"));
    }

    // ── Helper: map Club → ClubPublicResponse ───────────────────────────────
    private ClubPublicResponse toPublicResponse(Club club) {
        long totalMembers = clubMemberRepository.countByClubAndJoinStatus(club, JoinStatus.APPROVED);
        return ClubPublicResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .shortName(club.getShortName())
                .logoUrl(club.getLogoUrl())
                .headquarters(club.getHeadquarters())
                .contactEmail(club.getContactEmail())
                .contactPhone(club.getContactPhone())
                .status(club.getStatus().name())
                .managerName(club.getManager().getFullName())
                .homeVenueName(club.getHomeVenue() != null ? club.getHomeVenue().getName() : null)
                .totalMembers((int) totalMembers)
                .build();
    }

    // ── 1. Danh sách tất cả CLB đang ACTIVE ─────────────────────────────────
    public List<ClubPublicResponse> getAllActiveClubs() {
        return clubRepository.findAll().stream()
                .filter(c -> c.getStatus() == CommonStatus.ACTIVE)
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    // ── 2. Chi tiết một CLB + danh sách thành viên công khai ────────────────
    public ClubPublicDetailResponse getClubDetail(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CLB với ID: " + clubId));

        List<ClubMemberPublicResponse> members = clubMemberRepository
                .findByClubAndJoinStatus(club, JoinStatus.APPROVED)
                .stream()
                .map(cm -> ClubMemberPublicResponse.builder()
                        .athleteId(cm.getAthlete().getId())
                        .fullName(cm.getAthlete().getUser().getFullName())
                        .preferredNumber(cm.getAthlete().getPreferredNumber())
                        .preferredPosition(cm.getAthlete().getPreferredPosition())
                        .healthStatus(cm.getAthlete().getHealthStatus().name())
                        .clubRole(cm.getClubRole().name())
                        .build())
                .collect(Collectors.toList());

        return ClubPublicDetailResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .shortName(club.getShortName())
                .logoUrl(club.getLogoUrl())
                .headquarters(club.getHeadquarters())
                .contactEmail(club.getContactEmail())
                .contactPhone(club.getContactPhone())
                .status(club.getStatus().name())
                .managerName(club.getManager().getFullName())
                .homeVenueName(club.getHomeVenue() != null ? club.getHomeVenue().getName() : null)
                .totalMembers(members.size())
                .members(members)
                .build();
    }

    // ── 3. VĐV nộp đơn ứng tuyển vào CLB ───────────────────────────────────
    @Transactional
    public AthleteApplicationResponse applyToClub(ApplyToClubRequest request) {
        Athlete athlete = getCurrentAthlete();

        // Kiểm tra đã có đơn PENDING hoặc APPROVED tại bất kỳ CLB nào chưa
        boolean hasActiveApplication = clubMemberRepository
                .findFirstByAthleteAndJoinStatusIn(athlete, List.of(JoinStatus.PENDING, JoinStatus.APPROVED))
                .isPresent();
        if (hasActiveApplication) {
            throw new RuntimeException("Bạn đã có đơn đang chờ duyệt hoặc đã là thành viên của một CLB. Vui lòng rời CLB hiện tại trước khi ứng tuyển.");
        }

        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CLB"));

        if (club.getStatus() != CommonStatus.ACTIVE) {
            throw new RuntimeException("CLB này hiện không nhận thành viên mới");
        }

        ClubMember newMember = ClubMember.builder()
                .club(club)
                .athlete(athlete)
                .joinStatus(JoinStatus.PENDING)
                .clubRole(ClubRole.MEMBER)
                .build();

        ClubMember saved = clubMemberRepository.save(newMember);

        return AthleteApplicationResponse.builder()
                .memberId(saved.getId())
                .clubId(club.getId())
                .clubName(club.getName())
                .clubShortName(club.getShortName())
                .joinStatus(saved.getJoinStatus().name())
                .clubRole(saved.getClubRole().name())
                .appliedAt(saved.getCreatedAt())
                .joinedDate(saved.getJoinedDate())
                .leftDate(saved.getLeftDate())
                .build();
    }

    // ── 4. Lịch sử ứng tuyển của VĐV đang đăng nhập ─────────────────────────
    public List<AthleteApplicationResponse> getMyApplications() {
        Athlete athlete = getCurrentAthlete();
        return clubMemberRepository.findByAthleteOrderByCreatedAtDesc(athlete)
                .stream()
                .map(cm -> AthleteApplicationResponse.builder()
                        .memberId(cm.getId())
                        .clubId(cm.getClub().getId())
                        .clubName(cm.getClub().getName())
                        .clubShortName(cm.getClub().getShortName())
                        .joinStatus(cm.getJoinStatus().name())
                        .clubRole(cm.getClubRole().name())
                        .appliedAt(cm.getCreatedAt())
                        .joinedDate(cm.getJoinedDate())
                        .leftDate(cm.getLeftDate())
                        .build())
                .collect(Collectors.toList());
    }
}