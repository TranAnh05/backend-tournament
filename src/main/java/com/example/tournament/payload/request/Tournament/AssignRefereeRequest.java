package com.example.tournament.payload.request.Tournament;

import com.example.tournament.enums.RefereeRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRefereeRequest {
    private Long refereeId;
    private RefereeRole role; // Mặc định là MAIN nếu không gửi
}
