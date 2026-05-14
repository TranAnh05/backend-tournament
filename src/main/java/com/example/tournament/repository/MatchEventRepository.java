package com.example.tournament.repository;

import com.example.tournament.entity.MatchEvent;
import com.example.tournament.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    // Lấy toàn bộ sự kiện của một trận đấu, xếp theo thứ tự thời gian xảy ra
    List<MatchEvent> findByMatchIdOrderByCreatedAtAsc(Long matchId);

    List<MatchEvent> findByMatchIdAndEventTypeIn(Long matchId, List<EventType> eventTypes);
}
