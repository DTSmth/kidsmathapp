package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.GameScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    List<GameScore> findByChildIdAndGameId(Long childId, Long gameId);

    @Query("SELECT gs FROM GameScore gs WHERE gs.game.id = :gameId ORDER BY gs.score DESC")
    List<GameScore> findTopScoresByGameId(@Param("gameId") Long gameId, Pageable pageable);

    @Query("SELECT gs FROM GameScore gs WHERE gs.child.id = :childId ORDER BY gs.playedAt DESC")
    List<GameScore> findRecentByChildId(@Param("childId") Long childId, Pageable pageable);

    Optional<GameScore> findTopByChildIdAndGameIdOrderByScoreDesc(Long childId, Long gameId);

    @Query("SELECT gs FROM GameScore gs WHERE gs.child.id = :childId AND gs.game.id = :gameId ORDER BY gs.score DESC")
    List<GameScore> findByChildIdAndGameIdOrderByScoreDesc(@Param("childId") Long childId, @Param("gameId") Long gameId);

    @Query("SELECT SUM(gs.starsEarned) FROM GameScore gs WHERE gs.child.id = :childId")
    Integer getTotalStarsEarnedByChildId(@Param("childId") Long childId);

    // Single query: one best-score row per game for this child (no N+1)
    @Query("SELECT gs FROM GameScore gs WHERE gs.child.id = :childId AND gs.score = " +
           "(SELECT MAX(gs2.score) FROM GameScore gs2 WHERE gs2.child.id = :childId AND gs2.game.id = gs.game.id) " +
           "GROUP BY gs.game.id, gs.id")
    List<GameScore> findBestScorePerGameForChild(@Param("childId") Long childId);

    // Count plays per game to detect difficulty adaptation eligibility
    @Query("SELECT gs.game.id, COUNT(gs) FROM GameScore gs WHERE gs.child.id = :childId GROUP BY gs.game.id")
    List<Object[]> countPlaysByGameForChild(@Param("childId") Long childId);

    @Query("SELECT gs FROM GameScore gs JOIN FETCH gs.child WHERE gs.game.id = :gameId AND gs.gameMode = :gameMode AND gs.child.parent.id = :parentId ORDER BY gs.score DESC")
    List<GameScore> findFamilyLeaderboard(@Param("gameId") Long gameId, @Param("gameMode") String gameMode, @Param("parentId") Long parentId, org.springframework.data.domain.Pageable pageable);
}
