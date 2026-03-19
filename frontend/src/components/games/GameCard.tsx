import { Star } from 'lucide-react';
import type { GameDto } from '../../types';
import { GAME_CONFIG } from './gameConfig';

interface GameCardProps {
  game: GameDto;
  onClick: () => void;
}

const MAX_STARS = 3;

const GameCard = ({ game, onClick }: GameCardProps) => {
  const cfg = GAME_CONFIG[game.gameType];
  const earnedStars = game.personalBestStars ?? 0;

  return (
    <button
      onClick={onClick}
      className="relative w-full rounded-3xl overflow-hidden aspect-[5/6] shadow-lg hover:scale-105 active:scale-95 transition-transform focus:outline-none focus:ring-4 focus:ring-white/60"
      style={{ background: cfg.gradient }}
    >
      {/* Grade badge */}
      <div className="absolute top-3 left-3 text-xs font-bold px-2.5 py-1 rounded-full bg-black/20 text-white">
        Gr {cfg.gradeLabel}
      </div>

      {/* Centered emoji */}
      <div className="absolute inset-0 flex items-center justify-center">
        <div className="w-24 h-24 rounded-full bg-white/20 flex items-center justify-center">
          <span className="text-6xl animate-float">{cfg.emoji}</span>
        </div>
      </div>

      {/* Bottom info overlay */}
      <div className="absolute bottom-0 inset-x-0 px-3 pb-3 pt-8" style={{ background: 'linear-gradient(to top, rgba(0,0,0,0.35), transparent)' }}>
        <p className="font-extrabold text-white text-sm leading-tight drop-shadow">{game.name}</p>
        <div className="flex gap-0.5 mt-1.5">
          {Array.from({ length: MAX_STARS }).map((_, i) => (
            <Star
              key={i}
              className={`w-3.5 h-3.5 ${i < earnedStars ? 'text-yellow-300 fill-current' : 'text-white/40 fill-current'}`}
            />
          ))}
        </div>
      </div>
    </button>
  );
};

export default GameCard;
