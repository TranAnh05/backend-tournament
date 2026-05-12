package com.example.tournament.controller.referee;

import com.example.tournament.payload.request.referee.ChangeMatchStatusRequest;
import com.example.tournament.payload.request.referee.ConfirmLineupRequest;
import com.example.tournament.payload.request.referee.MatchEventRequest;
import com.example.tournament.payload.request.referee.RefereeMatchRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.referee.MatchDetailResponse;
import com.example.tournament.payload.response.referee.RefereeAssignedMatchResponse;
import com.example.tournament.security.userdetail.CustomUserDetails;
import com.example.tournament.service.RefereeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/referee")
@RequiredArgsConstructor
public class RefereeController {

    private final RefereeService refereeService;

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<RefereeAssignedMatchResponse>>> getAssignedMatches(
            @ModelAttribute RefereeMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long refereeId = userDetails.getUser().getId();

        List<RefereeAssignedMatchResponse> matches = refereeService.getAssignedMatches(refereeId, request);

        ApiResponse<List<RefereeAssignedMatchResponse>> response = ApiResponse.<List<RefereeAssignedMatchResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách phân công thành công")
                .result(matches)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> getMatchDetail(
            @PathVariable("id") Long matchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long refereeId = userDetails.getUser().getId();

        MatchDetailResponse detail = refereeService.getMatchDetail(refereeId, matchId);

        ApiResponse<MatchDetailResponse> response = ApiResponse.<MatchDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết trận đấu thành công")
                .result(detail)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/matches/{id}/lineup-confirm")
    public ResponseEntity<ApiResponse<String>> confirmLineups(
            @PathVariable("id") Long matchId,
            @RequestBody @Valid ConfirmLineupRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long refereeId = userDetails.getUser().getId();

        String message = refereeService.confirmLineups(refereeId, matchId, request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message(message)
                .result(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/matches/{id}/status")
    public ResponseEntity<ApiResponse<String>> changeMatchStatus(
            @PathVariable("id") Long matchId,
            @RequestBody @Valid ChangeMatchStatusRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long refereeId = userDetails.getUser().getId();

        String message = refereeService.changeMatchStatus(refereeId, matchId, request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message(message)
                .result(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/matches/{id}/events")
    public ResponseEntity<ApiResponse<String>> recordMatchEvent(
            @PathVariable("id") Long matchId,
            @RequestBody @Valid MatchEventRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long refereeId = userDetails.getUser().getId();


        String message = refereeService.recordMatchEvent(refereeId, matchId, request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message(message)
                .result(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
