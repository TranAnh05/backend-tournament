package com.example.tournament.controller.club;

import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.club.DisciplineResponse;
import com.example.tournament.payload.response.club.RegistrationResponse;
import com.example.tournament.payload.response.club.TournamentResponse;
import com.example.tournament.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    // GET /tournaments
    @GetMapping("/tournaments")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getAllTournaments() {
        return ResponseEntity.ok(
                ApiResponse.<List<TournamentResponse>>builder()
                        .code(200)
                        .message("Lay danh sach giai dau thanh cong")
                        .result(tournamentService.getAllTournaments())
                        .build()
        );
    }

    // GET /clubs/me/disciplines
    @GetMapping("/clubs/me/disciplines")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<DisciplineResponse>>> getMyDisciplines() {
        return ResponseEntity.ok(
                ApiResponse.<List<DisciplineResponse>>builder()
                        .code(200)
                        .message("Lay danh sach ky luat thanh cong")
                        .result(tournamentService.getMyDisciplines())
                        .build()
        );
    }

    // GET /tournaments/registrations/my
    @GetMapping("/tournaments/registrations/my")
    @PreAuthorize("hasRole('CLUB_MANAGER')")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations() {
        return ResponseEntity.ok(
                ApiResponse.<List<RegistrationResponse>>builder()
                        .code(200)
                        .message("Lay danh sach dang ky giai thanh cong")
                        .result(tournamentService.getMyRegistrations())
                        .build()
        );
    }
}