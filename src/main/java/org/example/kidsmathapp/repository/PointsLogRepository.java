package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.PointsLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {

    List<PointsLog> findByChildId(Long childId);

    List<PointsLog> findByChildIdOrderByCreatedAtDesc(Long childId);

    List<PointsLog> findByChildIdOrderByCreatedAtDesc(Long childId, Pageable pageable);

    @Query("SELECT SUM(p.points) FROM PointsLog p WHERE p.child.id = :childId")
    Integer sumPointsByChildId(@Param("childId") Long childId);
}
