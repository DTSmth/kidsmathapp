import { useState, useEffect, useRef } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

interface Balloon {
  id: string;
  answer: string;
  x: number;    // % from left
  color: string;
  duration: number; // animation seconds
  popped: boolean;
  wrong: boolean;
  exited: boolean;
}

const COLORS = ['#FF6B6B', '#4ECDC4', '#A084E8', '#FFE66D'];

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong: (questionId: number) => void;
}

const BalloonPopGame = ({ question, onCorrect, onWrong }: Props) => {
  const [balloons, setBalloons] = useState<Balloon[]>([]);
  const [answered, setAnswered] = useState(false);
  const { playPop, playBoing } = useSoundEffects();
  const answeredRef = useRef(false);
  // Keep stable refs to callbacks so the all-gone effect always calls the latest version
  const onWrongRef = useRef(onWrong);
  onWrongRef.current = onWrong;
  const questionIdRef = useRef(question.id);
  questionIdRef.current = question.id;

  useEffect(() => {
    // Ensure correct answer is in the balloon set
    const opts = question.options.length > 0
      ? [...question.options]
      : [question.correctAnswer, String(parseInt(question.correctAnswer) + 1), String(parseInt(question.correctAnswer) - 1), String(parseInt(question.correctAnswer) + 2)];

    const shuffled = [...new Set([...opts])].sort(() => Math.random() - 0.5).slice(0, 4);
    // Guarantee correct answer is always included
    if (!shuffled.includes(question.correctAnswer)) {
      shuffled[Math.floor(Math.random() * shuffled.length)] = question.correctAnswer;
    }

    const newBalloons: Balloon[] = shuffled.map((answer, i) => ({
      id: `${question.id}-${i}`,
      answer,
      x: 10 + (i * 22) + Math.random() * 5,
      color: COLORS[i % COLORS.length],
      duration: 6 + Math.random() * 0.8,
      popped: false,
      wrong: false,
      exited: false,
    }));
    setBalloons(newBalloons);
    setAnswered(false);
    answeredRef.current = false;
  }, [question.id]);

  // Mark a balloon as exited — pure state update, NO side effects here.
  // Side effects (calling onWrong) happen in the useEffect below to avoid
  // StrictMode's double-invocation of functional updaters causing double calls.
  const handleBalloonExit = (balloonId: string) => {
    if (answeredRef.current) return;
    setBalloons(prev => prev.map(b => b.id === balloonId ? { ...b, exited: true } : b));
  };

  // Detect when all balloons are gone and fire onWrong exactly once.
  useEffect(() => {
    if (answeredRef.current || balloons.length === 0) return;
    if (!balloons.every(b => b.exited || b.popped)) return;
    answeredRef.current = true;
    const id = setTimeout(() => onWrongRef.current(questionIdRef.current), 100);
    return () => clearTimeout(id);
  }, [balloons]);

  const handleTap = (balloon: Balloon) => {
    if (answeredRef.current || balloon.popped || balloon.exited) return;
    const isCorrect = balloon.answer === question.correctAnswer;

    if (isCorrect) {
      playPop();
      answeredRef.current = true;
      setAnswered(true);
      setBalloons(prev => prev.map(b => b.id === balloon.id ? { ...b, popped: true } : b));
      setTimeout(() => onCorrect(question.id), 300);
    } else {
      playBoing();
      // Wrong tap: shake the balloon briefly, but do NOT advance the question
      // Only penalize via onWrong if you want life penalty — for now just visual feedback
      setBalloons(prev => prev.map(b => b.id === balloon.id ? { ...b, wrong: true } : b));
      setTimeout(() => {
        setBalloons(prev => prev.map(b => b.id === balloon.id ? { ...b, wrong: false } : b));
      }, 500);
      // Note: NOT calling onWrong here — wrong taps just give visual feedback.
      // The player loses when the correct balloon escapes.
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Question */}
      <div className="px-4 pt-4 pb-2 text-center shrink-0">
        <div className="bg-white rounded-2xl shadow-md px-5 py-4">
          <p className="text-xl font-bold text-gray-800">{question.questionText}</p>
        </div>
      </div>

      {/* Balloon arena */}
      <div className="flex-1 relative overflow-hidden">
        {balloons.map(balloon => (
          !balloon.popped && !balloon.exited && (
            <button
              key={balloon.id}
              aria-label={`Answer: ${balloon.answer}`}
              onClick={() => handleTap(balloon)}
              onAnimationEnd={() => handleBalloonExit(balloon.id)}
              className={`absolute bottom-0 flex items-center justify-center rounded-full font-bold text-white text-lg shadow-lg focus:outline-none
                ${balloon.wrong ? 'animate-shake' : 'animate-balloon-float'}`}
              style={{
                left: `${balloon.x}%`,
                width: 72,
                height: 72,
                backgroundColor: balloon.color,
                animationDuration: balloon.wrong ? undefined : `${balloon.duration}s`,
              }}
            >
              {balloon.answer}
            </button>
          )
        ))}
        {answered && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-6xl animate-bounce">🎉</div>
          </div>
        )}
      </div>
    </div>
  );
};

export default BalloonPopGame;
