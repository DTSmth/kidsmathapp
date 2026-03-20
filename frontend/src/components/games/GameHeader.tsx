import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface GameHeaderProps {
  emoji: string;
  timeRemaining?: number;
  lives: number;
  maxLives?: number;
  score: number;
  totalQuestions?: number;
  onExit?: () => void;
}

const GameHeader = ({
  emoji,
  timeRemaining,
  lives,
  maxLives = 3,
  score,
  totalQuestions,
  onExit,
}: GameHeaderProps) => {
  const navigate = useNavigate();
  const isLow = timeRemaining !== undefined && timeRemaining <= 10;

  const handleExit = () => {
    if (onExit) onExit();
    else navigate('/play');
  };

  return (
    <div className="sticky top-0 z-40 bg-white border-b border-gray-200 px-4 py-2 flex items-center gap-3 h-12">
      <button
        onClick={handleExit}
        aria-label="Exit game"
        className="p-1.5 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 transition-colors shrink-0"
      >
        <ArrowLeft className="w-5 h-5" />
      </button>

      <span className="text-xl shrink-0">{emoji}</span>

      {/* Timer — hidden in Endless Rush mode */}
      {timeRemaining !== undefined && (
        <div
          className={`font-mono font-bold tabular-nums text-sm px-2 py-0.5 rounded-lg shrink-0 ${
            isLow ? 'text-coral bg-coral/10 animate-pulse' : 'text-gray-700 bg-gray-100'
          }`}
          aria-live="polite"
          aria-label={`${timeRemaining} seconds remaining`}
        >
          {timeRemaining}s
        </div>
      )}

      <div className="flex-1" />

      {/* Score */}
      <span className="text-sm font-semibold text-gray-600 shrink-0">
        {totalQuestions !== undefined ? `${score}/${totalQuestions}` : `${score} ⚡`}
      </span>

      {/* Lives */}
      <div className="flex gap-0.5 shrink-0" aria-label={`${lives} lives remaining`}>
        {Array.from({ length: maxLives }).map((_, i) => (
          <span key={i} className={`text-lg ${i < lives ? '' : 'opacity-20'}`}>
            {i < lives ? '❤️' : '♡'}
          </span>
        ))}
      </div>
    </div>
  );
};

export default GameHeader;
