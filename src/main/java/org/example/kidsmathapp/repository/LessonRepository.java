package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByTopicId(Long topicId);

    List<Lesson> findByTopicIdOrderByOrderIndexAsc(Long topicId);
}
