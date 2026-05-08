package com.example.tournament.controller.admin;

import com.example.tournament.payload.request.admin.VenueCreateRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.VenueResponse;
import com.example.tournament.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/venues")
@RequiredArgsConstructor
public class AdminVenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VenueResponse>>> getVenues(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<VenueResponse> pageResponse = venueService.getVenues(search, status, page, size);

        ApiResponse<PageResponse<VenueResponse>> response = ApiResponse.<PageResponse<VenueResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách địa điểm thành công")
                .result(pageResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody VenueCreateRequest request) {

        VenueResponse venueResponse = venueService.createVenue(request);

        ApiResponse<VenueResponse> response = ApiResponse.<VenueResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Thêm địa điểm và danh sách sân thành công")
                .result(venueResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
