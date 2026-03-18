package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.ChildAchievement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildAchievementRepository extends JpaRepository<ChildAchievement, Long> {

    Optional<ChildAchievement> findByChildIdAndAchievementId(Long childId, Long achievementId);

    List<ChildAchievement> findByChildId(Long childId);

    List<ChildAchievement> findByAchievementId(Long achievementId);

    boolean existsByChildIdAndAchievementId(Long childId, Long achievementId);

    List<ChildAchievement> findByChildIdOrderByUnlockedAtDesc(Long childId);

    List<ChildAchievement> findByChildIdOrderByUnlockedAtDesc(Long childId, Pageable pageable);
}
