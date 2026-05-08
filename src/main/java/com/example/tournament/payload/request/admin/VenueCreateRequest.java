package com.example.tournament.payload.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class VenueCreateRequest {
    @NotBlank(message = "Tên địa điểm không được để trống")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Valid
    private List<CourtCreateRequest> courts;
}
