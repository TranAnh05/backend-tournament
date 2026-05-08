//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Athlete;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    List<MatchLineup> findByMatch(Match match);

    void deleteByMatchAndAthlete(Match match, Athlete athlete);
}