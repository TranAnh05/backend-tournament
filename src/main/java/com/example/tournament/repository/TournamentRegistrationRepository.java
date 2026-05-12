//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.TournamentRegistration;
import com.example.tournament.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {
    List<TournamentRegistration> findByClub(Club club);
    Optional<TournamentRegistration> findByTournamentIdAndClub(Long tournamentId, Club club);

    // Lấy tất cả đội đăng ký của một giải đấu (có phân trang)
    Page<TournamentRegistration> findByTournamentId(Long tournamentId, Pageable pageable);

    // Lấy đội đăng ký của một giải đấu theo trạng thái (VD: Lọc ra các đội đang PENDING)
    Page<TournamentRegistration> findByTournamentIdAndStatus(Long tournamentId, RegistrationStatus status, Pageable pageable);

    long countByTournamentIdAndStatus(Long tournamentId, RegistrationStatus status);
    Optional<TournamentRegistration> findByIdAndTournamentId(Long id, Long tournamentId);
}