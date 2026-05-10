//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByManager(User manager);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    // ADMIN
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}