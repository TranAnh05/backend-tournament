package com.example.tournament.payload.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleResponse {
    private Long id;
    private String ruleKey;
    private String ruleValue;
    private String description;
}
