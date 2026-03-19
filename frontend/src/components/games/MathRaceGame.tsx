import { useState, useEffect, useRef } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong: (questionId: number) => void;
  questionIndex: number;
  totalQuestions: number;
}

const ROCKET_COLORS = ['#FF6B6B', '#4ECDC4', '#A084E8', '#FFE66D'];

const MathRaceGame = ({ question, onCorrect, onWrong, questionIndex, totalQuestions }: Props) => {
  const [selected, setSelected] = useState<string | null>(null);
  const [isCorrect, setIsCorrect] = useState<boolean | null>(null);
  const [rocketPos, setRocketPos] = useState(0); // 0-100%
  const [shuffledOptions, setShuffledOptions] = useState<string[]>([]);
  const { playBoing, playWhoosh } = useSoundEffects();
  const lockRef = useRef(false);

  const trackProgress = Math.round((questionIndex / totalQuestions) * 100);

  useEffect(() => {
    const opts = [...question.options].sort(() => Math.random() - 0.5).slice(0, 4);
    setShuffledOptions(opts);
    setSelected(null);
    setIsCorrect(null);
    lockRef.current = false;
  }, [question.id]);

  useEffect(() => {
    setRocketPos(trackProgress);
  }, [trackProgress]);

  const handleAnswer = (answer: string) => {
    if (lockRef.current) return;
    lockRef.current = true;
    setSelected(answer);
    const correct = answer === question.correctAnswer;
    setIsCorrect(correct);

    if (correct) {
      playWhoosh();
      setTimeout(() => {
        onCorrect(question.id);
        lockRef.current = false;
        setSelected(null);
        setIsCorrect(null);
      }, 600);
    } else {
      playBoing();
      setTimeout(() => {
        onWrong(question.id);
        lockRef.current = false;
        setSelected(null);
        setIsCorrect(null);
      }, 700);
    }
  };

  return (
    <div className="flex flex-col h-full px-4 py-3 gap-4">
      {/* Race track */}
      <div className="shrink-0">
        <div className="relative h-16 bg-gray-100 rounded-2xl overflow-hidden border-2 border-gray-200">
          {/* Track markings */}
          {[25, 50, 75].map(p => (
            <div
              key={p}
              className="absolute top-0 bottom-0 w-px bg-gray-300 opacity-60"
              style={{ left: `${p}%` }}
            />
          ))}
          {/* Finish flag */}
          <div className="absolute right-3 top-1/2 -translate-y-1/2 text-xl">🏁</div>
          {/* Rocket */}
          <div
            className="absolute top-1/2 -translate-y-1/2 text-3xl transition-all duration-500"
            style={{ left: `calc(${rocketPos}% - 16px)` }}
          >
            🚀
          </div>
          {/* Progress label */}
          <div className="absolute left-3 top-1/2 -translate-y-1/2">
            <span className="text-xs font-bold text-gray-500">{questionIndex}/{totalQuestions}</span>
          </div>
        </div>
      </div>

      {/* Question */}
      <div className="bg-white rounded-2xl shadow-md px-5 py-4 text-center shrink-0">
        <p className="text-2xl font-bold text-gray-800">{question.questionText}</p>
      </div>

      {/* Answer grid */}
      <div className="grid grid-cols-2 gap-3 flex-1">
        {shuffledOptions.map((opt, i) => {
          const isSelected = selected === opt;
          const isRight = isSelected && isCorrect === true;
          const isWrong = isSelected && isCorrect === false;

          return (
            <button
              key={opt}
              onClick={() => handleAnswer(opt)}
              disabled={!!selected}
              className={`flex items-center justify-center rounded-2xl font-bold text-white text-2xl shadow-md transition-transform
                ${isRight ? 'scale-105 bg-green-500' : ''}
                ${isWrong ? 'animate-shake' : ''}
                ${!isSelected ? 'hover:scale-105 active:scale-95' : ''}
                ${selected && !isSelected ? 'opacity-60' : ''}`}
              style={{
                backgroundColor: isRight ? '#22C55E' : isWrong ? '#EF4444' : ROCKET_COLORS[i % ROCKET_COLORS.length],
              }}
            >
              {opt}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default MathRaceGame;
