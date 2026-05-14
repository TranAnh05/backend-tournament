package com.example.tournament.payload.response.Tournament;


import com.example.tournament.enums.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerMatchResponse {
    private Long id;
    private LocalDateTime scheduledTime;
    private MatchStatus status;
    private String groupStageName; // Hiển thị "Bảng A", "Bán Kết"...

    private ClubSummaryResponse homeClub;
    private ClubSummaryResponse awayClub;
    private RefereeSummaryResponse referee;
    private CourtResponse court;

}
