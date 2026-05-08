//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.homeClub = :club OR m.awayClub = :club ORDER BY m.scheduledTime ASC")
    List<Match> findByClub(@Param("club") Club club);
}