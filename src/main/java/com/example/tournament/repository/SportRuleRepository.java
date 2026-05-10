package com.example.tournament.repository;

import com.example.tournament.entity.SportRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportRuleRepository extends JpaRepository<SportRule, Long> {
    // REFEREE
    List<SportRule> findBySportId(Long sportId);
    // ==================================================
}
