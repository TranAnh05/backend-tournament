package com.example.tournament.service;

import com.example.tournament.entity.TournamentRegistration;
import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.payload.response.Tournament.TournamentRegistrationResponse;
import com.example.tournament.repository.TournamentRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
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
}
