package com.example.tournament.controller.organizer;

import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.organier.OverallStandingResponse;
import com.example.tournament.payload.response.organier.TournamentLookupResponse;
import com.example.tournament.payload.response.organier.TournamentStandingResponse;
import com.example.tournament.security.userdetail.CustomUserDetails;
import com.example.tournament.service.StandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizer/tournaments")
@RequiredArgsConstructor
public class OrganizerStandingController {

    private final StandingService standingService;

    @GetMapping("/{tournamentId}/standings")
    public ResponseEntity<ApiResponse<TournamentStandingResponse>> getStandings(@PathVariable Long tournamentId) {
        TournamentStandingResponse data = standingService.getTournamentStandings(tournamentId);

        return ResponseEntity.ok(ApiResponse.<TournamentStandingResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tải bảng xếp hạng thành công")
                .result(data)
                .build());
    }

    @GetMapping("/standing-context")
    public ResponseEntity<ApiResponse<List<TournamentLookupResponse>>> getStandingContextTournaments(
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long organizerId = userDetails.getUser().getId();

        List<TournamentLookupResponse> data = standingService.getTournamentsForStandingsLookup(organizerId);

        return ResponseEntity.ok(ApiResponse.<List<TournamentLookupResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Tải danh sách giải đấu thành công")
                .result(data)
                .build());
    }

    @GetMapping("/{tournamentId}/overall-standings")
    public ResponseEntity<ApiResponse<OverallStandingResponse>> getOverallStandings(
            @PathVariable Long tournamentId) {

        // Gọi Service xử lý logic gộp dữ liệu và sắp xếp theo phân bậc (Tiered Ranking)
        OverallStandingResponse data = standingService.getOverallStandings(tournamentId);

        return ResponseEntity.ok(ApiResponse.<OverallStandingResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tải bảng xếp hạng chung cuộc thành công")
                .result(data)
                .build());
    }
}
