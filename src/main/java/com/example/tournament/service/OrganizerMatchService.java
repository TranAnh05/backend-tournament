package com.example.tournament.service;


import com.example.tournament.entity.Court;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchReferee;
import com.example.tournament.entity.User;
import com.example.tournament.enums.RefereeRole;
import com.example.tournament.payload.request.Tournament.AssignRefereeRequest;
import com.example.tournament.payload.response.Tournament.*;
import com.example.tournament.payload.response.club.MatchResponse;
import com.example.tournament.repository.CourtRepository;
import com.example.tournament.repository.MatchRefereeRepository;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerMatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;

    private final MatchRefereeRepository matchRefereeRepository;

    public List<OrganizerMatchResponse> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);

        return matches.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
    }

    private OrganizerMatchResponse mapToMatchResponse(Match match) {
        // Mapping thông tin đội nhà
        ClubSummaryResponse home = match.getHomeClub() != null ? ClubSummaryResponse.builder()
                .id(match.getHomeClub().getId())
                .name(match.getHomeClub().getName())
                .logo(match.getHomeClub().getLogoUrl())
                .build() : null;

        // Mapping thông tin đội khách
        ClubSummaryResponse away = match.getAwayClub() != null ? ClubSummaryResponse.builder()
                .id(match.getAwayClub().getId())
                .name(match.getAwayClub().getName())
                .logo(match.getAwayClub().getLogoUrl())
                .build() : null;

        RefereeSummaryResponse refereeResponse = null;
        if (match.getReferees() != null && !match.getReferees().isEmpty()) {
            match.getReferees().stream()
                    .filter(mr -> mr.getRoleInMatch() == RefereeRole.MAIN) // Chỉ lấy Trọng tài chính
                    .findFirst()
                    .ifPresent(mainRef -> {
                        // Vì bên trong lamda biểu thức không gán trực tiếp biến ngoài được,
                        // ta sẽ set giá trị bằng cách tách hàm hoặc làm như sau:
                    });

            // Cách viết dễ hiểu hơn không dùng lamda scope:
            for (MatchReferee mr : match.getReferees()) {
                if (mr.getRoleInMatch() == RefereeRole.MAIN) {
                    refereeResponse = RefereeSummaryResponse.builder()
                            .id(mr.getReferee().getId())
                            .fullName(mr.getReferee().getFullName())
                            .build();
                    break; // Tìm thấy trọng tài chính thì thoát vòng lặp
                }
            }
        }
        CourtResponse courtRes = null;
        if (match.getCourt() != null) {
            courtRes = CourtResponse.builder()
                    .id(match.getCourt().getId())
                    .courtName(match.getCourt().getCourtName()) // Chú ý: Dùng getName() hay getCourtName() tùy thuộc vào Entity Court của bạn
                    .build();
        }
        return OrganizerMatchResponse.builder()
                .id(match.getId())
                .scheduledTime(match.getScheduledTime())
                .status(match.getStatus())
                .groupStageName(match.getGroupStage() != null ? match.getGroupStage().getName() : "N/A")
                .homeClub(home)
                .awayClub(away)
                .referee(refereeResponse)
                .court(courtRes)
                .build();
    }

    public List<emptyscheduleRefereeResponse> getAvailableRefereesForMatch(Long matchId) {
        // 1. Tìm trận đấu để lấy giờ thi đấu
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu!"));

        if (match.getScheduledTime() == null) {
            throw new RuntimeException("Trận đấu chưa được xếp lịch, không thể tìm trọng tài!");
        }

        LocalDateTime matchTime = match.getScheduledTime();

        // 2. Tính toán khoảng thời gian bận (Buffer time).
        // Giả sử 1 trận kéo dài 2 tiếng. Trọng tài cần đến trước 30p và nghỉ ngơi sau trận 30p.
        // Tức là khoảng bận = Trước 2.5 tiếng và Sau 2.5 tiếng.
        LocalDateTime startTime = matchTime.minusHours(2).minusMinutes(30);
        LocalDateTime endTime = matchTime.plusHours(2).plusMinutes(30);

        // 3. Lấy danh sách trọng tài rảnh từ Database
        List<User> availableReferees = userRepository.findAvailableReferees(startTime, endTime);

        // 4. Map Entity sang DTO để trả về Frontend
        return availableReferees.stream()
                .map(user -> emptyscheduleRefereeResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignRefereeToMatch(Long matchId, AssignRefereeRequest request) {
        // 1. Tìm trận đấu
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu!"));

        // 2. Tìm User trọng tài
        User referee = userRepository.findById(request.getRefereeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 3. Kiểm tra Role: Phải có Role REFEREE
        boolean isReferee = referee.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode().name().equals("REFEREE"));
        if (!isReferee) {
            throw new RuntimeException("Người dùng này không có vai trò trọng tài!");
        }

        // 4. Kiểm tra xem trọng tài này đã có trong trận này chưa
        if (matchRefereeRepository.existsByMatchIdAndRefereeId(matchId, request.getRefereeId())) {
            throw new RuntimeException("Trọng tài này đã được phân công cho trận đấu này rồi!");
        }

        // 5. (Nâng cao) Kiểm tra lại lịch trống một lần nữa ở phía Server để đảm bảo an toàn
        // Tận dụng hàm findAvailableReferees bạn đã viết ở bước trước...

        // 6. Tạo bản ghi phân công
        MatchReferee assignment = MatchReferee.builder()
                .match(match)
                .referee(referee)
                .roleInMatch(request.getRole() != null ? request.getRole() : RefereeRole.MAIN)
                .build();

        matchRefereeRepository.save(assignment);
    }
    // 1. Lấy danh sách sân trống cho 1 trận đấu cụ thể
    public List<CourtResponse> getAvailableCourtsForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu!"));

        if (match.getScheduledTime() == null) {
            throw new RuntimeException("Trận đấu chưa có lịch!");
        }

        // Tạo khoảng đệm thời gian an toàn (+/- 2 tiếng)
        LocalDateTime startTime = match.getScheduledTime().minusHours(2);
        LocalDateTime endTime = match.getScheduledTime().plusHours(2);
        // Lấy danh sách Entity từ DB
        List<Court> availableCourts = courtRepository.findAvailableCourts(
                match.getTournament().getSport().getId(),
                match.getTournament().getVenue().getId(),
                startTime,
                endTime
        );

        // Lấy từ repository (Dùng hàm đã viết ở bước trước)
// ✨ MAP SANG DTO để ngắt hoàn toàn các liên kết lồng nhau
        return availableCourts.stream()
                .map(court -> CourtResponse.builder()
                        .id(court.getId())
                        .name(court.getCourtName()) // Thay getName() bằng getter tương ứng của Entity Court
                        .build())
                .collect(Collectors.toList());
    }

    // 2. Gán sân thi đấu
    @Transactional
    public OrganizerMatchResponse assignCourtToMatch(Long matchId, Long courtId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu!"));
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sân!"));

        match.setCourt(court);
        return mapToMatchResponse(matchRepository.save(match));
    }
}
