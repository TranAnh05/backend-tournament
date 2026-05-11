package com.example.tournament.controller.Tournament;


import com.example.tournament.enums.RegistrationStatus;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.Tournament.TournamentRegistrationResponse;
import com.example.tournament.service.TournamentRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tournaments/{tournamentId}/registrations")
@RequiredArgsConstructor
public class TournamentRegistrationController {

    private final TournamentRegistrationService registrationService;

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER')") // Bảo mật: Chỉ Ban tổ chức mới được xem
    public ResponseEntity<ApiResponse<Page<TournamentRegistrationResponse>>> getRegistrations(
            @PathVariable Long tournamentId,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TournamentRegistrationResponse> result = registrationService.getRegistrationsByTournament(tournamentId, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<TournamentRegistrationResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách đội đăng ký thành công")
                        .result(result)
                        .build()
        );
    }
}
