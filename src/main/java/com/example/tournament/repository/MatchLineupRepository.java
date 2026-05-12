//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Athlete;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchLineup;
import com.example.tournament.enums.LineupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    List<MatchLineup> findByMatch(Match match);

    void deleteByMatchAndAthlete(Match match, Athlete athlete);

    // REFEREE
    @Query("SELECT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.athlete a " +
            "JOIN FETCH a.user u " +
            "WHERE ml.match.id = :matchId")
    List<MatchLineup> findLineupsByMatchId(@Param("matchId") Long matchId);

    @Modifying
    @Query("UPDATE MatchLineup ml SET ml.isConfirmed = true " +
            "WHERE ml.match.id = :matchId AND ml.id IN :lineupIds")
    int bulkConfirmLineups(@Param("matchId") Long matchId, @Param("lineupIds") List<Long> lineupIds);

    long countByMatchIdAndClubIdAndLineupTypeAndIsConfirmed(
            Long matchId,
            Long clubId,
            LineupType lineupType,
            Boolean isConfirmed);

    Optional<MatchLineup> findByMatchIdAndAthleteId(Long matchId, Long athleteId);
    // ==========================================
}