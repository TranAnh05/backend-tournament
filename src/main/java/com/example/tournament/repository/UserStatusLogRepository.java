package com.example.tournament.repository;

import com.example.tournament.entity.UserStatusLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {
    // Lấy lịch sử thay đổi trạng thái của một user cụ thể
    List<UserStatusLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT l FROM UserStatusLog l ORDER BY l.createdAt DESC")
    List<UserStatusLog> findRecentActivities(Pageable pageable);
}
