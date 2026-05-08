package com.example.tournament.payload.request.club;

import com.example.tournament.enums.ClubRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {

    @NotNull(message = "Vai trò không được để trống")
    private ClubRole clubRole;
}