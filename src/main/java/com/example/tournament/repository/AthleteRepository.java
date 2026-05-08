//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Athlete;
import com.example.tournament.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    Optional<Athlete> findByUser(User user);

    boolean existsByIdentityNumber(String identityNumber);
}