package com.example.tournament.payload.request.Tournament;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KnockoutDrawRequest {
    @NotEmpty(message = "Danh sách đội tham gia không được để trống")
    private List<Long> qualifiedClubIds;
}
