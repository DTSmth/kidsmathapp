package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByLessonId(Long lessonId);

    List<Question> findByGameId(Long gameId);

    List<Question> findByDifficulty(Difficulty difficulty);

    List<Question> findByQuestionType(QuestionType questionType);

    List<Question> findByLessonIdAndDifficulty(Long lessonId, Difficulty difficulty);

    List<Question> findByGameIdAndDifficulty(Long gameId, Difficulty difficulty);
}
