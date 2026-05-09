package com.example.tournament.controller.admin;

import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.admin.AdminOrganizerDetailResponse;
import com.example.tournament.payload.response.admin.AdminOrganizerResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.service.AdminOrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
