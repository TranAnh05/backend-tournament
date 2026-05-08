package com.example.tournament.payload.request.club;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClubRequest {

    @NotBlank(message = "Tên CLB không được để trống")
    @Size(max = 255, message = "Tên CLB tối đa 255 ký tự")
    private String name;

    @NotBlank(message = "Tên viết tắt không được để trống")
    @Size(max = 50, message = "Tên viết tắt tối đa 50 ký tự")
    private String shortName;

    @Size(max = 500)
    private String headquarters;

    private Long homeVenueId;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 20)
    private String contactPhone;
}