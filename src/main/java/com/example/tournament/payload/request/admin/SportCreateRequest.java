package com.example.tournament.payload.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SportCreateRequest {
    @NotBlank(message = "Tên môn thể thao không được để trống")
    private String name;

    private String description;

    private List<RuleRequest> rules;
}
