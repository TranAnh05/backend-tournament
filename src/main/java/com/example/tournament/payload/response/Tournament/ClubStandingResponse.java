package com.example.tournament.payload.response.Tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClubStandingResponse {
    private Long clubId;
    private String name;
    private String logo;
    private Integer seed; // Chúng ta có thể dùng ID hoặc một logic số thứ tự để làm hạt giống
}
