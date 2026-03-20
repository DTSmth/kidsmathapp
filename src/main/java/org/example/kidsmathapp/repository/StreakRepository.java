package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByChildIdAndDate(Long childId, LocalDate date);

    List<Streak> findByChildId(Long childId);

    List<Streak> findByChildIdOrderByDateDesc(Long childId);

    List<Streak> findByChildIdAndDateBetween(Long childId, LocalDate startDate, LocalDate endDate);

    List<Streak> findByChildIdAndDateBetweenOrderByDateAsc(Long childId, LocalDate from, LocalDate to);

    boolean existsByChildIdAndDate(Long childId, LocalDate date);
}
