package com.example.tournament.enums;

public enum EventType {
    // Nhóm Ghi điểm
    GOAL, PT_1, PT_2, PT_3, OWN_GOAL, VAR_CHALLENGE,

    // Nhóm Thẻ phạt & Vi phạm
    YELLOW_CARD, RED_CARD, FOUL, PENALTY, TECHNICAL_FOUL,

    // Nhóm Thay đổi nhân sự
    SUBSTITUTION, INJURY,

    // Nhóm Thống kê chuyên môn (Stats)
    ASSIST, REBOUND, BLOCK, STEAL, ACE, WINNER,

    // Nhóm Thời gian
    START_PERIOD, END_PERIOD, TIMEOUT,

    // Nhóm Hệ thống (Trạng thái toàn trận)
    MATCH_START, MATCH_PAUSE, MATCH_RESUME, MATCH_END, MATCH_CANCEL, RESUME_MATCH, PAUSE_MATCH
}
