package com.example.tournament.payload.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SportUpdateRequest {
    @NotBlank(message = "Tên môn thể thao không được để trống")
    private String name;

    private String description;

    @Valid
    private List<RuleRequest> rules;
}
