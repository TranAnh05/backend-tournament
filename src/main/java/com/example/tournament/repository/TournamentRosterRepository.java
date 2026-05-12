package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TournamentRosterRepository extends JpaRepository<TournamentRoster, Long> {
    List<TournamentRoster> findByTournamentAndClub(Tournament tournament, Club club);
    boolean existsByTournamentAndAthlete_Id(Tournament tournament, Long athleteId);
    Optional<TournamentRoster> findByTournamentAndAthlete_Id(Tournament tournament, Long athleteId);

    boolean existsByTournamentAndClub(Tournament tournament, Club club);
    void deleteByTournamentAndClub(Tournament tournament, Club club);

    List<TournamentRoster> findByTournamentIdAndClubId(Long tournamentId, Long clubId);

    long countByTournamentIdAndClubId(Long tournamentId, Long clubId);

    @Query("""
        SELECT r FROM TournamentRoster r
        WHERE r.club = :club
          AND r.tournament.id <> :tournamentId
          AND r.athlete.id IN :athleteIds
    """)
    List<TournamentRoster> findConflicts(
            @Param("club") Club club,
            @Param("tournamentId") Long tournamentId,
            @Param("athleteIds") Set<Long> athleteIds
    );
    @Query("""
        SELECT r FROM TournamentRoster r
        JOIN FETCH r.tournament
        WHERE r.club = :club
          AND r.tournament.id <> :tournamentId
    """)
    List<TournamentRoster> findByClubExcludingTournament(
            @Param("club") Club club,
            @Param("tournamentId") Long tournamentId
    );
}