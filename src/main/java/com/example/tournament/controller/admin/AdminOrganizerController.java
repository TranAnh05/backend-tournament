package com.example.tournament.controller.admin;

import com.example.tournament.enums.UserStatus;
import com.example.tournament.payload.request.admin.AdminUserStatusUpdateRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.admin.AdminOrganizerDetailResponse;
import com.example.tournament.payload.response.admin.AdminOrganizerResponse;
import com.example.tournament.payload.response.admin.AdminUserStatusUpdateResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.security.userdetail.CustomUserDetails;
import com.example.tournament.service.AdminOrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/organizers")
@RequiredArgsConstructor
public class AdminOrganizerController {

    private final AdminOrganizerService organizerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminOrganizerResponse>>> getOrganizers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<AdminOrganizerResponse> pageResponse = organizerService.getOrganizers(search, status, page, size);

        ApiResponse<PageResponse<AdminOrganizerResponse>> response = ApiResponse.<PageResponse<AdminOrganizerResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách Ban tổ chức thành công")
                .result(pageResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminOrganizerDetailResponse>> getOrganizerDetails(@PathVariable Long id) {

        AdminOrganizerDetailResponse detailResponse = organizerService.getOrganizerDetails(id);

        ApiResponse<AdminOrganizerDetailResponse> response = ApiResponse.<AdminOrganizerDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết Ban tổ chức thành công")
                .result(detailResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserStatusUpdateResponse>> updateOrganizerStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusUpdateRequest request) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentAdminId = userDetails.getUser().getId();

        AdminUserStatusUpdateResponse updateResponse = organizerService.updateOrganizerStatus(id, request, currentAdminId);

        String actionMessage = request.getStatus() == UserStatus.ACTIVE ? "Mở khóa" : "Khóa";

        ApiResponse<AdminUserStatusUpdateResponse> response = ApiResponse.<AdminUserStatusUpdateResponse>builder()
                .code(HttpStatus.OK.value())
                .message(actionMessage + " tài khoản Ban tổ chức thành công")
                .result(updateResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
