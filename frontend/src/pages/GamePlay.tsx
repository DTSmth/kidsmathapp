import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import { getGameDetail } from '../services/games';
import { useGameEngine } from '../hooks/useGameEngine';
import { useGameSubmit } from '../hooks/useGameSubmit';
import GameHeader from '../components/games/GameHeader';
import ComboTracker from '../components/games/ComboTracker';
import BalloonPopGame from '../components/games/BalloonPopGame';
import CountingCrittersGame from '../components/games/CountingCrittersGame';
import ShapeSafariGame from '../components/games/ShapeSafariGame';
import MathRaceGame from '../components/games/MathRaceGame';
import PatternParadeGame from '../components/games/PatternParadeGame';
import { GAME_CONFIG } from '../components/games/gameConfig';
import AnimalMascot from '../components/characters/AnimalMascot';
import type { GameDetailDto, GameType, QuestionDto } from '../types';

const GamePlay = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const { submit } = useGameSubmit();

  const [gameDetail, setGameDetail] = useState<GameDetailDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [questionIndex, setQuestionIndex] = useState(0);

  useEffect(() => {
    if (!gameId || !selectedChild) return;
    getGameDetail(Number(gameId), selectedChild.id)
      .then(data => {
        setGameDetail(data);
        setLoading(false);
      })
      .catch(() => {
        setError("Couldn't load this game. Please go back and try again.");
        setLoading(false);
      });
  }, [gameId, selectedChild]);

  const engine = useGameEngine({
    timeLimit: gameDetail?.timeLimit ?? 60,
    baseStarsReward: gameDetail?.baseStarsReward ?? 3,
    onGameOver: async result => {
      if (!selectedChild || !gameId || !gameDetail) return;
      const res = await submit(Number(gameId), {
        childId: selectedChild.id,
        score: result.score,
        timeSpent: result.timeSpent,
        comboBonus: result.comboBonus,
        answersLog: JSON.stringify(result.answersLog),
      });
      navigate(`/games/${gameId}/complete`, {
        state: { result, gameResult: res, gameName: gameDetail.name, gameType: gameDetail.gameType },
      });
    },
    onGameComplete: async result => {
      if (!selectedChild || !gameId || !gameDetail) return;
      const res = await submit(Number(gameId), {
        childId: selectedChild.id,
        score: result.score,
        timeSpent: result.timeSpent,
        comboBonus: result.comboBonus,
        answersLog: JSON.stringify(result.answersLog),
      });
      navigate(`/games/${gameId}/complete`, {
        state: { result, gameResult: res, gameName: gameDetail.name, gameType: gameDetail.gameType },
      });
    },
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-6xl animate-bounce">🎮</div>
      </div>
    );
  }

  if (error || !gameDetail) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-4 px-4">
        <p className="text-2xl">😕</p>
        <p className="text-gray-700 text-center">{error}</p>
        <button onClick={() => navigate('/play')} className="text-primary font-semibold">
          Go Back
        </button>
      </div>
    );
  }

  const cfg = GAME_CONFIG[gameDetail.gameType];
  const mascotAnimal = (selectedChild?.avatarId ?? 'leo') as 'leo' | 'ollie' | 'bella' | 'max';

  // INTRO screen
  if (engine.phase === 'intro') {
    return (
      <div
        className="min-h-screen flex flex-col items-center justify-center gap-8 px-6"
        style={{ background: `linear-gradient(135deg, ${cfg.color}22, ${cfg.color}11)` }}
      >
        <div className="text-7xl animate-float">{cfg.emoji}</div>
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-800">{gameDetail.name}</h1>
          <p className="text-gray-500 mt-2 text-sm">{gameDetail.description}</p>
          <div className="flex items-center justify-center gap-4 mt-4 text-sm text-gray-400">
            <span>⏱ {gameDetail.timeLimit}s</span>
            <span>⭐ {gameDetail.baseStarsReward} stars max</span>
            <span>❓ {gameDetail.questions.length} questions</span>
          </div>
        </div>
        <AnimalMascot
          animal={mascotAnimal}
          mood="excited"
          message="Ready to play? Let's go! 🚀"
        />
        <button
          onClick={engine.startGame}
          className="text-white font-bold text-xl rounded-2xl px-10 py-4 shadow-lg hover:scale-105 active:scale-95 transition-transform"
          style={{ backgroundColor: cfg.color }}
        >
          Start Game!
        </button>
        <button
          onClick={() => navigate('/play')}
          className="text-gray-400 text-sm hover:text-gray-600"
        >
          ← Back
        </button>
      </div>
    );
  }

  // COUNTDOWN overlay
  if (engine.phase === 'countdown') {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div
          key={engine.countdown}
          className="text-9xl font-bold animate-countdown-digit"
          style={{ color: cfg.color }}
        >
          {engine.countdown > 0 ? engine.countdown : '🚀'}
        </div>
      </div>
    );
  }

  const questions = gameDetail.questions;
  const currentQuestion: QuestionDto = questions[questionIndex % questions.length];

  const handleCorrect = (questionId: number) => {
    engine.onCorrect(questionId);
    const nextIndex = questionIndex + 1;
    setQuestionIndex(nextIndex);
    if (nextIndex >= questions.length) {
      engine.onAllQuestionsAnswered();
    }
  };

  const handleWrong = (questionId: number) => {
    engine.onWrong(questionId);
    // Don't advance when this was the last life — endGame is pending, stay on current question
    if (engine.lives <= 1) return;
    const nextIndex = questionIndex + 1;
    setQuestionIndex(nextIndex);
    if (nextIndex >= questions.length) {
      engine.onAllQuestionsAnswered();
    }
  };

  const renderGame = () => {
    const gameProps = { question: currentQuestion, onCorrect: handleCorrect, onWrong: handleWrong };
    switch (gameDetail.gameType as GameType) {
      case 'NUMBER_POP':
        return <BalloonPopGame {...gameProps} />;
      case 'COUNTING_CRITTERS':
        return <CountingCrittersGame {...gameProps} />;
      case 'SHAPE_SAFARI':
        return <ShapeSafariGame {...gameProps} />;
      case 'MATH_RACE':
        return (
          <MathRaceGame
            {...gameProps}
            questionIndex={questionIndex}
            totalQuestions={questions.length}
          />
        );
      case 'PATTERN_PARADE':
        return <PatternParadeGame {...gameProps} />;
      default:
        return <BalloonPopGame {...gameProps} />;
    }
  };

  // Show a brief loading state while score is being submitted and navigation is pending
  if (engine.phase === 'gameover' || engine.phase === 'complete') {
    return (
      <div className="h-screen flex flex-col items-center justify-center gap-4 bg-background">
        <div className="text-6xl animate-bounce">{cfg.emoji}</div>
        <p className="text-xl font-bold text-gray-700">
          {engine.phase === 'complete' ? 'Amazing! 🎉' : 'Game over! 😅'}
        </p>
        <p className="text-gray-400 text-sm">Saving your score…</p>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-background overflow-hidden">
      <GameHeader
        timeRemaining={engine.timeRemaining}
        lives={engine.lives}
        score={engine.score}
        totalQuestions={questions.length}
        emoji={cfg.emoji}
        onExit={() => navigate('/play')}
      />
      <ComboTracker
        combo={engine.combo}
        visible={engine.comboJustTriggered}
        signatureColor={cfg.color}
      />
      <div className="flex-1 overflow-hidden">
        {renderGame()}
      </div>
    </div>
  );
};

export default GamePlay;
