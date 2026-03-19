import { useMemo } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

const ANSWER_COLORS = ['#FF6B6B', '#4ECDC4', '#A084E8', '#F59E0B'];

/** Extract the first emoji from a string, e.g. "Count the puppies! 🐕🐕" → "🐕" */
function extractEmoji(text: string): string {
  const match = text.match(/\p{Extended_Pictographic}/u);
  return match ? match[0] : '🐾';
}

/** Strip all emojis (and trailing punctuation/spaces) from display text */
function stripEmojis(text: string): string {
  return text.replace(/\p{Extended_Pictographic}+/gu, '').trim().replace(/[!?,.\s]+$/, '').trim();
}

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong: (questionId: number) => void;
}

function generateFallbackOptions(target: number): string[] {
  const opts = new Set<string>();
  opts.add(String(target));
  const candidates = [target - 2, target - 1, target + 1, target + 2].filter(n => n > 0);
  for (const n of candidates) {
    if (opts.size >= 4) break;
    opts.add(String(n));
  }
  return [...opts].sort(() => Math.random() - 0.5);
}

const CountingCrittersGame = ({ question, onCorrect, onWrong }: Props) => {
  const target = parseInt(question.correctAnswer, 10);
  const critterEmoji = extractEmoji(question.questionText);
  const displayText = stripEmojis(question.questionText);
  const { playDing, playBoing } = useSoundEffects();

  // Stable scattered positions — computed once per question
  const positions = useMemo(() => {
    const count = isNaN(target) ? 4 : Math.min(Math.max(target, 1), 10);
    const cols = count <= 4 ? count : Math.min(count, 5);
    return Array.from({ length: count }, (_, i) => {
      const col = i % cols;
      const row = Math.floor(i / cols);
      const totalRows = Math.ceil(count / cols);
      const cellW = 90 / cols;
      const cellH = 80 / Math.max(totalRows, 1);
      return {
        id: i,
        // center of cell + small fixed jitter based on index (no Math.random — stable across re-renders)
        x: 5 + col * cellW + cellW * 0.25 + (i % 3) * 4,
        y: 8 + row * cellH + cellH * 0.2 + (i % 2) * 5,
        delay: ((i * 0.3) % 1.5).toFixed(1),
      };
    });
  }, [question.id, target]);

  const options = useMemo(() => {
    const opts = question.options.length >= 4
      ? [...question.options].sort(() => Math.random() - 0.5)
      : generateFallbackOptions(isNaN(target) ? 0 : target);
    return opts.slice(0, 4);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [question.id]);

  const handleAnswer = (answer: string) => {
    if (answer === question.correctAnswer) {
      playDing();
      onCorrect(question.id);
    } else {
      playBoing();
      onWrong(question.id);
    }
  };

  return (
    <div className="flex flex-col h-full px-4 py-3 gap-3">
      {/* Question */}
      <div className="bg-white rounded-2xl shadow-md px-5 py-4 text-center shrink-0">
        <p className="text-xl font-bold text-gray-800">{displayText}</p>
      </div>

      {/* Meadow — static critters that gently bob */}
      <div className="flex-1 relative bg-green-50 rounded-2xl border-2 border-green-200 overflow-hidden">
        {/* Decorative grass strip */}
        <div className="absolute bottom-0 inset-x-0 h-6 bg-green-200 rounded-b-2xl" />

        {positions.map(pos => (
          <span
            key={pos.id}
            className="absolute text-5xl select-none pointer-events-none animate-mascot-bounce"
            style={{
              left: `${pos.x}%`,
              top: `${pos.y}%`,
              animationDelay: `${pos.delay}s`,
            }}
          >
            {critterEmoji}
          </span>
        ))}

        {/* Corner hint */}
        <div className="absolute top-2 right-3 text-xs font-semibold text-green-600 bg-green-100 px-2 py-0.5 rounded-full">
          Count them! 👇
        </div>
      </div>

      {/* Answer buttons */}
      <div className="grid grid-cols-4 gap-2 shrink-0">
        {options.map((opt, i) => (
          <button
            key={opt}
            onClick={() => handleAnswer(opt)}
            className="rounded-2xl py-4 font-extrabold text-2xl text-white shadow-md hover:scale-105 active:scale-95 transition-transform focus:outline-none"
            style={{ backgroundColor: ANSWER_COLORS[i % ANSWER_COLORS.length] }}
          >
            {opt}
          </button>
        ))}
      </div>
    </div>
  );
};

export default CountingCrittersGame;
