package com.example.tournament.service;

import com.example.tournament.entity.*;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.UserStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.admin.AdminUserStatusUpdateRequest;
import com.example.tournament.payload.response.admin.*;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.UserRepository;
import com.example.tournament.repository.UserStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrganizerService {
    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final UserStatusLogRepository userStatusLogRepository;
    private final ObjectMapper objectMapper;

    public PageResponse<AdminOrganizerResponse> getOrganizers(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepository.searchOrganizers(search, status, pageable);

        // Map Entity sang DTO
        List<AdminOrganizerResponse> content = userPage.getContent().stream()
                .map(this::mapToOrganizerResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminOrganizerResponse>builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .content(content)
                .build();
    }

    private AdminOrganizerResponse mapToOrganizerResponse(User user) {
        return AdminOrganizerResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().toString() : "ACTIVE")
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminOrganizerDetailResponse getOrganizerDetails(Long id) {
        // Tìm và Validate User
        User user = userRepository.findOrganizerById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy Ban tổ chức hoặc tài khoản không hợp lệ"));

        // Xử lý Hồ sơ mở rộng
        UserProfile profile = user.getUserProfile();
        String bio = null;
        List<AdminAchievementDto> achievementList = new ArrayList<>();

        if (profile != null) {
            bio = profile.getBio();

            if (profile.getAchievements() != null) {
                achievementList = profile.getAchievements().stream()
                        .map(map -> objectMapper.convertValue(map, AdminAchievementDto.class))
                        .collect(Collectors.toList());
            }
        }

        // Lấy ngày được cấp quyền từ bảng UserRole
        var assignedAt = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getRoleCode() == RoleCode.ORGANIZER)
                .map(UserRole::getAssignedAt)
                .findFirst()
                .orElse(user.getCreatedAt());

        // Lấy số liệu Thống kê & Hoạt động
        long totalTournaments = tournamentRepository.countByOrganizerId(id);
        long totalClubs = tournamentRepository.countDistinctClubsByOrganizerId(id);

        List<Tournament> recentTournaments = tournamentRepository.findTop5ByOrganizerIdOrderByCreatedAtDesc(id);
        List<AdminTournamentShortResponse> recentTournamentDtos = recentTournaments.stream()
                .map(t -> AdminTournamentShortResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .status(t.getStatus().name())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .build())
                .collect(Collectors.toList());

        return AdminOrganizerDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .createdAt(user.getCreatedAt())
                .assignedAt(assignedAt)
                .bio(bio)
                .achievements(achievementList)
                .totalTournaments(totalTournaments)
                .totalParticipatingClubs(totalClubs)
                .recentTournaments(recentTournamentDtos)
                .build();
    }

    @Transactional
    public AdminUserStatusUpdateResponse updateOrganizerStatus(Long targetUserId, AdminUserStatusUpdateRequest request, Long adminId) {
        User targetUser = userRepository.findOrganizerById(targetUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy Ban tổ chức hoặc tài khoản không hợp lệ"));

        User adminUser = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "Không xác định được danh tính Quản trị viên"));

        UserStatus oldStatus = targetUser.getStatus();
        UserStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái mới không có sự thay đổi");
        }

        // XU LY TUY BIEN LY DO
        String finalReason = request.getReason();

        if (newStatus != UserStatus.ACTIVE) {
            // TH1: KHi khoa - bat buoc co ly do
            if (finalReason == null || finalReason.trim().isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập lý do khóa tài khoản");
            }

            if (oldStatus == UserStatus.ACTIVE) {
                boolean hasActiveTournaments = tournamentRepository.hasActiveTournaments(targetUserId);
                if (hasActiveTournaments) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "Không thể khóa tài khoản này. BTC đang quản lý các giải đấu đang chạy hoặc sắp mở. " +
                                    "Vui lòng chuyển quyền quản lý giải đấu trước khi khóa.");
                }
            }
        } else {
            // TH2: Mo khoa - Nhap hoac khong nhap ly do
            if (finalReason == null || finalReason.trim().isEmpty()) {
                finalReason = "Khôi phục trạng thái hoạt động bình thường";
            }
        }

        targetUser.setStatus(newStatus);
        userRepository.save(targetUser);

        UserStatusLog statusLog = UserStatusLog.builder()
                .user(targetUser)
                .changedBy(adminUser)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(finalReason)
                .build();
        userStatusLogRepository.save(statusLog);

        // 6. Đóng gói trả về
        return AdminUserStatusUpdateResponse.builder()
                .userId(targetUser.getId())
                .fullName(targetUser.getFullName())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .reason(statusLog.getReason())
                .changedByAdmin(adminUser.getFullName())
                .changedAt(statusLog.getCreatedAt() != null ? statusLog.getCreatedAt() : LocalDateTime.now())
                .build();
    }

}
