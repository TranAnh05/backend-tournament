package com.example.tournament.controller.athlete;

import com.example.tournament.payload.request.athlete.ApplyToClubRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.athlete.*;
import com.example.tournament.service.AthleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/athlete")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    // ── PUBLIC: Lấy danh sách tất cả CLB đang ACTIVE ────────────────────────
    // GET /api/v1/athlete/clubs
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<ClubPublicResponse>>> getAllClubs() {
        return ResponseEntity.ok(
                ApiResponse.<List<ClubPublicResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách CLB thành công")
                        .result(athleteService.getAllActiveClubs())
                        .build()
        );
    }

    // ── PUBLIC: Xem chi tiết một CLB + danh sách thành viên ─────────────────
    // GET /api/v1/athlete/clubs/{clubId}
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubPublicDetailResponse>> getClubDetail(
            @PathVariable Long clubId) {
        return ResponseEntity.ok(
                ApiResponse.<ClubPublicDetailResponse>builder()
                        .code(200)
                        .message("Lấy chi tiết CLB thành công")
                        .result(athleteService.getClubDetail(clubId))
                        .build()
        );
    }

    // ── ATHLETE: Nộp đơn ứng tuyển vào CLB ──────────────────────────────────
    // POST /api/v1/athlete/clubs/apply
    @PostMapping("/clubs/apply")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<ApiResponse<AthleteApplicationResponse>> applyToClub(
            @Valid @RequestBody ApplyToClubRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AthleteApplicationResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Nộp đơn ứng tuyển thành công! Đang chờ CLB phê duyệt.")
                        .result(athleteService.applyToClub(request))
                        .build()
        );
    }

    // ── ATHLETE: Lịch sử ứng tuyển của VĐV đang đăng nhập ───────────────────
    // GET /api/v1/athlete/my-applications
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<ApiResponse<List<AthleteApplicationResponse>>> getMyApplications() {
        return ResponseEntity.ok(
                ApiResponse.<List<AthleteApplicationResponse>>builder()
                        .code(200)
                        .message("Lấy lịch sử ứng tuyển thành công")
                        .result(athleteService.getMyApplications())
                        .build()
        );
    }
}