package com.example.tournament.repository;

import com.example.tournament.entity.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    /**
     * Tìm bản ghi thống kê cá nhân của 1 VĐV trong 1 Giải đấu cụ thể
     * (Để lấy lên và cộng dồn số bàn thắng / kiến tạo / thẻ phạt)
     */
    Optional<PlayerStatistic> findByTournamentIdAndAthleteId(Long tournamentId, Long athleteId);

}
