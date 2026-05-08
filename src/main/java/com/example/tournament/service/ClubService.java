//clb
package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.JoinStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.exception.custom.ResourceNotFoundException;
import com.example.tournament.payload.request.club.*;
import com.example.tournament.payload.response.club.ClubMemberResponse;
import com.example.tournament.payload.response.club.ClubResponse;
import com.example.tournament.repository.*;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .status(club.getStatus())
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
    public ClubResponse getMyClubInfo() {
        return toClubResponse(getMyClub());
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
}