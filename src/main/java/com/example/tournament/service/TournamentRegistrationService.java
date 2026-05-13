package com.example.tournament.service;

import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentRegistration;
import com.example.tournament.entity.TournamentRoster;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.payload.response.Tournament.OrganizerRosterResponse;
import com.example.tournament.payload.response.Tournament.RegistrationDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentRegistrationResponse;
import com.example.tournament.repository.TournamentRegistrationRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRosterRepository rosterRepository;

    private final TournamentRepository tournamentRepository;
    // Inject thêm TournamentRepository nếu bạn muốn kiểm tra giải đấu có tồn tại không trước khi query

    public Page<TournamentRegistrationResponse> getRegistrationsByTournament(
            Long tournamentId,
            RegistrationStatus status,
            Pageable pageable) {

        // 1. Kiểm tra giải đấu có tồn tại không
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu với ID: " + tournamentId));

        // ✨ 2. KIỂM TRA ĐÚNG YÊU CẦU NGHIỆP VỤ: GIẢI ĐẤU PHẢI ĐANG MỞ ĐĂNG KÝ
        // (Trung thay thế TournamentStatus.OPEN bằng Enum thực tế trong project của bạn nhé, ví dụ REGISTRATION_OPEN)
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new RuntimeException("Giải đấu hiện không ở trạng thái mở đăng ký. Không thể lấy danh sách!");
        }

        Page<TournamentRegistration> registrations;

        // Nếu có truyền status lên thì lọc theo status, không thì lấy tất cả
        if (status != null) {
            registrations = registrationRepository.findByTournamentIdAndStatus(tournamentId, status, pageable);
        } else {
            registrations = registrationRepository.findByTournamentId(tournamentId, pageable);
        }

        // Chuyển đổi Entity sang Response DTO
        return registrations.map(this::mapToResponse);
    }

    private TournamentRegistrationResponse mapToResponse(TournamentRegistration entity) {
        return TournamentRegistrationResponse.builder()
                .id(entity.getId())
                .clubId(entity.getClub().getId())
                .clubName(entity.getClub().getName()) // Thay đổi theo cấu trúc thực tế của Entity Club
                .status(entity.getStatus())
                .rejectReason(entity.getRejectReason())
                .financialProofUrl(entity.getFinancialProofUrl())
                .homeKitColor(entity.getHomeKitColor())
                .awayKitColor(entity.getAwayKitColor())
                .appliedAt(entity.getAppliedAt())
                .reviewedAt(entity.getReviewedAt())
                .build();
    }


    public RegistrationDetailResponse getRegistrationDetail(Long tournamentId, Long regId) {
        // 1. Truy vấn dữ liệu, ném lỗi nếu không tồn tại hoặc ID không khớp
        TournamentRegistration registration = registrationRepository.findByIdAndTournamentId(regId, tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký hợp lệ."));

        Long clubId = registration.getClub().getId();

        // 2. Truy vấn danh sách VĐV của CLB này trong giải đấu
        List<TournamentRoster> rosters = rosterRepository.findByTournamentIdAndClubId(tournamentId, clubId);

        // 3. Mapping danh sách Roster sang DTO
        List<OrganizerRosterResponse> rosterResponses = rosters.stream().map(roster ->
                OrganizerRosterResponse.builder()
                        .id(roster.getId())
                        .athleteId(roster.getAthlete().getId())
                        .athleteName(roster.getAthlete().getIdentityNumber())
                        .avatarUrl(roster.getAthlete().getPortraitUrl())
                        .jerseyNumber(roster.getJerseyNumber())
                        .position(roster.getPosition())
                        .role(roster.getRole().name())
                        .status(roster.getStatus().name())
                        .build()
        ).collect(Collectors.toList());

        // 4. Trả về DTO tổng hợp
        return RegistrationDetailResponse.builder()
                .id(registration.getId())
                .status(registration.getStatus().name())
                .rejectReason(registration.getRejectReason())
                .financialProofUrl(registration.getFinancialProofUrl())
                .homeKitColor(registration.getHomeKitColor())
                .awayKitColor(registration.getAwayKitColor())
                .appliedAt(registration.getAppliedAt())
                .reviewedAt(registration.getReviewedAt())

                // --- Thông tin Câu lạc bộ ---
                .clubId(registration.getClub().getId())
                .clubName(registration.getClub().getName())
                .clubLogo(registration.getClub().getLogoUrl())
                .repName(registration.getClub().getName())
                .phone(registration.getClub().getContactPhone())
                .email(registration.getClub().getContactEmail())
                .roster(rosterResponses)
                .build();


    }
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu
    public void approveRegistration(Long tournamentId, Long regId) {
        // 1. Tìm đơn đăng ký
        TournamentRegistration registration = registrationRepository.findByIdAndTournamentId(regId, tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký hợp lệ."));

        // 2. Kiểm tra trạng thái (Chỉ duyệt đơn đang PENDING)
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt đơn đăng ký đang ở trạng thái CHỜ DUYỆT.");
        }

        Tournament tournament = registration.getTournament();
        Long clubId = registration.getClub().getId();

        // 3. Kiểm tra số lượng thành viên (Business Logic)
        long currentAthletesCount = rosterRepository.countByTournamentIdAndClubId(tournamentId, clubId);

        int min = tournament.getMinAthletes() != null ? tournament.getMinAthletes() : 0;
        int max = tournament.getMaxAthletes() != null ? tournament.getMaxAthletes() : 999;

        if (currentAthletesCount < min || currentAthletesCount > max) {
            throw new RuntimeException("Không thể duyệt! Số lượng VĐV hiện tại là " + currentAthletesCount +
                    ", nhưng giải đấu yêu cầu từ " + min + " đến " + max + " VĐV.");
        }

        // 4. Lấy thông tin người đang thao tác (Admin/Organizer) từ Spring Security
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // 5. Cập nhật trạng thái và Lưu lịch sử duyệt
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setReviewedBy(currentUser); // Lưu vết người duyệt

        // Xóa lý do từ chối (nếu trước đó đã từng từ chối rồi nộp lại)
        registration.setRejectReason(null);

        registrationRepository.save(registration);

        // (Tùy chọn) Nếu bạn có bảng History riêng, bạn có thể insert thêm vào đây
        // historyRepository.save(new RegistrationHistory(regId, "APPROVED", currentUser, LocalDateTime.now()));
    }

    @Transactional
    public void rejectRegistration(Long tournamentId, Long regId, String rawReason) {
        // 1. Tìm đơn đăng ký
        TournamentRegistration registration = registrationRepository.findByIdAndTournamentId(regId, tournamentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký hợp lệ."));

        // 2. Kiểm tra trạng thái (Chỉ từ chối đơn đang PENDING)
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối đơn đăng ký đang ở trạng thái CHỜ DUYỆT.");
        }

        // 3. Thu thập các "mảnh ghép" thông tin
        // [A] Tên Admin đang thao tác
        String adminName = SecurityContextHolder.getContext().getAuthentication().getName();

        // [B] Tên Câu lạc bộ bị từ chối
        String clubName = registration.getClub().getName();

        // [C] Lý do thô (rawReason) đã được truyền vào hàm

        // [Thời gian] Format thời gian hiện tại cho đẹp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedTime = LocalDateTime.now().format(formatter);

        // 4. Lắp ráp chuỗi Log theo đúng Format
        String fullRejectReason = String.format("Admin %s đã từ chối CLB %s vì lý do: %s vào lúc %s",
                adminName, clubName, rawReason, formattedTime);

        // 5. Cập nhật Entity và Lưu DB
        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setRejectReason(fullRejectReason);
        registration.setReviewedAt(LocalDateTime.now());
        // Nếu ở bước duyệt bạn có thêm trường reviewedBy, thì ở đây cũng nên set vào:
        // registration.setReviewedBy(adminName);

        registrationRepository.save(registration);
    }
}
