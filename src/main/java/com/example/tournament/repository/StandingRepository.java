package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {
    List<Standing> findByClub(Club club);
    Optional<Standing> findByTournamentIdAndClub(Long tournamentId, Club club);
    List<Standing> findByGroupStageIdOrderByTotalPointsDesc(Long groupStageId);

    // REFEREE
    Optional<Standing> findByGroupStageIdAndClubId(Long groupStageId, Long clubId);
}