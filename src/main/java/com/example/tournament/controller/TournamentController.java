package com.example.tournament.controller;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.Tournament.TournamentDetailResponse;
import com.example.tournament.payload.response.Tournament.TournamentResponse;
import com.example.tournament.security.jwt.JwtTokenProvider;
import com.example.tournament.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;

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
}
