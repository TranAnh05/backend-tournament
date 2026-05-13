package com.example.tournament.repository;

import com.example.tournament.entity.GroupStage;
import com.example.tournament.enums.StageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupStageRepository extends JpaRepository<GroupStage,Long> {
    List<GroupStage> findByTournamentIdAndStageType(Long tournamentId, StageType stageType);
}
