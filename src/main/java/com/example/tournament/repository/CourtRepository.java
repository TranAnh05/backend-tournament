package com.example.tournament.repository;


import com.example.tournament.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface  CourtRepository extends JpaRepository<Court,Long> {
    // 1. Lọc sân cho TỰ ĐỘNG XẾP LỊCH (Chỉ cần cùng Địa điểm và cùng Môn thể thao)
    // Lưu ý: Sửa lại tên trường `supportedSports`, `location`, `sport` cho khớp với Entity của bạn nhé!
    @Query("SELECT c FROM Court c " +
            "JOIN c.supportedSports s " + // Bảng trung gian court_supporter_sport
            "WHERE s.id = :sportId " +
            "AND c.venue.id = :venueId")
    List<Court> findValidCourtsForTournament(
            @Param("sportId") Long sportId,
            @Param("venueId") Long venueId
    );

    // 2. Lọc sân cho XẾP THỦ CÔNG (Thêm lớp phễu check trùng lịch)
    @Query("SELECT c FROM Court c " +
            "JOIN c.supportedSports s " +
            "WHERE s.id = :sportId " +
            "AND c.venue.id = :venueId " +
            "AND c.id NOT IN (" +
            "   SELECT m.court.id FROM Match m " +
            "   WHERE m.court IS NOT NULL " +
            "   AND m.scheduledTime BETWEEN :startTime AND :endTime" +
            ")")
    List<Court> findAvailableCourts(
            @Param("sportId") Long sportId,
            @Param("venueId") Long venueId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
