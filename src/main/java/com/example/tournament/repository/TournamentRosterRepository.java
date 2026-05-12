package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRosterRepository extends JpaRepository<TournamentRoster, Long> {
    List<TournamentRoster> findByTournamentAndClub(Tournament tournament, Club club);
    boolean existsByTournamentAndAthlete_Id(Tournament tournament, Long athleteId);
    Optional<TournamentRoster> findByTournamentAndAthlete_Id(Tournament tournament, Long athleteId);

    boolean existsByTournamentAndClub(Tournament tournament, Club club);
    void deleteByTournamentAndClub(Tournament tournament, Club club);

    List<TournamentRoster> findByTournamentIdAndClubId(Long tournamentId, Long clubId);
}