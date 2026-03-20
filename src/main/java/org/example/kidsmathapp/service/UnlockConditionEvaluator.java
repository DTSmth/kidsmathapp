package org.example.kidsmathapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnlockConditionEvaluator {

    private final ObjectMapper objectMapper;
    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;

    /**
     * Evaluates an unlock condition JSON against a child's current state.
     *
     * Supported types:
     *   lessons_completed: {"type":"lessons_completed","count":5}
     *   topic_completed:   {"type":"topic_completed","topicId":1}
     *   streak:            {"type":"streak","days":7}
     *   stars:             {"type":"stars","count":100}
     *   topics_started:    {"type":"topics_started","count":3}
     *   always:            {"type":"always"}
     */
    public boolean evaluate(Child child, String conditionJson) {
        if (conditionJson == null || conditionJson.isBlank()) return false;
        try {
            JsonNode condition = objectMapper.readTree(conditionJson);
            String type = condition.has("type") ? condition.get("type").asText() : "";
            return switch (type) {
                case "lessons_completed" -> evaluateLessonsCompleted(child, condition);
                case "topic_completed"   -> evaluateTopicCompleted(child, condition);
                case "streak"            -> child.getCurrentStreak() >= condition.path("days").asInt(0);
                case "stars"             -> child.getTotalStars()    >= condition.path("count").asInt(0);
                case "topics_started"    -> evaluateTopicsStarted(child, condition);
                case "always"            -> true;
                default -> {
                    log.warn("Unknown unlock condition type: {}", type);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Failed to evaluate unlock condition: {}", conditionJson, e);
            return false;
        }
    }

    private boolean evaluateLessonsCompleted(Child child, JsonNode condition) {
        int required = condition.path("count").asInt(0);
        long completed = progressRepository.countByChildIdAndCompletedTrue(child.getId());
        return completed >= required;
    }

    private boolean evaluateTopicCompleted(Child child, JsonNode condition) {
        if (!condition.has("topicId")) return false;
        Long topicId = condition.get("topicId").asLong();
        int total = lessonRepository.findByTopicId(topicId).size();
        if (total == 0) return false;
        long done = progressRepository.findByChildIdAndLessonTopicId(child.getId(), topicId)
                .stream().filter(p -> Boolean.TRUE.equals(p.getCompleted())).count();
        return done >= total;
    }

    private boolean evaluateTopicsStarted(Child child, JsonNode condition) {
        int required = condition.path("count").asInt(0);
        long started = progressRepository.countDistinctStartedTopicsByChildId(child.getId());
        return started >= required;
    }
}
