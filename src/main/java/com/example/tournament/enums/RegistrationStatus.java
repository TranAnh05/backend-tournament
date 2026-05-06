package com.example.tournament.enums;

public enum RegistrationStatus {
    PENDING,   // Đang chờ BTC duyệt
    APPROVED,  // Đã duyệt (Đủ điều kiện tham gia)
    REJECTED,  // Từ chối
    WITHDRAWN  // CLB tự rút lui
}
