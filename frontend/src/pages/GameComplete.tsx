import { useEffect, useState } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { GAME_CONFIG } from '../components/games/gameConfig';
import type { GameResult, GameScoreResult, GameType } from '../types';
import { Star, Trophy, RotateCcw, Home } from 'lucide-react';

interface LocationState {
  result: GameResult;
  gameResult: GameScoreResult | null;
  gameName: string;
  gameType: GameType;
}

const GameComplete = () => {
  const navigate = useNavigate();
  const { gameId } = useParams<{ gameId: string }>();
  const location = useLocation();
  const state = location.state as LocationState | null;

  const [starsVisible, setStarsVisible] = useState(0);

  const result = state?.result;
  const gameResult = state?.gameResult;
  const gameName = state?.gameName ?? 'Game';
  const gameType = state?.gameType ?? 'NUMBER_POP';
  const cfg = GAME_CONFIG[gameType];

  const starsEarned = gameResult?.starsEarned ?? result?.starsEarned ?? 0;
  const isNewPB = gameResult?.isNewPersonalBest ?? false;
  const score = result?.score ?? 0;
  const passed = score >= 50;

  // Animate stars in one by one
  useEffect(() => {
    if (starsEarned === 0) {
      setStarsVisible(0);
      return;
    }
    let shown = 0;
    const interval = setInterval(() => {
      shown += 1;
      setStarsVisible(shown);
      if (shown >= starsEarned) clearInterval(interval);
    }, 350);
    return () => clearInterval(interval);
  }, [starsEarned]);

  if (!state) {
    navigate('/play');
    return null;
  }

  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center gap-6 px-6 py-10"
      style={{ background: `linear-gradient(135deg, ${cfg.color}22, ${cfg.color}08)` }}
    >
      {/* Game emoji */}
      <div className="text-7xl animate-bounce">{cfg.emoji}</div>

      {/* Title */}
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-800">
          {passed ? 'Amazing! 🎉' : 'Good try! 💪'}
        </h1>
        <p className="text-gray-500 mt-1 text-sm">{gameName}</p>
      </div>

      {/* Score */}
      <div className="bg-white rounded-3xl shadow-lg border-2 border-gray-100 px-8 py-6 text-center w-full max-w-xs">
        <p className="text-5xl font-bold" style={{ color: cfg.color }}>{score}%</p>
        <p className="text-gray-400 text-sm mt-1">accuracy</p>
      </div>

      {/* Stars */}
      <div className="flex items-center gap-2">
        {Array.from({ length: 3 }, (_, i) => (
          <Star
            key={i}
            className={`w-12 h-12 transition-all duration-300 ${
              i < starsVisible
                ? 'text-amber-400 fill-current scale-110'
                : 'text-gray-200 fill-current'
            }`}
          />
        ))}
      </div>

      {/* New personal best badge */}
      {isNewPB && (
        <div className="flex items-center gap-2 bg-amber-50 border-2 border-amber-300 rounded-2xl px-5 py-3 animate-bounce-in">
          <Trophy className="w-5 h-5 text-amber-500" />
          <span className="font-bold text-amber-700">New Personal Best!</span>
        </div>
      )}

      {/* New achievements */}
      {gameResult?.newAchievements && gameResult.newAchievements.length > 0 && (
        <div className="w-full max-w-xs space-y-2">
          {gameResult.newAchievements.map(a => (
            <div
              key={a.id}
              className="flex items-center gap-3 bg-purple/10 border border-purple/30 rounded-2xl px-4 py-3"
            >
              <Trophy className="w-5 h-5 text-purple shrink-0" />
              <div>
                <p className="font-bold text-sm text-gray-800">{a.name}</p>
                <p className="text-xs text-gray-500">{a.description}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Actions */}
      <div className="flex gap-3 w-full max-w-xs">
        <button
          onClick={() => navigate(`/games/${gameId}/play`)}
          className="flex-1 flex items-center justify-center gap-2 bg-white border-2 border-gray-200 text-gray-700 font-bold rounded-2xl py-3 hover:border-gray-300 transition-colors"
        >
          <RotateCcw className="w-4 h-4" />
          Play Again
        </button>
        <button
          onClick={() => navigate('/play')}
          className="flex-1 flex items-center justify-center gap-2 text-white font-bold rounded-2xl py-3 shadow-md hover:opacity-90 transition-opacity"
          style={{ backgroundColor: cfg.color }}
        >
          <Home className="w-4 h-4" />
          More Games
        </button>
      </div>
    </div>
  );
};

export default GameComplete;
