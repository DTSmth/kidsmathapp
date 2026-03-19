import { Star } from 'lucide-react';
import type { GameDto } from '../../types';
import { GAME_CONFIG } from './gameConfig';

interface GameCardProps {
  game: GameDto;
  onClick: () => void;
}

const GameCard = ({ game, onClick }: GameCardProps) => {
  const cfg = GAME_CONFIG[game.gameType];

  return (
    <button
      onClick={onClick}
      className={`relative w-full rounded-2xl bg-gradient-to-br ${cfg.gradient} border-2 border-white/60 shadow-md p-4 flex flex-col justify-between aspect-[5/6] transition-transform hover:scale-105 active:scale-95 focus:outline-none focus:ring-2 focus:ring-offset-2`}
      style={{ '--tw-ring-color': cfg.color } as React.CSSProperties}
    >
      {/* Grade badge */}
      <div
        className="self-start text-xs font-bold px-2 py-0.5 rounded-full text-white"
        style={{ backgroundColor: cfg.color }}
      >
        Grade {cfg.gradeLabel}
      </div>

      {/* Emoji */}
      <div className="text-5xl self-end animate-float">{cfg.emoji}</div>

      {/* Name + personal best */}
      <div className="text-left">
        <p className="font-bold text-gray-800 text-sm leading-tight">{game.name}</p>
        {game.personalBestStars !== null && game.personalBestStars !== undefined ? (
          <div className="flex items-center gap-1 mt-1">
            <Star className="w-3 h-3 text-amber-500 fill-current" />
            <span className="text-xs text-gray-600">{game.personalBestStars} best</span>
          </div>
        ) : (
          <p className="text-xs text-gray-400 mt-1">— first play!</p>
        )}
      </div>
    </button>
  );
};

export default GameCard;
