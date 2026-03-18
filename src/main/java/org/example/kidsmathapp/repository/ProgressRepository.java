package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByChildIdAndLessonId(Long childId, Long lessonId);

    List<Progress> findByChildId(Long childId);

    List<Progress> findByLessonId(Long lessonId);

    List<Progress> findByChildIdAndCompleted(Long childId, Boolean completed);

    boolean existsByChildIdAndLessonId(Long childId, Long lessonId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.child.id = :childId AND p.completed = true")
    long countByChildIdAndCompletedTrue(@Param("childId") Long childId);

    @Query("SELECT p FROM Progress p WHERE p.child.id = :childId AND p.lesson.topic.id = :topicId")
    List<Progress> findByChildIdAndLessonTopicId(@Param("childId") Long childId, @Param("topicId") Long topicId);

    @Query("SELECT COUNT(DISTINCT p.lesson.topic.id) FROM Progress p WHERE p.child.id = :childId AND p.completed = true")
    long countDistinctCompletedTopicsByChildId(@Param("childId") Long childId);

    @Query("SELECT COUNT(DISTINCT p.lesson.topic.id) FROM Progress p WHERE p.child.id = :childId")
    long countDistinctStartedTopicsByChildId(@Param("childId") Long childId);

    /**
     * Returns completed lesson counts grouped by topic ID for a given child.
     * Used to avoid N+1 queries when building TopicsWithProgress.
     * Returns List of [topicId (Long), completedCount (Long)] pairs.
     */
    @Query("SELECT p.lesson.topic.id, COUNT(p) FROM Progress p " +
           "WHERE p.child.id = :childId AND p.completed = true " +
           "GROUP BY p.lesson.topic.id")
    List<Object[]> countCompletedGroupByTopicId(@Param("childId") Long childId);
}
