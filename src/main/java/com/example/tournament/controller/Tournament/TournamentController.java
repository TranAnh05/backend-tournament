package com.example.tournament.controller.Tournament;

import com.example.tournament.enums.RoleCode;
import com.example.tournament.payload.request.Tournament.TournamentRequest;
import com.example.tournament.payload.request.club.SubmitRosterRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.Tournament.TournamentDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentResponse;
import com.example.tournament.payload.response.Tournament.TournamentSelectResponse;
import com.example.tournament.payload.response.admin.SportResponse;
import com.example.tournament.payload.response.admin.VenueResponse;
import com.example.tournament.payload.response.club.DisciplineResponse;
import com.example.tournament.payload.response.club.RegistrationResponse;
import com.example.tournament.security.jwt.JwtTokenProvider;
import com.example.tournament.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.tournament.payload.request.club.RegisterTournamentRequest;


import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping
    public ResponseEntity<Page<TournamentResponse>> getTournaments(
            Authentication authentication,
            @RequestParam(required = false) String name,
            Pageable pageable) {

        // Sử dụng hàm bạn vừa viết trong JwtTokenProvider
        RoleCode role = jwtTokenProvider.getRoleFromAuthentication(authentication);

        return ResponseEntity.ok(tournamentService.getAllTournaments(pageable,role, name));
    }

    @GetMapping("/{id}")
    public ApiResponse<TournamentDetailResponse> getTournamentById(@PathVariable Long id) {
        return ApiResponse.<TournamentDetailResponse>builder()
                .code(200)
                .message("Lấy chi tiết giải đấu thành công")
                .result(tournamentService.getTournamentById(id))
                .build();
    }

    @GetMapping("/registrations/my")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations() {
        return ResponseEntity.ok(
                ApiResponse.<List<RegistrationResponse>>builder()
                        .code(200)
                        .message("Lay danh sach dang ky thanh cong")
                        .result(tournamentService.getMyRegistrations())
                        .build()
        );
    }

    // GET /clubs/me/disciplines
    @GetMapping("/disciplines/my")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<DisciplineResponse>>> getMyDisciplines() {
        return ResponseEntity.ok(
                ApiResponse.<List<DisciplineResponse>>builder()
                        .code(200)
                        .message("Lay danh sach ky luat thanh cong")
                        .result(tournamentService.getMyDisciplines())
                        .build()
        );
    }

    @GetMapping("/sports/all")
    public ResponseEntity<ApiResponse<List<SportResponse>>> getAllSports() {
        List<SportResponse> sports = tournamentService.getAllSportsForSelect();
        return ResponseEntity.ok(
                ApiResponse.<List<SportResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách môn thi đấu thành công")
                        .result(sports)
                        .build()
        );
    }

    @GetMapping("/venues/all")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenue(){

       List<VenueResponse> venues = tournamentService.getAllVenuesForSelect();
        return ResponseEntity.ok(
                ApiResponse.<List<VenueResponse>>builder()
                        .code(200)
                        .message("lấy danh sách thành công")
                        .result(venues)
                        .build()
        );
    }

    @PostMapping
    // Chỉ Ban tổ chức mới có quyền tạo
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TournamentDetailResponse>> createTournament(
            @Valid @RequestBody TournamentRequest request) {

        // Gọi service xử lý logic tạo mới
        TournamentDetailResponse response = tournamentService.createTournament(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TournamentDetailResponse>builder()
                        .code(200)
                        .message("Tạo giải đấu thành công")
                        .result(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TournamentDetailResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody TournamentRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<TournamentDetailResponse>builder()
                        .result(tournamentService.updateTournament(id, request))
                        .build()
        );
    }


    //Club
    // Đăng ký giải đấu
    @PostMapping("/{tournamentId}/register")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @PathVariable Long tournamentId,
            @RequestBody RegisterTournamentRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<RegistrationResponse>builder()
                        .code(200)
                        .message("Đăng ký giải đấu thành công")
                        .result(tournamentService.registerTournament(
                                tournamentId,
                                request.getHomeKitColor(),
                                request.getAwayKitColor(),
                                request.getFinancialProofUrl()))
                        .build()
        );
    }
    // Rút đơn khỏi giải đấu
    @DeleteMapping("/{tournamentId}/withdraw")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable Long tournamentId) {
        tournamentService.withdrawTournament(tournamentId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Rút đơn thành công")
                        .build()
        );
    }
    //kiet them phan nay
    @PostMapping("/{tournamentId}/roster")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> submitRoster(
            @PathVariable Long tournamentId,
            @RequestBody SubmitRosterRequest request) {
        tournamentService.submitRoster(tournamentId, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200).message("Nộp danh sách thi đấu thành công").build());
    }

    @GetMapping("/{tournamentId}/roster/status")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<Boolean>> getRosterStatus(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(200).message("OK")
                .result(tournamentService.hasRoster(tournamentId)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTournament(@PathVariable Long id) {

        tournamentService.deleteTournament(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Xóa giải đấu thành công")
                        .build()
        );
    }

    @PatchMapping("/{id}/registration")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TournamentDetailResponse>> toggleRegistration(
            @PathVariable Long id) {

        TournamentDetailResponse response = tournamentService.toggleRegistrationStatus(id);

        String message = response.getStatus().equals("REGISTRATION_OPEN") ? "Đã mở cổng đăng ký" : "Đã đóng cổng đăng ký";

        return ResponseEntity.ok(
                ApiResponse.<TournamentDetailResponse>builder()
                        .code(200)
                        .message(message)
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TournamentDetailResponse>> startTournament(@PathVariable Long id) {

        TournamentDetailResponse response = tournamentService.startTournament(id);

        return ResponseEntity.ok(
                ApiResponse.<TournamentDetailResponse>builder()
                        .code(200)
                        .message("Giải đấu đã chính thức bắt đầu!")
                        .result(response)
                        .build()
        );
    }

    /**
     * API Kết thúc giải đấu
     */
    @PatchMapping("/{id}/finish")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TournamentDetailResponse>> finishTournament(@PathVariable Long id) {

        TournamentDetailResponse response = tournamentService.finishTournament(id);

        return ResponseEntity.ok(
                ApiResponse.<TournamentDetailResponse>builder()
                        .code(200)
                        .message("Giải đấu đã kết thúc thành công!")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/opening")
    public ResponseEntity<ApiResponse<List<TournamentSelectResponse>>> getOpeningTournaments() {
        return ResponseEntity.ok(
                ApiResponse.<List<TournamentSelectResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách giải đấu đang mở đăng ký thành công")
                        .result(tournamentService.getOpeningTournaments())
                        .build()
        );
    }


}
