package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SportBasicResponse {
    private Long id;
    private String name;
}
