import { useState, useEffect } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

const CRITTERS = ['🐱', '🐶', '🐰', '🐸', '🐼', '🦊', '🐨', '🦄'];

interface Critter {
  id: string;
  emoji: string;
  y: number;      // % from top of parade zone
  speed: number;  // animation duration seconds
  caught: boolean;
}

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong: (questionId: number) => void;
}

const CountingCrittersGame = ({ question, onCorrect, onWrong }: Props) => {
  const target = parseInt(question.correctAnswer, 10);
  const [critters, setCritters] = useState<Critter[]>([]);
  const [caught, setCaught] = useState(0);
  const [submitted, setSubmitted] = useState(false);
  const { playDing, playBoing } = useSoundEffects();

  useEffect(() => {
    const count = isNaN(target) ? 5 : Math.min(Math.max(target + 2, 4), 10);
    const newCritters: Critter[] = Array.from({ length: count }, (_, i) => ({
      id: `${question.id}-${i}`,
      emoji: CRITTERS[i % CRITTERS.length],
      y: 10 + Math.random() * 60,
      speed: 5 + Math.random() * 4,
      caught: false,
    }));
    setCritters(newCritters);
    setCaught(0);
    setSubmitted(false);
  }, [question.id]);

  const handleTapCritter = (id: string) => {
    if (submitted) return;
    setCritters(prev => prev.map(c => c.id === id && !c.caught ? { ...c, caught: true } : c));
    playDing();
    setCaught(c => c + 1);
  };

  const handleSubmit = () => {
    if (submitted) return;
    setSubmitted(true);
    if (caught === target) {
      onCorrect(question.id);
    } else {
      playBoing();
      onWrong(question.id);
    }
  };

  return (
    <div className="flex flex-col h-full px-4 py-3 gap-3">
      {/* Question */}
      <div className="bg-white rounded-2xl shadow-md px-5 py-3 text-center shrink-0">
        <p className="text-lg font-bold text-gray-800">{question.questionText}</p>
      </div>

      {/* Parade zone */}
      <div className="flex-1 relative bg-green-50 rounded-2xl overflow-hidden border-2 border-green-200">
        {critters.map(critter => (
          !critter.caught && (
            <button
              key={critter.id}
              onClick={() => handleTapCritter(critter.id)}
              aria-label={`Tap ${critter.emoji}`}
              className="absolute text-4xl cursor-pointer hover:scale-110 transition-transform focus:outline-none"
              style={{
                top: `${critter.y}%`,
                left: '-60px',
                animation: `slideInRight ${critter.speed}s linear forwards`,
              }}
            >
              {critter.emoji}
            </button>
          )
        ))}
        <div className="absolute bottom-2 right-3 text-xs text-green-700 font-medium">Tap to catch!</div>
      </div>

      {/* Count basket + submit */}
      <div className="flex items-center gap-4 shrink-0">
        <div className="flex-1 bg-white rounded-2xl shadow-md px-4 py-3 flex items-center gap-3">
          <span className="text-3xl">🧺</span>
          <span className="text-4xl font-bold text-gray-800 tabular-nums">{caught}</span>
          <span className="text-sm text-gray-500">caught</span>
        </div>
        <button
          onClick={handleSubmit}
          disabled={submitted}
          className="bg-primary text-white font-bold rounded-2xl px-6 py-3 shadow-md hover:bg-primary-dark transition-colors disabled:opacity-50"
        >
          That's {caught}! ✓
        </button>
      </div>
    </div>
  );
};

export default CountingCrittersGame;
