package com.example.tournament.payload.request.club;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveMemberRequest {

    @NotNull(message = "Trạng thái phê duyệt không được để trống")
    private Boolean approved;

    // Bắt buộc khi approved = false
    private String rejectReason;
}