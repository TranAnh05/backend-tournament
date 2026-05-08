package com.example.tournament.controller.club;

import com.example.tournament.payload.request.club.SubmitLineupRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.club.MatchResponse;
import com.example.tournament.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMyMatches() {
        return ResponseEntity.ok(
                ApiResponse.<List<MatchResponse>>builder()
                        .code(200)
                        .message("Lay lich thi dau thanh cong")
                        .result(matchService.getMyMatches())
                        .build()
        );
    }

    @PostMapping("/{matchId}/lineup")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> submitLineup(
            @PathVariable Long matchId,
            @RequestBody SubmitLineupRequest request) {

        matchService.submitLineup(matchId, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Nop doi hinh thanh cong")
                        .build()
        );
    }
}