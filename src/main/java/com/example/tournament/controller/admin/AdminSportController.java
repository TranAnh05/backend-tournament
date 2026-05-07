package com.example.tournament.controller.admin;

import com.example.tournament.payload.request.admin.SportCreateRequest;
import com.example.tournament.payload.request.admin.SportUpdateRequest;
import com.example.tournament.payload.request.admin.StatusUpdateRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.SportResponse;
import com.example.tournament.service.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sports")
@RequiredArgsConstructor
public class AdminSportController {

    private final SportService sportService;

    @PostMapping
    public ResponseEntity<ApiResponse<SportResponse>> createSport(@Valid @RequestBody SportCreateRequest request) {
        SportResponse sportResponse = sportService.createSportWithRules(request);

        ApiResponse<SportResponse> response = ApiResponse.<SportResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tạo môn thể thao và cấu hình luật thành công")
                .result(sportResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SportResponse>>> getSports(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<SportResponse> pageResponse = sportService.getSports(search, status, page, size);

        ApiResponse<PageResponse<SportResponse>> response = ApiResponse.<PageResponse<SportResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách môn thể thao thành công")
                .result(pageResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SportResponse>> updateSport(
            @PathVariable Long id,
            @Valid @RequestBody SportUpdateRequest request) {

        SportResponse updatedSport = sportService.updateSport(id, request);

        ApiResponse<SportResponse> response = ApiResponse.<SportResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật môn thể thao thành công")
                .result(updatedSport)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SportResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        SportResponse updatedSport = sportService.updateStatus(id, request.getStatus());

        ApiResponse<SportResponse> response = ApiResponse.<SportResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái thành công")
                .result(updatedSport)
                .build();

        return ResponseEntity.ok(response);
    }
}
