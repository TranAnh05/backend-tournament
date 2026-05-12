package com.example.tournament.payload.request.athlete;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateAthleteProfileRequest {

    @Size(max = 100, message = "Họ và tên không được quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;

    @Size(min = 9, max = 12, message = "Số CCCD phải từ 9 đến 12 ký tự")
    @Pattern(regexp = "^[0-9]+$", message = "Số CCCD chỉ được chứa chữ số")
    private String identityNumber;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Vị trí thi đấu không được quá 100 ký tự")
    private String preferredPosition;

    @Min(value = 1, message = "Số áo tối thiểu là 1")
    @Max(value = 99, message = "Số áo tối đa là 99")
    private Integer preferredNumber;

    @Size(max = 500, message = "URL ảnh không được quá 500 ký tự")
    private String portraitUrl;
}