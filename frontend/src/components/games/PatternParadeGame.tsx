import { useState, useEffect } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong: (questionId: number) => void;
}

const PatternParadeGame = ({ question, onCorrect, onWrong }: Props) => {
  const [selected, setSelected] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<'correct' | 'wrong' | null>(null);
  const [shuffledOptions, setShuffledOptions] = useState<string[]>([]);
  const { playDing, playBoing } = useSoundEffects();

  // Parse pattern items from question text.
  // Supports two formats:
  //   Comma-separated: "🔴, 🔵, 🔴, 🔵, ?"  or  "What's missing? 1, 2, 3, ?, 5"
  //   Prose+emoji:     "What comes next? 🔴🔵🔴🔵🔴?"  (split into individual graphemes)
  const parsePatternParts = (text: string): string[] => {
    if (text.includes(',')) {
      const parts = text.split(',').map(s => s.trim());
      // Strip prose prefix from first item (e.g. "What's missing? 1" → "1")
      const first = parts[0];
      const stripped = first.match(/^.*[\s?:]\s*(.+)$/)?.[1];
      if (stripped) parts[0] = stripped.trim();
      return parts;
    }
    // No commas — strip prose prefix ("What comes next? " / "Complete: ")
    // then split the remaining emoji/char sequence into individual graphemes
    const noPrefix = text.replace(/^[\w'\s]+[?:]\s*/, '');
    if (noPrefix && noPrefix !== text) {
      return [...noPrefix].filter(c => c.trim().length > 0);
    }
    return [text];
  };

  const patternParts = parsePatternParts(question.questionText);
  const blankIndex = patternParts.findIndex(p => p === '?' || p === '___');
  const displayParts = blankIndex >= 0 ? patternParts : [...patternParts, '?'];
  const actualBlank = blankIndex >= 0 ? blankIndex : displayParts.length - 1;

  useEffect(() => {
    const opts = question.options.length > 0
      ? [...question.options].sort(() => Math.random() - 0.5).slice(0, 3)
      : [question.correctAnswer, '🟢', '🟡'].sort(() => Math.random() - 0.5);
    setShuffledOptions(opts);
    setSelected(null);
    setFeedback(null);
  }, [question.id]);

  const handleAnswer = (answer: string) => {
    if (feedback) return;
    setSelected(answer);
    const correct = answer === question.correctAnswer;
    setFeedback(correct ? 'correct' : 'wrong');

    if (correct) {
      playDing();
      setTimeout(() => onCorrect(question.id), 600);
    } else {
      playBoing();
      setTimeout(() => onWrong(question.id), 700);
    }
  };

  return (
    <div className="flex flex-col h-full px-4 py-3 gap-4">
      {/* Header */}
      <div className="bg-white rounded-2xl shadow-md px-5 py-3 text-center shrink-0">
        <p className="text-sm font-semibold text-gray-500">Complete the pattern!</p>
      </div>

      {/* Pattern display */}
      <div className="flex-1 bg-gradient-to-br from-purple/10 to-purple/5 rounded-2xl border-2 border-purple/30 flex flex-col items-center justify-center gap-4 p-4">
        {/* Pattern tiles */}
        <div className="flex flex-wrap justify-center gap-2">
          {displayParts.map((part, i) => {
            const isBlank = i === actualBlank;
            const isFilled = isBlank && selected;
            return (
              <div
                key={i}
                className={`w-14 h-14 rounded-2xl flex items-center justify-center text-2xl font-bold border-2 transition-all
                  ${isBlank && !isFilled
                    ? 'border-dashed border-purple bg-purple/10 animate-pulse-slow'
                    : isBlank && feedback === 'correct'
                    ? 'border-green-400 bg-green-50 scale-110'
                    : isBlank && feedback === 'wrong'
                    ? 'border-red-400 bg-red-50 animate-shake'
                    : 'border-white/60 bg-white shadow-sm'
                  }`}
              >
                {isBlank ? (isFilled ? selected : '?') : part}
              </div>
            );
          })}
        </div>

        <p className="text-xs text-purple font-medium">What comes next?</p>
      </div>

      {/* Answer choices */}
      <div className="flex gap-3 shrink-0 pb-1">
        {shuffledOptions.map(opt => {
          const isSelected = selected === opt;
          return (
            <button
              key={opt}
              onClick={() => handleAnswer(opt)}
              disabled={!!feedback}
              className={`flex-1 h-16 rounded-2xl text-2xl font-bold border-2 shadow-md transition-transform
                ${isSelected && feedback === 'correct' ? 'bg-green-100 border-green-400 scale-105' : ''}
                ${isSelected && feedback === 'wrong' ? 'bg-red-100 border-red-400 animate-shake' : ''}
                ${!feedback || !isSelected ? 'bg-white border-gray-200 hover:scale-105 active:scale-95 hover:border-purple' : ''}
                ${feedback && !isSelected ? 'opacity-50' : ''}`}
            >
              {opt}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default PatternParadeGame;
