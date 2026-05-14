package com.example.tournament.controller.Tournament;


import com.example.tournament.entity.Court;
import com.example.tournament.payload.request.Tournament.AssignRefereeRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.Tournament.CourtResponse;
import com.example.tournament.payload.response.Tournament.OrganizerMatchResponse;
import com.example.tournament.payload.response.Tournament.emptyscheduleRefereeResponse;
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

    @GetMapping("matches/{matchId}/available-referees")
    public ResponseEntity<ApiResponse<List<emptyscheduleRefereeResponse>>> getAvailableReferees(
            @PathVariable Long matchId) {

        List<emptyscheduleRefereeResponse> freeReferees = matchService.getAvailableRefereesForMatch(matchId);

        return ResponseEntity.ok(
                ApiResponse.<List<emptyscheduleRefereeResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách trọng tài trống lịch thành công!")
                        .result(freeReferees)
                        .build()
        );


    }

    @PostMapping("matches/{matchId}/referees")
    public ResponseEntity<ApiResponse<String>> assignReferee(
            @PathVariable Long matchId,
            @RequestBody AssignRefereeRequest request) {

        matchService.assignRefereeToMatch(matchId, request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Phân công trọng tài thành công!")
                        .build()
        );
    }

//    // ✨ API Gỡ trọng tài khỏi trận đấu (Nếu BTC chọn nhầm)
//    @DeleteMapping("/{matchId}/referees/{refereeId}")
//    public ResponseEntity<ApiResponse<String>> removeReferee(
//            @PathVariable Long matchId,
//            @PathVariable Long refereeId) {
//
//        matchService.removeRefereeFromMatch(matchId, refereeId); // Bạn viết thêm hàm delete tương ứng nhé
//
//        return ResponseEntity.ok(
//                ApiResponse.<String>builder()
//                        .code(200)
//                        .message("Đã gỡ trọng tài khỏi trận đấu")
//                        .build()
//        );
//    }
@GetMapping("/{matchId}/available-courts")
public ResponseEntity<ApiResponse<List<CourtResponse>>> getAvailableCourts(@PathVariable Long matchId) {
    return ResponseEntity.ok(
            ApiResponse.<List<CourtResponse>>builder()
                    .code(200)
                    .result(matchService.getAvailableCourtsForMatch(matchId)).build()
    );
}

    @PatchMapping("/{matchId}/court")
    public ResponseEntity<ApiResponse<OrganizerMatchResponse>> assignCourt(
            @PathVariable Long matchId,
            @RequestParam Long courtId) {
        return ResponseEntity.ok(
                ApiResponse.<OrganizerMatchResponse>builder().code(200).message("Đổi sân thành công!").result(matchService.assignCourtToMatch(matchId, courtId)).build()
        );
    }
}
