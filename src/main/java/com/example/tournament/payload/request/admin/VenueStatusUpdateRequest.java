package com.example.tournament.payload.request.admin;

import com.example.tournament.enums.CommonStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VenueStatusUpdateRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private CommonStatus status;
}
