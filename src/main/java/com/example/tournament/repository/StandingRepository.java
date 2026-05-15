package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {
    List<Standing> findByClub(Club club);
    Optional<Standing> findByTournamentIdAndClub(Long tournamentId, Club club);
    List<Standing> findByGroupStageIdOrderByTotalPointsDesc(Long groupStageId);

    List<Standing> findByGroupStageId(Long groupStageId);
    // REFEREE
    Optional<Standing> findByGroupStageIdAndClubId(Long groupStageId, Long clubId);

    // ORGANIER
    @Query("SELECT s FROM Standing s " +
            "JOIN FETCH s.club c " +
            "JOIN FETCH s.groupStage g " +
            "WHERE s.tournament.id = :tournamentId " +
            "AND g.stageType IN ('GROUP', 'KNOCKOUT')")
    List<Standing> findStandingsWithDetailsByTournamentId(@Param("tournamentId") Long tournamentId);

    @Query("SELECT s.club.id, " +         // Index 0
            "s.club.name, " +             // Index 1 (Mới thêm)
            "s.club.logoUrl, " +          // Index 2 (Mới thêm)
            "MAX(g.name), " +             // Index 3
            "SUM(s.matchesPlayed), " +    // Index 4
            "SUM(s.matchesWon), " +       // Index 5
            "SUM(s.matchesDrawn), " +     // Index 6
            "SUM(s.matchesLost), " +      // Index 7
            "SUM(s.scoresFor), " +        // Index 8
            "SUM(s.scoresAgainst), " +    // Index 9
            "SUM(s.scoreDifference), " +  // Index 10
            "SUM(s.totalPoints) " +       // Index 11
            "FROM Standing s " +
            "JOIN s.groupStage g " +
            "WHERE s.tournament.id = :tournamentId " +
            "GROUP BY s.club.id, s.club.name, s.club.logoUrl")
    List<Object[]> aggregateOverallStandingsByTournamentId(@Param("tournamentId") Long tournamentId);
}