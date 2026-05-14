//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Match;
import com.example.tournament.enums.MatchStatus;
import com.example.tournament.enums.StageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // ĐÃ SỬA: Thêm JOIN FETCH để tránh LazyInitializationException
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.tournament " +
            "JOIN FETCH m.groupStage " +
            "LEFT JOIN FETCH m.homeClub " +
            "LEFT JOIN FETCH m.awayClub " +
            "LEFT JOIN FETCH m.events " +
            "WHERE m.homeClub = :club OR m.awayClub = :club " +
            "ORDER BY m.scheduledTime ASC")
    List<Match> findByClub(@Param("club") Club club);

    // ADMIN
    boolean existsByCourtId(Long courtId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.court.id = :courtId AND m.tournament.sport.id = :sportId " +
            "AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    boolean hasUpcomingMatchesForSportOnCourt(@Param("courtId") Long courtId, @Param("sportId") Long sportId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.court.venue.id = :venueId " +
            "AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    boolean hasActiveMatchesAtVenue(@Param("venueId") Long venueId);

    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.tournament t " +
            "JOIN FETCH t.sport s " +
            "JOIN FETCH m.homeClub hc " +
            "JOIN FETCH m.awayClub ac " +
            "LEFT JOIN FETCH m.court c " +
            "LEFT JOIN FETCH c.venue v " +
            "WHERE m.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "AND m.scheduledTime >= :startOfDay AND m.scheduledTime <= :endOfDay " +
            "ORDER BY m.scheduledTime ASC")
    List<Match> findLiveAndUpcomingMatches(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    long countByStatusAndUpdatedAtBetween(MatchStatus status, LocalDateTime start, LocalDateTime end);

    // REFEREE
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.tournament t " +
            "JOIN FETCH t.sport s " +
            "LEFT JOIN FETCH m.homeClub " +
            "LEFT JOIN FETCH m.awayClub " +
            "LEFT JOIN FETCH m.court c " +
            "LEFT JOIN FETCH c.venue " +
            "WHERE m.id = :matchId")
    Optional<Match> findMatchDetailById(@Param("matchId") Long matchId);

    // Kiểm tra xem bảng đấu này đã có lịch thi đấu chưa (Hàm bạn đã thêm lúc nãy)
    boolean existsByGroupStageId(Long groupStageId);

    List<Match> findByTournamentId(Long tournamentId);

    @Query("SELECT COUNT(m) > 0 FROM Match m " +
            "WHERE m.tournament.id = :tournamentId " +
            "AND m.groupStage.stageType = :stageType " +
            "AND m.status != :status")
    boolean hasUnfinalizedMatches(
            @Param("tournamentId") Long tournamentId,
            @Param("stageType") StageType stageType,
            @Param("status") MatchStatus status
    );


    @Query("SELECT m.nextMatch FROM Match m WHERE m.id = :currentMatchId")
    Optional<Match> findNextMatchByCurrentMatchId(@Param("currentMatchId") Long currentMatchId);
}