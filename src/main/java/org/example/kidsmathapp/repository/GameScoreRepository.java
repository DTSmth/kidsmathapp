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
}
