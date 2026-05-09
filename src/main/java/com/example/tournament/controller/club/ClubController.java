package com.example.tournament.controller.club;

import com.example.tournament.enums.JoinStatus;
import com.example.tournament.payload.request.club.*;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.club.ClubMemberResponse;
import com.example.tournament.payload.response.club.ClubResponse;
import com.example.tournament.payload.response.club.RosterResponse;
import com.example.tournament.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.tournament.payload.response.club.DisciplineResponse;
import com.example.tournament.service.TournamentService;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLUB_MANAGER')") // ROLE_ prefix được tự động thêm bởi Spring
public class ClubController {

    private final ClubService clubService;
    private final TournamentService tournamentService;

    // Tạo hồ sơ CLB
    @PostMapping
    public ResponseEntity<ApiResponse<ClubResponse>> createClub(
            @Valid @RequestBody CreateClubRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ClubResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Tạo hồ sơ CLB thành công")
                        .result(clubService.createClub(request))
                        .build()
        );
    }

    //Xem thông tin CLB
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ClubResponse>> getMyClub() {

        return ResponseEntity.ok(
                ApiResponse.<ClubResponse>builder()
                        .code(200)
                        .message("Lấy thông tin CLB thành công")
                        .result(clubService.getMyClubInfo())
                        .build()
        );
    }

    //Cập nhật hồ sơ CLB
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ClubResponse>> updateClub(
            @Valid @RequestBody UpdateClubRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<ClubResponse>builder()
                        .code(200)
                        .message("Cập nhật hồ sơ CLB thành công")
                        .result(clubService.updateClub(request))
                        .build()
        );
    }

    //Danh sách thành viên
    @GetMapping("/me/members")
    public ResponseEntity<ApiResponse<List<ClubMemberResponse>>> getMembers(
            @RequestParam(defaultValue = "APPROVED") JoinStatus status) {

        return ResponseEntity.ok(
                ApiResponse.<List<ClubMemberResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách thành viên thành công")
                        .result(clubService.getMembers(status))
                        .build()
        );
    }

    //Phê duyệt VĐV
    @PatchMapping("/me/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<ClubMemberResponse>> approveMember(
            @PathVariable Long memberId,
            @Valid @RequestBody ApproveMemberRequest request) {

        ClubMemberResponse result = clubService.approveMember(memberId, request);
        boolean isApproved = result.getJoinStatus() == JoinStatus.APPROVED;

        return ResponseEntity.ok(
                ApiResponse.<ClubMemberResponse>builder()
                        .code(200)
                        .message(isApproved ? "Phê duyệt VĐV thành công" : "Đã từ chối hồ sơ VĐV")
                        .result(result)
                        .build()
        );
    }

    // Cập nhật hồ sơ VĐV
    @PutMapping("/me/members/{memberId}")
    public ResponseEntity<ApiResponse<ClubMemberResponse>> updateAthlete(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateAthleteRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<ClubMemberResponse>builder()
                        .code(200)
                        .message("Cập nhật thông tin VĐV thành công")
                        .result(clubService.updateAthlete(memberId, request))
                        .build()
        );
    }

    //Xóa VĐV khỏi CLB
    @DeleteMapping("/me/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long memberId) {

        clubService.removeMember(memberId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Đã xóa VĐV khỏi CLB")
                        .build()
        );
    }

    //Phân công vai trò
    @PatchMapping("/me/members/{memberId}/role")
    public ResponseEntity<ApiResponse<ClubMemberResponse>> assignRole(
            @PathVariable Long memberId,
            @Valid @RequestBody AssignRoleRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<ClubMemberResponse>builder()
                        .code(200)
                        .message("Phân công vai trò thành công")
                        .result(clubService.assignRole(memberId, request))
                        .build()
        );
    }
    @GetMapping("/me/disciplines")
    public ResponseEntity<ApiResponse<List<DisciplineResponse>>> getMyDisciplines() {
        return ResponseEntity.ok(
                ApiResponse.<List<DisciplineResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách kỷ luật thành công")
                        .result(tournamentService.getMyDisciplines())
                        .build()
        );
    }

    // Lấy roster hiện tại
    @GetMapping("/tournaments/{tournamentId}/roster")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<RosterResponse>> getMyRoster(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(ApiResponse.<RosterResponse>builder()
                .code(200)
                .message("Lấy danh sách thi đấu thành công")
                .result(clubService.getMyRoster(tournamentId))
                .build());
    }

    // Chốt danh sách thi đấu
    @PostMapping("/tournaments/{tournamentId}/roster")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<RosterResponse>> submitRoster(
            @PathVariable Long tournamentId,
            @RequestBody RosterRequest request) {
        return ResponseEntity.ok(ApiResponse.<RosterResponse>builder()
                .code(200)
                .message("Chốt danh sách thi đấu thành công")
                .result(clubService.submitRoster(tournamentId, request))
                .build());
    }
}