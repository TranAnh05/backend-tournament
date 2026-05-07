package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SportResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private List<RuleResponse> rules;
}
