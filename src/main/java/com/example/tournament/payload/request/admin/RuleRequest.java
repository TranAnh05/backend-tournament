package com.example.tournament.payload.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RuleRequest {
    @NotBlank(message = "Mã quy tắc (key) không được để trống")
    private String ruleKey;

    @NotBlank(message = "Giá trị quy tắc không được để trống")
    private String ruleValue;

    private String description;
}
