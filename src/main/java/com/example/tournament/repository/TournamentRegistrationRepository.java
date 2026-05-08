//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {
    List<TournamentRegistration> findByClub(Club club);
    Optional<TournamentRegistration> findByTournamentIdAndClub(Long tournamentId, Club club);
}