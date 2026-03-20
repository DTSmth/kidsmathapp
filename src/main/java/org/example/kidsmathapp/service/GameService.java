package org.example.kidsmathapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.content.*;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Game;
import org.example.kidsmathapp.entity.GameScore;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.GameMode;
import org.example.kidsmathapp.entity.enums.ItemDropSource;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GAME FLOW:
 *
 * GameController ──► GameService.getGames(childId)
 *                         └──► GameScoreRepository.findBestScorePerGameForChild (1 query)
 *
 * GameController ──► GameService.getGameDetail(gameId, childId)
 *                         └──► Adaptive difficulty: if ≥3 plays with >80% EASY accuracy → prefer MEDIUM
 *
 * GameController ──► GameService.recordScore(gameId, req)
 *                         ├──► saveGameScore() [REQUIRES_NEW — always persists]
 *                         └──► applyGamification() [try-catch — survives failure]
 *                                   └──► GamificationOrchestrator.orchestrate()
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final ChildRepository childRepository;
    private final QuestionRepository questionRepository;
    private final GamificationOrchestrator gamificationOrchestrator;

    @Transactional(readOnly = true)
    public List<GameDto> getGames(Long childId) {
        List<Game> games = gameRepository.findAll();

        // Load all personal bests in one query — no N+1
        Map<Long, GameScore> bestByGame = new HashMap<>();
        gameScoreRepository.findBestScorePerGameForChild(childId)
                .forEach(gs -> bestByGame.put(gs.getGame().getId(), gs));

        return games.stream()
                .map(game -> {
                    GameScore best = bestByGame.get(game.getId());
                    return GameDto.builder()
                            .id(game.getId())
                            .name(game.getName())
                            .description(game.getDescription())
                            .gameType(game.getGameType())
                            .iconName(game.getIconName())
                            .baseStarsReward(game.getBaseStarsReward())
                            .timeLimit(game.getTimeLimit())
                            .personalBestScore(best != null ? best.getScore() : null)
                            .personalBestStars(best != null ? best.getStarsEarned() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GameDetailDto getGameDetail(Long gameId, Long childId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> ApiException.notFound("Game not found"));

        List<Question> questions = questionRepository.findByGameId(gameId);
        questions = applyAdaptiveDifficulty(questions, gameId, childId);

        List<QuestionDto> questionDtos = questions.stream()
                .map(this::toQuestionDto)
                .collect(Collectors.toList());

        String bestAnswersLog = gameScoreRepository
                .findTopByChildIdAndGameIdOrderByScoreDesc(childId, gameId)
                .map(GameScore::getAnswersLog)
                .orElse(null);

        return GameDetailDto.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .gameType(game.getGameType())
                .baseStarsReward(game.getBaseStarsReward())
                .timeLimit(game.getTimeLimit())
                .questions(questionDtos)
                .bestAnswersLog(bestAnswersLog)
                .build();
    }

    /**
     * Adaptive difficulty: if child has ≥3 plays and >80% EASY accuracy, prefer harder questions.
     * Falls back to all questions if not enough data or no harder questions available.
     */
    private List<Question> applyAdaptiveDifficulty(List<Question> all, Long gameId, Long childId) {
        List<GameScore> history = gameScoreRepository.findByChildIdAndGameId(childId, gameId);
        if (history.size() < 3) {
            return all; // not enough data yet
        }

        // Average score as proxy for EASY mastery (score = % correct)
        double avgScore = history.stream().mapToInt(GameScore::getScore).average().orElse(0);

        if (avgScore >= 80) {
            List<Question> harder = all.stream()
                    .filter(q -> q.getDifficulty() == Difficulty.MEDIUM || q.getDifficulty() == Difficulty.HARD)
                    .collect(Collectors.toList());
            if (!harder.isEmpty()) {
                log.debug("Adaptive difficulty: child {} avg {}% on game {} — serving {} harder questions",
                        childId, (int) avgScore, gameId, harder.size());
                return harder;
            }
        }

        return all;
    }

    @Transactional
    public GameScoreResultDto recordScore(Long gameId, GameScoreRequest req) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> ApiException.notFound("Game not found"));
        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> ApiException.notFound("Child not found"));

        int starsEarned = calculateStars(req.getScore(), game.getBaseStarsReward(),
                req.getComboBonus(), game.getBaseStarsReward());

        // Check personal best before saving
        Optional<GameScore> previousBest = gameScoreRepository
                .findTopByChildIdAndGameIdOrderByScoreDesc(child.getId(), game.getId());
        boolean isNewBest = previousBest.isEmpty() || req.getScore() > previousBest.get().getScore();

        // Save score in its own transaction — survives gamification failure
        GameScore saved = saveGameScore(game, child, req, starsEarned,
                GameMode.valueOf(req.getGameMode() != null ? req.getGameMode() : "NORMAL"));

        // Apply gamification in outer transaction with rescue
        boolean gamificationApplied = true;
        boolean streakUpdated = false;
        List<AchievementDto> newAchievements = Collections.emptyList();
        org.example.kidsmathapp.dto.inventory.InventoryItemDto droppedItem = null;
        try {
            GamificationOrchestrator.OrchestratorResult result = gamificationOrchestrator.orchestrate(
                    child.getId(), starsEarned,
                    String.format("Completed game: %s (Score: %d%%)", game.getName(), req.getScore()),
                    ItemDropSource.GAME_COMPLETION
            );
            streakUpdated = result.streakUpdated();
            newAchievements = result.newAchievements();
            if (!result.newItems().isEmpty()) {
                droppedItem = result.newItems().get(0);
            }
        } catch (Exception e) {
            gamificationApplied = false;
            log.error("Gamification failed for child {} game {} score {} — GameScore id={} still saved",
                    child.getId(), gameId, req.getScore(), saved.getId(), e);
        }

        log.info("Game score recorded: child={} game={} score={} stars={} newBest={} itemDrop={}",
                child.getId(), gameId, req.getScore(), starsEarned, isNewBest, droppedItem != null ? droppedItem.getName() : "none");

        return GameScoreResultDto.builder()
                .score(req.getScore())
                .starsEarned(starsEarned)
                .personalBestScore(isNewBest ? req.getScore() : previousBest.get().getScore())
                .isNewPersonalBest(isNewBest)
                .streakUpdated(streakUpdated)
                .newAchievements(newAchievements)
                .gamificationApplied(gamificationApplied)
                .newItem(droppedItem)
                .build();
    }

    /**
     * Saved in REQUIRES_NEW — always commits even if the outer transaction fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GameScore saveGameScore(Game game, Child child, GameScoreRequest req, int starsEarned, GameMode gameMode) {
        GameScore gs = GameScore.builder()
                .game(game)
                .child(child)
                .score(req.getScore())
                .starsEarned(starsEarned)
                .timeSpent(req.getTimeSpent())
                .answersLog(req.getAnswersLog())
                .gameMode(gameMode)
                .playedAt(LocalDateTime.now())
                .build();
        return gameScoreRepository.save(gs);
    }

    /**
     * Stars = (score% * baseReward) + comboBonus, with comboBonus capped at 50% of base.
     */
    int calculateStars(int score, int baseReward, Integer comboBonus, int baseStarsReward) {
        int base = (int) Math.round((score / 100.0) * baseReward);
        int cappedCombo = comboBonus != null
                ? Math.min(Math.max(comboBonus, 0), baseStarsReward / 2)
                : 0;
        return base + cappedCombo;
    }

    private QuestionDto toQuestionDto(Question question) {
        List<String> options = Collections.emptyList();
        if (question.getOptions() != null && !question.getOptions().isBlank()) {
            try {
                options = OBJECT_MAPPER.readValue(question.getOptions(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to parse game question options for id={}", question.getId());
            }
        }
        return QuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .options(options)
                .difficulty(question.getDifficulty())
                .imageUrl(question.getImageUrl())
                .correctAnswer(question.getCorrectAnswer())
                .build();
    }
}
