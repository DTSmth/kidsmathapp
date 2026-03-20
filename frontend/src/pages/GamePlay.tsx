import { useState, useEffect, useRef } from 'react';
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

// Ghost race types
type GhostEntry = { questionId: number; answeredAt: number };

// Speed tier helper for Endless Rush
const speedTier = (correct: number) => {
  if (correct >= 15) return { emoji: '⚡', label: 'Lightning!', color: '#FFE66D' };
  if (correct >= 10) return { emoji: '🚀', label: 'Speeding Up', color: '#A084E8' };
  if (correct >= 5)  return { emoji: '🐇', label: 'Getting Fast', color: '#4ECDC4' };
  return { emoji: '🐢', label: 'Warm Up', color: '#98D8C8' };
};

const GamePlay = () => {
  const { gameId } = useParams<{ gameId: string }>();
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const { submit } = useGameSubmit();

  const [gameDetail, setGameDetail] = useState<GameDetailDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [questionIndex, setQuestionIndex] = useState(0);

  // Mode toggle state
  const [gameMode, setGameMode] = useState<'NORMAL' | 'ENDLESS'>('NORMAL');

  // Ghost race state
  const [ghostLog, setGhostLog] = useState<GhostEntry[]>([]);
  const [ghostScore, setGhostScore] = useState(0);
  const playStartMs = useRef(0);

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

  // Parse ghost log from best run
  useEffect(() => {
    if (gameDetail?.bestAnswersLog) {
      try {
        setGhostLog(JSON.parse(gameDetail.bestAnswersLog));
      } catch { /* ignore */ }
    }
  }, [gameDetail]);

  const engine = useGameEngine({
    timeLimit: gameMode === 'ENDLESS' ? 99999 : (gameDetail?.timeLimit ?? 60),
    baseStarsReward: gameDetail?.baseStarsReward ?? 3,
    onGameOver: async result => {
      if (!selectedChild || !gameId || !gameDetail) return;
      const res = await submit(Number(gameId), {
        childId: selectedChild.id,
        score: gameMode === 'ENDLESS'
          ? Math.min(100, Math.round((result.score / 20) * 100))
          : result.score,
        timeSpent: result.timeSpent,
        comboBonus: result.comboBonus,
        answersLog: JSON.stringify(result.answersLog),
        gameMode,
      });
      navigate(`/games/${gameId}/complete`, {
        state: { result, gameResult: res, gameName: gameDetail.name, gameType: gameDetail.gameType, gameMode },
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
        gameMode,
      });
      navigate(`/games/${gameId}/complete`, {
        state: { result, gameResult: res, gameName: gameDetail.name, gameType: gameDetail.gameType, gameMode },
      });
    },
  });

  // Track game start time for ghost race
  useEffect(() => {
    if (engine.phase === 'playing' && playStartMs.current === 0) {
      playStartMs.current = Date.now();
    }
    if (engine.phase === 'intro') {
      playStartMs.current = 0;
      setGhostScore(0);
      setQuestionIndex(0);
    }
  }, [engine.phase]);

  // Advance ghost score every 500ms during playing phase (NORMAL mode only)
  useEffect(() => {
    if (engine.phase !== 'playing' || ghostLog.length === 0 || gameMode !== 'NORMAL') return;
    const interval = setInterval(() => {
      const elapsed = Date.now() - playStartMs.current;
      const count = ghostLog.filter(e => e.answeredAt <= elapsed).length;
      setGhostScore(count);
    }, 500);
    return () => clearInterval(interval);
  }, [engine.phase, ghostLog, gameMode]);

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
            {gameMode === 'NORMAL' && <span>⏱ {gameDetail.timeLimit}s</span>}
            <span>⭐ {gameDetail.baseStarsReward} stars max</span>
            <span>❓ {gameDetail.questions.length} questions</span>
            {gameMode === 'ENDLESS' && <span>⚡ Endless mode</span>}
          </div>
        </div>

        {/* Mode selector */}
        <div
          className="flex gap-1 p-1 rounded-2xl"
          style={{ backgroundColor: `${cfg.color}33` }}
        >
          <button
            onClick={() => setGameMode('NORMAL')}
            className={`px-4 py-2 rounded-xl font-bold text-sm transition-all ${
              gameMode === 'NORMAL' ? 'bg-white shadow-md text-gray-800' : 'text-white/70'
            }`}
          >
            🏁 Normal
          </button>
          <button
            onClick={() => setGameMode('ENDLESS')}
            className={`px-4 py-2 rounded-xl font-bold text-sm transition-all ${
              gameMode === 'ENDLESS' ? 'bg-white shadow-md text-gray-800' : 'text-white/70'
            }`}
          >
            ⚡ Endless Rush
          </button>
        </div>

        <AnimalMascot
          animal={mascotAnimal}
          mood="excited"
          message={gameMode === 'ENDLESS' ? 'Answer as many as you can! 🔥' : 'Ready to play? Let\'s go! 🚀'}
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
    // In ENDLESS mode, questions never end — only lives running out stops the game
    if (gameMode === 'NORMAL' && nextIndex >= questions.length) {
      engine.onAllQuestionsAnswered();
    }
  };

  const handleWrong = (questionId: number) => {
    engine.onWrong(questionId);
    // Don't advance when this was the last life — endGame is pending, stay on current question
    if (engine.lives <= 1) return;
    const nextIndex = questionIndex + 1;
    setQuestionIndex(nextIndex);
    // In ENDLESS mode, never trigger completion — only lives ending stops the game
    if (gameMode === 'NORMAL' && nextIndex >= questions.length) {
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
        {engine.phase === 'gameover' && gameMode === 'ENDLESS' && (
          <p className="text-2xl font-bold text-primary">
            Total correct: {engine.score}
          </p>
        )}
        <p className="text-gray-400 text-sm">Saving your score…</p>
      </div>
    );
  }

  const tier = speedTier(engine.score);

  return (
    <div className="h-screen flex flex-col bg-background overflow-hidden">
      <GameHeader
        timeRemaining={gameMode === 'ENDLESS' ? undefined : engine.timeRemaining}
        lives={engine.lives}
        score={engine.score}
        totalQuestions={gameMode === 'ENDLESS' ? undefined : questions.length}
        emoji={cfg.emoji}
        onExit={() => navigate('/play')}
      />

      {/* Endless Rush speed tier indicator */}
      {gameMode === 'ENDLESS' && (
        <div
          className="mx-4 mt-1 px-4 py-2 rounded-xl flex items-center justify-between text-sm font-bold"
          style={{ backgroundColor: `${tier.color}33`, color: tier.color }}
        >
          <span>{tier.emoji} {tier.label}</span>
          <span className="text-gray-700 font-bold">{engine.score} correct</span>
        </div>
      )}

      <ComboTracker
        combo={engine.combo}
        visible={engine.comboJustTriggered}
        signatureColor={cfg.color}
      />

      {/* Game area — relative so ghost badge can float over it */}
      <div className="flex-1 overflow-hidden relative">
        {/* Ghost race badge (NORMAL mode only) */}
        {ghostLog.length > 0 && gameMode === 'NORMAL' && (
          <div className="absolute top-3 right-4 z-10 bg-white/80 backdrop-blur rounded-xl px-3 py-2 text-sm shadow">
            <div className="text-gray-400 text-xs">👻 Ghost</div>
            <div className="font-bold text-gray-600">{ghostScore} correct</div>
          </div>
        )}
        {renderGame()}
      </div>
    </div>
  );
};

export default GamePlay;
