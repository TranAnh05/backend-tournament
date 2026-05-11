package com.example.tournament.service;

import com.example.tournament.entity.TournamentRegistration;
import com.example.tournament.entity.TournamentRoster;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.payload.response.Tournament.OrganizerRosterResponse;
import com.example.tournament.payload.response.Tournament.RegistrationDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentRegistrationResponse;
import com.example.tournament.repository.TournamentRegistrationRepository;
import com.example.tournament.repository.TournamentRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRosterRepository rosterRepository;
    // Inject thêm TournamentRepository nếu bạn muốn kiểm tra giải đấu có tồn tại không trước khi query

    public Page<TournamentRegistrationResponse> getRegistrationsByTournament(
            Long tournamentId,
            RegistrationStatus status,
            Pageable pageable) {

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
}
