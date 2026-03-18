package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Game;
import org.example.kidsmathapp.entity.enums.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByGameType(GameType gameType);

    List<Game> findByTopicsId(Long topicId);
}
