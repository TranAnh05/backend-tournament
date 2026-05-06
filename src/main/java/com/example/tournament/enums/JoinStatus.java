package com.example.tournament.enums;

public enum JoinStatus {
    PENDING,  // Đang chờ duyệt
    APPROVED, // Đã được duyệt vào CLB
    REJECTED, // Bị từ chối
    LEFT,     // Tự động rời đi
    REMOVED   // Bị quản lý kích khỏi CLB
}
