package com.example.tournament.controller.Tournament;


import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.Tournament.OrganizerMatchResponse;
import com.example.tournament.payload.response.club.MatchResponse;
import com.example.tournament.service.OrganizerMatchService;
import com.example.tournament.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tournaments")
@RequiredArgsConstructor
public class OrganizerMatchController {

    private final OrganizerMatchService matchService;
    private final ScheduleService scheduleService;

    @GetMapping("/{tournamentId}/matches")
    public ResponseEntity<ApiResponse<List<OrganizerMatchResponse>>> getTournamentMatches(
            @PathVariable Long tournamentId) {

        List<OrganizerMatchResponse> matches = matchService.getMatchesByTournament(tournamentId);

        return ResponseEntity.ok(
                ApiResponse.<List<OrganizerMatchResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách trận đấu thành công")
                        .result(matches)
                        .build()
        );
    }

    @PostMapping("/{tournamentId}/generate-group-schedule")
    public ResponseEntity<ApiResponse<String>> generateGroupSchedule(
            @PathVariable Long tournamentId) {

        // Gọi xuống tầng Service để chạy thuật toán Round-Robin
        scheduleService.generateGroupStageSchedule(tournamentId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Tạo lịch thi đấu vòng bảng thành công!")
                        .build()
        );
    }
}
