//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.homeClub = :club OR m.awayClub = :club ORDER BY m.scheduledTime ASC")
    List<Match> findByClub(@Param("club") Club club);

    // ADMIN
    // Kiểm tra xem sân có trận đấu nào không
    boolean existsByCourtId(Long courtId);

    // Kiểm tra xem một môn thể thao cụ thể trên sân có trận đấu nào chưa kết thúc không
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.court.id = :courtId AND m.tournament.sport.id = :sportId " +
            "AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    boolean hasUpcomingMatchesForSportOnCourt(@Param("courtId") Long courtId, @Param("sportId") Long sportId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.court.venue.id = :venueId " +
            "AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    boolean hasActiveMatchesAtVenue(@Param("venueId") Long venueId);
}