package com.example.tournament.repository;

import com.example.tournament.entity.Sport;
import com.example.tournament.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    // ADMIN
    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"rules"})
    @Query("SELECT s FROM Sport s WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR s.status = :status)")
    Page<Sport> searchSports(@Param("search") String search,
                             @Param("status") CommonStatus status,
                             Pageable pageable);
}
