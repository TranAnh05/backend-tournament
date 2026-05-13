package com.example.tournament.controller.athlete;

import com.example.tournament.payload.request.athlete.ApplyToClubRequest;
import com.example.tournament.payload.request.athlete.AthleteRegisterRequest;       // ← THÊM
import com.example.tournament.payload.request.athlete.UpdateAthleteProfileRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.athlete.*;
import com.example.tournament.service.AthleteRegistrationService;                   // ← THÊM
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
    private final AthleteRegistrationService athleteRegistrationService;            // ← THÊM

    // ── PUBLIC: Đăng ký tài khoản VĐV ───────────────────────────────────────
    // POST /api/v1/athlete/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody AthleteRegisterRequest request) {
        athleteRegistrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Đăng ký tài khoản VĐV thành công")
                        .build()
        );
    }

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

    // ── ATHLETE: Lịch sử ứng tuyển ──────────────────────────────────────────
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

    // ── ATHLETE: Lấy hồ sơ cá nhân ──────────────────────────────────────────
    // GET /api/v1/athlete/my-profile
    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<ApiResponse<AthleteProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                ApiResponse.<AthleteProfileResponse>builder()
                        .code(200)
                        .message("Lấy hồ sơ thành công")
                        .result(athleteService.getMyProfile())
                        .build()
        );
    }

    // ── ATHLETE: Cập nhật hồ sơ cá nhân ─────────────────────────────────────
    // PUT /api/v1/athlete/my-profile
    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<ApiResponse<AthleteProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateAthleteProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<AthleteProfileResponse>builder()
                        .code(200)
                        .message("Cập nhật hồ sơ thành công")
                        .result(athleteService.updateMyProfile(request))
                        .build()
        );
    }
}